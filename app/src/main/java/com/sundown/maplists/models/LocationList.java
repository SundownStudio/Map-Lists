package com.sundown.maplists.models;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * Created by Sundown on 8/25/2015.
 */
public abstract class LocationList extends AbstractList implements PropertiesHandler {


    private int mapId;

    public int getMapId() {
        return mapId;
    }

    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    protected LocationList(int mapId, int color) {
        super();
        this.mapId = mapId;
        this.color = color;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(MAP_ID, mapId);
        properties.put(COLOR, color);
        return properties;
    }

    @Override
    public LocationList setProperties(Map properties) {
        super.setProperties(properties);
        mapId = (Integer) properties.get(MAP_ID);
        color = (Integer) properties.get(COLOR);
        return this;
    }

}
