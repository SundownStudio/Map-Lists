package com.sundown.maplists.models.fields;

import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PhotoUtils;
import com.sundown.maplists.utils.PreferenceManager;

/**
 * Created by Sundown on 9/8/2015.
 */
public class FieldFactory {

    public static Field createField(String title, String entry, int type, boolean permanent){
        Field field;
        switch (type){
            case Field.PHOTO:
                field = new PhotoField(title, permanent, PhotoUtils.getInstance(), FileManager.getInstance(), PreferenceManager.getInstance());
                break;

            case Field.DATE_TIME:
                field = new EntryField(title, entry, type, permanent).addEntry("");
                break;

            default:
                field = new EntryField(title, entry, type, permanent);
                break;
        }
        return field;
    }
}
