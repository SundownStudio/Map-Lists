package com.sundown.maplists.models;

import android.graphics.Color;

/**
 * Created by Sundown on 9/8/2015.
 */
public class LocationListFactory {

    public static final int MAPLIST = 10;
    public static final int SECONDARYLIST = 20;


    public static LocationList createLocationList(int type, int mapId){
        LocationList locationList = null;

        switch(type){
            case MAPLIST:
                locationList = new MapList(mapId, Color.parseColor("#303F9F"));
                locationList.addField(FieldFactory.createField("Name", "New Location", FieldType.TEXT, true));
                locationList.addField(FieldFactory.createField("Snippet", "Empty", FieldType.TEXT, true));
                locationList.addField(FieldFactory.createField("Photo", "", FieldType.PHOTO, true));
                break;

            case SECONDARYLIST:
                locationList = new SecondaryList(mapId, -1, Color.parseColor("#303F9F"));
                locationList.addField(FieldFactory.createField("Subject", "", FieldType.SUBJECT, true));
                break;
        }
        return locationList;
    }

}
