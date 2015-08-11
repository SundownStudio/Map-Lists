package com.sundown.maplists.models;

import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.interfaces.PropertiesHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.FIELD_ENTRIES;
import static com.sundown.maplists.storage.JsonConstants.FIELD_PERMANENT;
import static com.sundown.maplists.storage.JsonConstants.FIELD_TYPE;
import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * Created by Sundown on 7/14/2015.
 */
public abstract class Item implements PropertiesHandler {

    public String documentId;
    public int mapId;
    private Map<Integer, Field> fields;

    public Item(int mapId){
        this.mapId = mapId;
        fields = new HashMap<>();
    }

    private int getNextKey(){
        Object[] keys = getKeys();
        int size = keys.length;
        int lastKey = 0;
        if (size > 0) lastKey = (int) keys[size-1];
        return ++lastKey;
    }

    public Integer[] getKeys(){
        Object[] keys = fields.keySet().toArray();
        Arrays.sort(keys);
        return Arrays.copyOf(keys, keys.length, Integer[].class);
    }

    public Collection<Field> getValues(){return fields.values();}

    public void removeField(int id){fields.remove(id);}

    public Field getField(Integer id){ return fields.get(id);}

    public int addField(Field field){
        int nextKey = getNextKey();
        fields.put(nextKey, field);
        return nextKey;
    }

    public ArrayList<PhotoField> getPhotos(){
        ArrayList<PhotoField> photoFields = new ArrayList<>();
        Object[] keys = getKeys();

        for (Object key: keys){
            Field field = fields.get(key);
            if (field.type == Constants.FIELDS.FIELD_PIC){
                photoFields.add((PhotoField) field);
            }
        }

        return photoFields;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap();
        properties.put(MAP_ID, mapId);

        List<Map<String, Object>> list = new ArrayList();
        Object[] keys = getKeys();

        int num = keys.length;
        for (int i = 0; i < num; ++i){
            list.add(fields.get(keys[i]).getProperties());
        }

        properties.put(FIELD_ENTRIES, list);
        return properties;
    }

    @Override
    public Item setProperties(Map properties) {
        fields.clear();

        documentId = String.valueOf(properties.get(DOCUMENT_ID));
        mapId = (Integer) properties.get(MAP_ID);

        List<Map<String, Object>> list = (List<Map<String, Object>>) properties.get(FIELD_ENTRIES);

        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Map<String, Object> props = list.get(i);
                int type = (Integer) props.get(FIELD_TYPE);
                boolean permanent = Boolean.parseBoolean(String.valueOf(properties.get(FIELD_PERMANENT)));

                if (type == Constants.FIELDS.FIELD_PIC){
                    fields.put(i, new PhotoField(permanent).setProperties(props));
                } else {
                    fields.put(i, new EntryField(permanent).setProperties(props));
                }
            }
        }

        return this;
    }

}

/*
    public List<Field> getSchema(){
        return (List<Field>)(List<?>) fields;
    }
*/