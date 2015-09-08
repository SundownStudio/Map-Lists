package com.sundown.maplists.models;

import android.graphics.Color;

import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PhotoUtils;
import com.sundown.maplists.utils.PreferenceManager;

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
                locationList = new MapList(mapId, 0.0F);
                locationList.addField(new EntryField(mapId, "Name", "New Location", FieldType.TEXT, true));
                locationList.addField(new EntryField(mapId, "Snippet", "Empty", FieldType.TEXT, true));
                locationList.addField(new PhotoField(mapId, true, PhotoUtils.getInstance(), FileManager.getInstance(), PreferenceManager.getInstance()));
                break;

            case SECONDARYLIST:
                locationList = new SecondaryList(mapId, -1, Color.parseColor("#303F9F"));
                locationList.addField(new EntryField(mapId, "Subject", "", FieldType.SUBJECT, true));
                break;
        }
        return locationList;
    }

}
