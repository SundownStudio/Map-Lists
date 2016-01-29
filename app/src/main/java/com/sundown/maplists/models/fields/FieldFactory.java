package com.sundown.maplists.models.fields;

import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PhotoUtils;
import com.sundown.maplists.utils.PreferenceManager;

/**
 * Created by Sundown on 9/8/2015.
 */
public class FieldFactory {

    public static Field createField(String title, String entry, int type, boolean permanent){

        switch (type){
            case Field.PHOTO:
                return new PhotoField(title, permanent, PhotoUtils.getInstance(), FileManager.getInstance(), PreferenceManager.getInstance());

            case Field.DATE_TIME:
                return new EntryField(title, entry, type, permanent).addEntry("");

            default:
                return new EntryField(title, entry, type, permanent);

        }
    }
}
