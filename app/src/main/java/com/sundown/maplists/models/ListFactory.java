package com.sundown.maplists.models;

import android.content.res.Resources;
import android.graphics.Color;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 9/8/2015.
 */
public class ListFactory {

    public static SchemaList createList(Resources resources, ListType listType, int mapId){
        SchemaList list = null;

        switch(listType){
            case MAP:
                list = new MapList(mapId, Color.parseColor(resources.getString(R.string.default_marker_color)));
                list.addField(FieldFactory.createField(resources.getString(R.string.name), resources.getString(R.string.new_location), FieldType.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.snippet), resources.getString(R.string.empty), FieldType.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.photo), "", FieldType.PHOTO, true));
                break;

            case SECONDARY:
                list = new SecondaryList(mapId, -1, Color.parseColor(resources.getString(R.string.default_marker_color)));
                list.addField(FieldFactory.createField(resources.getString(R.string.subject), "", FieldType.SUBJECT, true));
                break;
        }
        return list;
    }

}
