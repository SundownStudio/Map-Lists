package com.sundown.maplists.models;

import android.graphics.Color;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.LIST_ID;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_LOCATION_LIST;

/**
 * Created by Sundown on 5/4/2015.
 */
public class SecondaryList extends LocationList {


    private int listId;
    private int color;

    public int getColor() {
        return color;
    }


    public SecondaryList(int mapId) {
        super(mapId);
        listId = -1;
        color = Color.parseColor("#303F9F");
        super.addField(new EntryField(listId, "Subject", "", FieldType.SUBJECT, true));
    }

    @Override
    public void setColor(String color) {
        this.color = Color.parseColor(color);
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(TYPE, TYPE_LOCATION_LIST);
        properties.put(LIST_ID, listId);
        properties.put(COLOR, color);
        return properties;
    }


    @Override
    public SecondaryList setProperties(Map properties) {
        super.setProperties(properties);
        listId = (Integer) properties.get(LIST_ID);
        color = (Integer) properties.get(COLOR);
        return this;
    }
}
