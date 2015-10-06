package com.sundown.maplists.models.lists;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * In addition to primary lists, each location can also have a set of secondary lists, these are created in list-mode
 */
public class SecondaryList extends MapList {


    private int listId;


    protected SecondaryList(int mapId, int listId, int color) {
        super(mapId, ListType.SECONDARY, color);
        this.listId = listId;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
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
