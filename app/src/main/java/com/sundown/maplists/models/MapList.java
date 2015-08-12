package com.sundown.maplists.models;

import com.google.android.gms.maps.model.LatLng;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;

/**
 * Created by Sundown on 4/13/2015.
 */
public class MapList extends List {

    public boolean multipleListsEnabled;
    public LatLng latLng;


    public MapList(){
        super(-1);
        super.addField(new EntryField(-1, "Name", "New Location", Constants.FIELDS.FIELD_TEXT, true));
        super.addField(new EntryField(-1, "Snippet", "Empty", Constants.FIELDS.FIELD_TEXT, true));
        super.addField(new PhotoField(-1, true));
        multipleListsEnabled = false;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.TYPE, JsonConstants.TYPE_MAP_LIST);
        properties.put(JsonConstants.MAP_LATITUDE, latLng.latitude);
        properties.put(JsonConstants.MAP_LONGITUDE, latLng.longitude);
        properties.put(JsonConstants.MAP_MULTIPLE_LISTS_ENABLED, String.valueOf(multipleListsEnabled));
        return properties;
    }

    @Override
    public MapList setProperties(Map properties) {
        super.setProperties(properties);
        multipleListsEnabled = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.MAP_MULTIPLE_LISTS_ENABLED)));
        latLng = new LatLng((Double) properties.get(JsonConstants.MAP_LATITUDE), (Double) properties.get(JsonConstants.MAP_LONGITUDE));
        return this;
    }

}