package com.sundown.maplists.models.lists;

import android.content.res.Resources;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * These are drawable lists associated with a particular map id
 */
public abstract class MapList extends SchemaList {

    private int mapId;

    private void setMapId(int mapId) { this.mapId = mapId; }

    public int getMapId() {
        return mapId;
    }


    protected MapList(Resources resources, int mapId, ListType listType) {
        super(resources, listType);
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
        setMapId((Integer) properties.get(MAP_ID));
        return this;
    }

    public MapList setSchemaProperties(Map properties, ListType listType) {
        super.setProperties(properties);
        setListType(listType);
        return this;
    }
}