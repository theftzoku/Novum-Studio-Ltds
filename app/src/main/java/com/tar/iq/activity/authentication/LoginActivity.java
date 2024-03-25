package com.tar.iq.activity.authentication;


import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tar.iq.R;
import com.tar.iq.activity.dashboard.MainActivity;
import com.tar.iq.util.FingerprintHandler;
import com.tar.iq.util.SharedPrefUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    public static final String IS_ADMIN_KEY = "isAdmin";
    public static final String FCM_TOKEN = "fcmToken";
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    ImageView finger;
    public FirebaseAuth mAuth;
    public FirebaseFirestore dbFirestore;
    public DocumentReference userInfo;

    private EditText emailField;
    private EditText passwordField;
    public ProgressBar progressBar;
    private SharedPrefUtils prefUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setProgressBar(R.id.progressBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFE4E1"));
        }
        prefUtils = new SharedPrefUtils(this);

        mAuth = FirebaseAuth.getInstance();    // Initialize Firebase Authentication
        dbFirestore = FirebaseFirestore.getInstance();    // Initialize Cloud Firestore

        // Access UI widgets
        emailField = findViewById(R.id.fieldEmail);
        passwordField = findViewById(R.id.fieldPassword);
        finger = findViewById(R.id.fingerprint);

        // Access buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        findViewById(R.id.fingerprint).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        } else if (i == R.id.emailSignInButton) {
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }else if (i == R.id.fingerprint) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {


                keyguardManager =
                        (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                fingerprintManager =
                        (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

                textView = (TextView) findViewById(R.id.textview);

                if (!fingerprintManager.isHardwareDetected()) {

                    textView.setText("Your device doesn't support fingerprint authentication");

                }



                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    textView.setText("Please enable the fingerprint permission");

                }

                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    textView.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");

                }

                if (!keyguardManager.isKeyguardSecure()) {
                    textView.setText("Please enable lockscreen security in your device's Settings");
                } else {
                    try {
                        generateKey();
                    } catch (FingerprintException e) {
                        e.printStackTrace();
                    }
                    if (initCipher()) {
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerprintHandler helper = new FingerprintHandler(this);
                        helper.startAuth(fingerprintManager, cryptoObject);
                    }
                }

            }

        }
    }
    private void generateKey() throws FingerprintException {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");


            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (KeyStoreException
                 | NoSuchAlgorithmException
                 | NoSuchProviderException
                 | InvalidAlgorithmParameterException
                 | CertificateException
                 | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }


    }



    public boolean initCipher() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                 NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                 | UnrecoverableKeyException | IOException
                 | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }



    private class FingerprintException extends Exception {

        public FingerprintException(Exception e) {
            super(e);
        }
    }

    private void createAccount(String email, String password) {
        // Creates a new account
        if (!validateForm()) {
            return;
        }
        showProgressBar();    // show the progress before the account formation process

        // Create user with email using the Firebase Auth's method
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    assert user != null;
                    String user_Id = user.getUid();
                    // Create a hashmap of user admin privileges to add to 'users' collection in Firestore
                    Map<String, Object> userAccessLevel = new HashMap<String, Object>();
                    userAccessLevel.put(IS_ADMIN_KEY, "false");     // By default, a new user is not an admin
                    // Add user admin privileges to 'users' collection in Firestore
                    dbFirestore.collection("users").document(user_Id).set(userAccessLevel);
                    updateUI(user);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Account formation failed. Please try again.", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
                hideProgressBar();   // hide the progress bar after the account formation process
            }
        });
    }

    private void signIn(String email, String password) {
        // Signs in a user

        if (!validateForm()) {
            return;
        }
        showProgressBar();    // show the progress bar before the sign-in

        // Sign in with email
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
                hideProgressBar();   // hide the progress bar after signing in
            }
        });
    }

    private boolean validateForm() {
        // Makes sure that email and password are entered

        boolean valid = true;
        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");  // Require Email
            valid = false;
        } else {
            emailField.setError(null);
        }
        String password = passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required.");   // Require Email
            valid = false;
        } else {
            passwordField.setError(null);
        }
        return valid;
    }

    private void generateAndSaveFCMToken(final FirebaseUser user) {
        // Generates the FCM (Firebase Cloud Messaging) registration token and stores in a document associated with the user

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();  // Get new FCM registration token

                Map<String, Object> userToken = new HashMap<String, Object>();
                userToken.put(FCM_TOKEN, token);
                dbFirestore.collection("users").document(user.getUid()).update(userToken);  // Add token to Firestore
            }
        });
    }


    private void updateUI(final FirebaseUser user) {
        // Goes to next page depending on the user type

        hideProgressBar();    // hide the progress bar

        if (user != null) {
            userInfo = dbFirestore.collection("users").document(user.getUid());
            userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null) {
                            generateAndSaveFCMToken(user);  // generate new token for users only when signing in and creating a new account
                            showRegularUI();
                        } else {
                            Log.d(TAG, "Getting user document failed: ", task.getException());
                        }
                    }
                }
            });
        } else {
            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
        }
    }


    private void showRegularUI() {
        // Switches to regular user UI
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // clears the stack (disables going back with back button)
        startActivity(intent);
    }

    public void setProgressBar(int resId) {
        progressBar = findViewById(resId);
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
