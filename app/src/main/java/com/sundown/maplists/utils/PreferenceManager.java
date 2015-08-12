package com.sundown.maplists.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sundown.maplists.MapListsApp;

/**
 * Created by Sundown on 4/7/2015.
 */
public final class PreferenceManager {

    public static final String PREF_FILE_NAME="prefs";

    private static PreferenceManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private PreferenceManager(Context context){
        prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }



    public static synchronized PreferenceManager getInstance() {
        if (instance == null) {
            instance = new PreferenceManager(MapListsApp.getContext());
        }
        return instance;
    }


    public void putString(final String key, final String value){editor.putString(key, value);}


    public String getString(final String key){return prefs.getString(key, "");}

    public void putFloat(final String key, final float value){editor.putFloat(key, value);}

    public float getFloat(final String key, final float defaultValue) { return prefs.getFloat(key, defaultValue);}

    public void putBoolean(final String key, final boolean value){editor.putBoolean(key, value);}

    public boolean getBoolean(final String key){
        return prefs.getBoolean(key, false);
    }

    public void putDouble(final String key, final Double value){ editor.putLong(key, Double.doubleToRawLongBits(value));}

    public double getDouble(final String key){return Double.longBitsToDouble(prefs.getLong(key, 0));}

    public void putInt(final String key, final int value){editor.putInt(key, value);}

    public int getInt(final String key){return prefs.getInt(key, 0);}


    public boolean containsKey(final String key){
        return prefs.contains(key);
    }

    public void remove(final String key){
        editor.remove(key);
    }


    public void apply(){
        editor.apply(); //commit() writes its preferences to storage synchronously
        //however apply() starts an asynchronous commit to disk and you wont be notified
        //of any failures. however its a bit faster than commit();
    }

    public void commit(){ //this will block until it is done i believe
        editor.commit();
    }

}
