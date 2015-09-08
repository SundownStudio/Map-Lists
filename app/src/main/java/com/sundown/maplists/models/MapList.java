package com.sundown.maplists.models;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PhotoUtils;
import com.sundown.maplists.utils.PreferenceManager;

import java.util.Map;

/**
 * Created by Sundown on 4/13/2015.
 */
public class MapList extends LocationList {

    private boolean multipleListsEnabled;

    public void setMultipleListsEnabled(boolean enabled) {
        this.multipleListsEnabled = enabled;
    }

    public boolean isMultipleListsEnabled() {
        return multipleListsEnabled;
    }

    private LatLng latLng;

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    private float color;

    public float getColor() {
        return color;
    }

    public MapList() {
        super(-1);
        super.addField(new EntryField(-1, "Name", "New Location", FieldType.TEXT, true));
        super.addField(new EntryField(-1, "Snippet", "Empty", FieldType.TEXT, true));
        super.addField(new PhotoField(-1, true, PhotoUtils.getInstance(), FileManager.getInstance(), PreferenceManager.getInstance()));
        multipleListsEnabled = false;
        color = 0.0F;
    }


    @Override
    public void setColor(String color) {
        float[] hue = new float[3];
        Color.colorToHSV(Color.parseColor(color), hue);
        this.color = hue[0];
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.TYPE, JsonConstants.TYPE_MAP_LIST);
        properties.put(JsonConstants.MAP_LATITUDE, latLng.latitude);
        properties.put(JsonConstants.MAP_LONGITUDE, latLng.longitude);
        properties.put(JsonConstants.MAP_MULTIPLE_LISTS_ENABLED, String.valueOf(multipleListsEnabled));
        properties.put(JsonConstants.COLOR, String.valueOf(color));
        return properties;
    }

    @Override
    public MapList setProperties(Map properties) {
        super.setProperties(properties);
        multipleListsEnabled = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.MAP_MULTIPLE_LISTS_ENABLED)));
        latLng = new LatLng((Double) properties.get(JsonConstants.MAP_LATITUDE), (Double) properties.get(JsonConstants.MAP_LONGITUDE));
        color = Float.parseFloat(String.valueOf(properties.get(JsonConstants.COLOR)));
        return this;
    }

}