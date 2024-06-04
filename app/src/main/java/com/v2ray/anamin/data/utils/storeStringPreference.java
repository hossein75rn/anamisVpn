package com.v2ray.anamin.data.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class storeStringPreference {
    Context context ;
    public storeStringPreference(Context context) {
        this.context = context;
    }
    public void save(String key,String sharedPreference){
        SharedPreferences preferences = context.getSharedPreferences("com.anamisvpn.ang", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, sharedPreference);
        editor.apply();
    }
    public void save(String key,int sharedPreference){
        SharedPreferences preferences = context.getSharedPreferences("com.anamisvpn.ang", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, sharedPreference);
        editor.apply();
    }
    public String read(String sharedPreference){
        SharedPreferences preferences = context.getSharedPreferences("com.anamisvpn.ang", Context.MODE_PRIVATE);
        return preferences.getString(sharedPreference,"");
    }

    public int readInt(String sharedPreference){
        SharedPreferences preferences = context.getSharedPreferences("com.anamisvpn.ang", Context.MODE_PRIVATE);
        return preferences.getInt(sharedPreference,401);
    }

}
