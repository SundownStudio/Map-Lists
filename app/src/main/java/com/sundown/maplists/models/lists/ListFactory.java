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
public class ListFactory {

    public static BaseList createList(Resources resources, int listType, int mapId){
        BaseList list = null;

        switch(listType){
            case Constants.TYPE_PRIMARY_LIST: {
                list = new PrimaryList(mapId);
                Schema schema = list.getSchema();
                setListDefaults(resources, schema);
                schema.addField(FieldFactory.createField(resources.getString(R.string.name), resources.getString(R.string.new_location), Field.TEXT, true));
                schema.addField(FieldFactory.createField(resources.getString(R.string.snippet), resources.getString(R.string.empty), Field.TEXT, true));
                schema.addField(FieldFactory.createField(resources.getString(R.string.photo), "", Field.PHOTO, true));
                break;
            }

            case Constants.TYPE_SECONDARY_LIST: {
                list = new SecondaryList(mapId, Constants.LIST_ID_DEFAULT);
                Schema schema = list.getSchema();
                setListDefaults(resources, schema);
                schema.addField(FieldFactory.createField(resources.getString(R.string.subject), "", Field.SUBJECT, true));
                break;
            }
        }
        return list;
    }

    private static void setListDefaults(Resources resources, Schema schema){
        schema.setColor(Color.parseColor(Constants.MARKER_COLOR_DEFAULT));
        schema.setSchemaId(Constants.SCHEMA_ID_DEFAULT);
        schema.setSchemaName(resources.getString(R.string.default_schema_name));
    }
}
