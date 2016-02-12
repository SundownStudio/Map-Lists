package com.sundown.maplists.pojo;

import android.content.Intent;

/**
 * Created by Sundown on 7/1/2015.
 */
public class ActivityResult {

    public int requestCode;
    public int resultCode;
    public Intent data;

    public ActivityResult(int requestCode, int resultCode, Intent data){
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}
