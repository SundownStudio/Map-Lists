package com.sundown.maplists.models;

import android.graphics.Color;

/**
 * Created by Sundown on 9/8/2015.
 */
public class ListFactory {




    public static SchemaList createList(ListType listType, int mapId){
        SchemaList list = null;

        switch(listType){
            case MAP:
                list = new MapList(mapId, Color.parseColor("#303F9F"));
                list.addField(FieldFactory.createField("Name", "New Location", FieldType.TEXT, true));
                list.addField(FieldFactory.createField("Snippet", "Empty", FieldType.TEXT, true));
                list.addField(FieldFactory.createField("Photo", "", FieldType.PHOTO, true));
                break;

            case SECONDARY:
                list = new SecondaryList(mapId, -1, Color.parseColor("#303F9F"));
                list.addField(FieldFactory.createField("Subject", "", FieldType.SUBJECT, true));
                break;
        }
        return list;
    }

}
