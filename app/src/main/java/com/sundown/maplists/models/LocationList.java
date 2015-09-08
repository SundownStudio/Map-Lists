package com.sundown.maplists.models;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * Created by Sundown on 8/25/2015.
 */
public abstract class LocationList extends AbstractList implements PropertiesHandler {


    private int mapId;
    public int getMapId(){ return mapId; }

    abstract public void setColor(String color);


    public LocationList(int mapId){
        super();
        this.mapId = mapId;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(MAP_ID, mapId);
        return properties;
    }

    @Override
    public LocationList setProperties(Map properties) {
        super.setProperties(properties);
        mapId = (Integer) properties.get(MAP_ID);
        return this;
    }

}
