package com.tar.iq.activity.filmdetail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tar.iq.R;
import com.tar.iq.activity.dashboard.MainActivity;
import com.tar.iq.api.DataService;
import com.tar.iq.model.Filme;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.tar.iq.util.Constantes.BASE_URL;

public class FilmeDetalhes extends AppCompatActivity {

    private Filme filme;
    private TextView textTitle, textYear, textDirector, textActors, textType, textRating, textWriter, textPlot, textRuntime, textGenre;
    private ImageView imgFilme;
    private ImageButton imgBtnFavoritar;

    private String strId;
    private Retrofit retrofit;
    private ArrayList<Filme.Rating> listaRatings;
    private boolean achou=false;
    private TextView titulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filme_detalhes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFE4E1"));
        }
        titulo = findViewById(R.id.textToolbar);

        textTitle = findViewById(R.id.textTitulo);
        imgFilme = findViewById(R.id.imgFilme);
        textYear = findViewById(R.id.textAno);
        textType = findViewById(R.id.textTipo);
        textActors = findViewById(R.id.textActors);
        textDirector = findViewById(R.id.textDirector);
        textWriter = findViewById(R.id.textWriter);
        textGenre =  findViewById(R.id.textGenre);
        textPlot =  findViewById(R.id.textPlot);
        textRating =  findViewById(R.id.textRating);
        textRuntime =  findViewById(R.id.textRuntime);
        imgBtnFavoritar = findViewById(R.id.imgBtnFavoritar);


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        //toolbar.setTitle("Detalhes");
        setSupportActionBar(toolbar);

        titulo.setText("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        filme = (Filme) getIntent().getSerializableExtra("filme");
        strId = filme.getImdbID();
       // Log.i("XXX", "ID RECEBIDO: "+strId);
        pesquisarRetrofit(strId);
        carregarIcone();

    }
    //---------------------------------------------------------------------------------------------

    private void carregarIcone(){
        try{
            if(!verificarRegistroBanco().isEmpty()){
                this.imgBtnFavoritar.setImageResource(R.drawable.ic_favoritos_branco_30dp);
                this.imgBtnFavoritar.setTag("ic_favoritos_branco_30dp");
                achou=true;
            }
            else{
                this.imgBtnFavoritar.setImageResource(R.drawable.ic_favoritos_borda_branco_30dp);
                this.imgBtnFavoritar.setTag("ic_favoritos_borda_branco_30dp");
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------------------------------
    private List<Filme> verificarRegistroBanco(){
        List<Filme> list = new ArrayList<Filme>();
        try {
            Cursor cursor = MainActivity.banco.rawQuery("SELECT*FROM Filme WHERE imdbID ='"+filme.getImdbID()+"'",null);
           while (cursor.moveToNext()) {
                Filme f = new Filme();
                f.setImdbID(cursor.getString(cursor.getColumnIndex("imdbID")));
                list.add(f);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
    //----------------------------------------------------------------------------------------------
    public void favoritar(View v){
        try {

            if(v.getTag().equals("ic_favoritos_borda_branco_30dp") && !achou)
            {
                Toast.makeText(this, "Saved to favorites!", Toast.LENGTH_SHORT).show();
                imgBtnFavoritar.setImageResource(R.drawable.ic_favoritos_branco_30dp);
                v.setTag("ic_favoritos_roxo_24dp");
                MainActivity.banco.execSQL("INSERT INTO Filme(imdbID) VALUES('"+filme.getImdbID()+"')");
            }
            else{
                Toast.makeText(this, "Removed from favorites!", Toast.LENGTH_SHORT).show();
                imgBtnFavoritar.setImageResource(R.drawable.ic_favoritos_borda_branco_30dp);
                v.setTag("ic_favoritos_borda_branco_30dp");
                MainActivity.banco.execSQL("DELETE FROM Filme WHERE imdbID = '"+filme.getImdbID()+"'");
                //Log.d("XXX", "ID DO CLIQUE DELETADO: "+filme.getImdbID());

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------

    private void pesquisarRetrofit(String id) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DataService servicosRetrofit = retrofit.create(DataService.class);

        Call<Filme> call = servicosRetrofit.recuperarFilmesId(id);

        call.enqueue(new Callback<Filme>() {
            @Override
            public void onResponse(Call<Filme> call, retrofit2.Response<Filme> response) {

                if (response.isSuccessful()) {

                    Filme filme = response.body();
                    listaRatings = filme.getRatings();

                    textTitle.setText(filme.getTitle());
                    textYear.setText("Year: " + filme.getYear());
                    textType.setText("Type: "+filme.getType());
                    textActors.setText("Cast: "+filme.getActors());
                    textDirector.setText("Directors: "+filme.getDirector());
                    textWriter.setText("Writers: "+filme.getWriter());
                    textGenre.setText("Gender: "+filme.getGenre());
                    textPlot.setText("Plot: "+filme.getPlot());
                    textRating.setText("Assessment: "+listaRatings.get(0).getValue());
                    textRuntime.setText("Duration: "+filme.getRuntime());

                    Picasso.with(getApplicationContext())
                                .load(filme.getPoster())
                                .into(imgFilme);
                    } else{
                }
            }

            @Override
            public void onFailure(Call<Filme> call, Throwable t) {
            }

        });
    }
}
