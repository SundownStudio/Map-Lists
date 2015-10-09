package com.sundown.maplists.models.lists;

import android.content.res.Resources;

import com.sundown.maplists.R;
import com.sundown.maplists.models.fields.FieldFactory;
import com.sundown.maplists.models.fields.FieldType;

/**
 * Created by Sundown on 9/8/2015.
 */
public class MapListFactory {

    public static MapList createList(Resources resources, ListType listType, int mapId){
        MapList list = null;

        switch(listType){
            case PRIMARY:
                list = new PrimaryList(resources, mapId);
                list.addField(FieldFactory.createField(resources.getString(R.string.name), resources.getString(R.string.new_location), FieldType.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.snippet), resources.getString(R.string.empty), FieldType.TEXT, true));
                list.addField(FieldFactory.createField(resources.getString(R.string.photo), "", FieldType.PHOTO, true));
                break;

            case SECONDARY:
                list = new SecondaryList(resources, mapId);
                list.addField(FieldFactory.createField(resources.getString(R.string.subject), "", FieldType.SUBJECT, true));
                break;
        }
        return list;
    }

    public static SchemaList createSchemaList(Resources resources, MapList list){
        return new SchemaList(resources, list);
    }

}
