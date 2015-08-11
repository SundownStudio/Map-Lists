package com.sundown.maplists;

import android.app.Application;
import android.content.Context;

import com.sundown.maplists.logging.Log;


/**
 * Created by Sundown on 5/20/2015.
 */
public class MapLists extends Application {


    private static MapLists instance;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.m("InventoryApp.onCreate was called");
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

}
