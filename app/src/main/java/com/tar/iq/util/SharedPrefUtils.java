package com.tar.iq.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SharedPrefUtils {
    public static final String MY_PREFS_NAME = "MiCarService";

    private final Context activity;

    public SharedPrefUtils(Context activity) {
        this.activity = activity;
    }

    public void save(String key, String name) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, name);
        editor.apply();
    }

    public String get(String key) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public void setBool(String key, boolean name) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(key, name);
        editor.apply();
    }

    public boolean getBool(String key) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defVal) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(key, defVal);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defVal) {
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, defVal);
    }

    public void setMap(String key, Map<String, Object> value){
        JSONObject jsonObject = new JSONObject(value);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor editor = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(key, jsonString);
        editor.apply();
    }

    public Map<String, Object> getMap(String key){
        Map<String, Object> outputMap = new HashMap<>();
        SharedPreferences prefs = activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String map = prefs.getString(key, (new JSONObject()).toString());
        JSONObject jsonObject = null;
        try {
            assert map != null;
            jsonObject = new JSONObject(map);
            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String mKey = keysItr.next();
                outputMap.put(mKey, jsonObject.get(mKey));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return outputMap;
    }

}
