package com.sundown.maplists.models;

import com.sundown.maplists.extras.Constants;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.ITEM_ID;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_LOCATION_ITEM;

/**
 * Created by Sundown on 5/4/2015.
 */
public class LocationItem extends Item {


    public int itemId;

    public LocationItem(int mapId){
        super(mapId);
        itemId = -1;
        super.addField(new EntryField(itemId, "Name", "", Constants.FIELDS.FIELD_TEXT, true));
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(TYPE, TYPE_LOCATION_ITEM);
        properties.put(ITEM_ID, itemId);
        return properties;
    }


    @Override
    public LocationItem setProperties(Map properties) {
        super.setProperties(properties);
        itemId = (Integer) properties.get(ITEM_ID);
        return this;
    }
}
