package com.sundown.maplists.utils;

import android.graphics.Color;

/**
 * Created by Sundown on 9/8/2015.
 */
public class ColorUtils {

    private static float[] hue = new float[3];

    public static float getColorHue(int color){
        Color.colorToHSV(color, hue);
        return hue[0];
    }
}
