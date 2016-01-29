package com.sundown.maplists.models.lists;

import com.couchbase.lite.UnsavedRevision;
import com.sundown.maplists.Constants;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * In addition to primary lists, each location can also have a set of secondary lists, these are created in list-mode
 */
public class SecondaryList extends BaseList {


    private int listId;
    public int getListId(){ return listId;}
    public void setListId(int listId){ this.listId = listId; }


    protected SecondaryList(int mapId, int listId) {
        super(mapId);
        setListId(listId);
        getSchema().setType(Constants.TYPE_SECONDARY_LIST);
    }

    @Override
    public Map<String, Object> getProperties(Map<String, Object> properties, UnsavedRevision newRevision) {
        super.getProperties(properties, newRevision);
        if (getSchema().getType() == Constants.TYPE_SECONDARY_LIST)
            properties.put(LIST_ID, listId);
        return properties;
    }


    @Override
    public SecondaryList setProperties(Map properties) {
        super.setProperties(properties);
        if (getSchema().getType() == Constants.TYPE_SECONDARY_LIST)
            setListId((Integer) properties.get(LIST_ID));
        return this;
    }
}
