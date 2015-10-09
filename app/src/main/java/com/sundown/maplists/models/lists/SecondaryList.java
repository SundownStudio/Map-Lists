package com.sundown.maplists.models.lists;

import android.content.res.Resources;

import com.sundown.maplists.R;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * In addition to primary lists, each location can also have a set of secondary lists, these are created in list-mode
 */
public class SecondaryList extends MapList {


    private int listId;
    public int getListId(){ return listId;}
    public void setListId(int listId){ this.listId = listId; }


    protected SecondaryList(Resources resources, int mapId) {
        super(resources, mapId, ListType.SECONDARY);
        setListId(Integer.parseInt(resources.getString(R.string.default_list_id)));
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
