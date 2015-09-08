package com.sundown.maplists.models;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;

/**
 * Created by Sundown on 4/13/2015.
 */
public class MapList extends LocationList {


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

    protected MapList(int mapId, float color) {
        super(mapId);
        this.color = color;
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
        properties.put(JsonConstants.COLOR, String.valueOf(color));
        return properties;
    }

    @Override
    public MapList setProperties(Map properties) {
        super.setProperties(properties);
        latLng = new LatLng((Double) properties.get(JsonConstants.MAP_LATITUDE), (Double) properties.get(JsonConstants.MAP_LONGITUDE));
        color = Float.parseFloat(String.valueOf(properties.get(JsonConstants.COLOR)));
        return this;
    }

}