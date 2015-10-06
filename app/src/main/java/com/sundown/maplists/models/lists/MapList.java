package com.sundown.maplists.models.lists;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * These are drawable lists associated with a particular map id
 */
public abstract class MapList extends SchemaList {

    private int mapId;

    public int getMapId() {
        return mapId;
    }


    protected MapList(int mapId, ListType listType, int color) {
        super(listType, color);
        this.mapId = mapId;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(MAP_ID, mapId);
        return properties;
    }

    @Override
    public MapList setProperties(Map properties) {
        super.setProperties(properties);
        mapId = (Integer) properties.get(MAP_ID);
        return this;
    }

}