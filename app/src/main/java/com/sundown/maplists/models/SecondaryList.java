package com.sundown.maplists.models;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_LOCATION_LIST;

/**
 * Created by Sundown on 5/4/2015.
 */
public class SecondaryList extends LocationList {


    public int listId;

    public SecondaryList(int mapId){
        super(mapId);
        listId = -1;
        super.addField(new EntryField(listId, "Name", "", FieldType.FIELD_TEXT, true));
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(TYPE, TYPE_LOCATION_LIST);
        properties.put(LIST_ID, listId);
        return properties;
    }


    @Override
    public SecondaryList setProperties(Map properties) {
        super.setProperties(properties);
        listId = (Integer) properties.get(LIST_ID);
        return this;
    }
}