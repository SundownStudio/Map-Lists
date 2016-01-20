package com.sundown.maplists.models.lists;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * In addition to primary lists, each location can also have a set of secondary lists, these are created in list-mode
 */
public class SecondaryList extends MapList {


    private int listId;
    public int getListId(){ return listId;}
    public void setListId(int listId){ this.listId = listId; }


    protected SecondaryList(int mapId, int listId) {
        super(mapId);
        setListId(listId);
        setListType(SECONDARY);
    }

    @Override
    public SchemaList copySchema() {
        SchemaList schemaList = super.copy();
        schemaList.setListType(SECONDARY_SCHEMA);
        return schemaList;
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
        setListId((Integer) properties.get(LIST_ID));
        return this;
    }
}
