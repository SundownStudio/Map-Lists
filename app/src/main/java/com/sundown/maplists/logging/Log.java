package com.sundown.maplists.logging;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Sundown on 4/13/2015.
 */
public class Log {

    private static Toast toast;
    public static final int TOAST_SHORT = Toast.LENGTH_SHORT;
    public static final int TOAST_LONG = Toast.LENGTH_LONG;

    public static void m(String message){ android.util.Log.d("debug", "" + message); }

    public static void m(String TAG, String message){
        android.util.Log.d(TAG, "" + message);
    }

    public static void e(Exception e){
        android.util.Log.e("error", "ERROR", e);
    }

    public static void e(String message, Exception e){
        android.util.Log.e("error", message, e);
    }

    public static void e(String tag, String message, Exception e){ android.util.Log.e(tag, message, e);}

    public static void Toast(Context context, String message, int duration){
        if(toast!=null)
            toast.cancel();
        toast = Toast.makeText(context, message + "", duration);
        toast.show();

    }



}
