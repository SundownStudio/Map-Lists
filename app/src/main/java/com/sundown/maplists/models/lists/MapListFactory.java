package com.sundown.maplists.models.lists;

import android.content.res.Resources;
import android.graphics.Color;

import com.sundown.maplists.Constants;
import com.sundown.maplists.R;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.FieldFactory;

/**
 * Created by Sundown on 9/8/2015.
 */
public class MapListFactory {

    public static MapList createList(Resources resources, int listType, int mapId){
        MapList list = null;

        switch(listType){
            case BaseList.PRIMARY:
                list = new PrimaryList(mapId);
                setListDefaults(resources, list);
                list.addField(FieldFactory.createField(resources.getString(R.string.name), resources.getString(R.string.new_location), Field.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.snippet), resources.getString(R.string.empty), Field.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.photo), "", Field.PHOTO, true));
                break;

            case BaseList.SECONDARY:
                list = new SecondaryList(mapId, Constants.LIST_ID_DEFAULT);
                setListDefaults(resources, list);
                list.addField(FieldFactory.createField(resources.getString(R.string.subject), "", Field.SUBJECT, true));
                break;
        }
        return list;
    }

    private static void setListDefaults(Resources resources, MapList list){
        list.setColor(Color.parseColor(Constants.MARKER_COLOR_DEFAULT));
        list.setSchemaId(Constants.SCHEMA_ID_DEFAULT);
        list.setSchemaName(resources.getString(R.string.default_schema_name));
    }
}
