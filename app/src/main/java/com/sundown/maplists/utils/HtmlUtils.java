package com.sundown.maplists.utils;

/**
 * Created by Sundown on 9/16/2015.
 */
public class HtmlUtils {

    public static String determineColorHtml(String entry){
        if (entry.length() > 0){
            if (entry.substring(0, 1).equals("+")){
                entry = "<font color='#14CD05'>" + entry + "</font>";
            } else if (entry.substring(0, 1).equals("-")){
                entry = "<font color='#CC0000'>" + entry + "</font>";
            }
        }
        return entry;
    }

    /** <ul> and <li> are not supported in android.. yay.. */
    public static String getListItemHtml(String entry){
        if (entry.length() > 0){
            entry = " - " + entry;
        }
        return entry;
    }

}
