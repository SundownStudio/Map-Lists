package com.sundown.maplists.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.FIELD_ENTRIES;
import static com.sundown.maplists.storage.JsonConstants.FIELD_PERMANENT;
import static com.sundown.maplists.storage.JsonConstants.FIELD_TYPE;

/**
 * Created by Sundown on 7/14/2015.
 */
public abstract class List implements PropertiesHandler {

    public String documentId;
    public Map<Integer, Field> fields;


    public List(){
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

    public ArrayList<Field> getValues(){return new ArrayList(fields.values());}

    public void removeField(int id){fields.remove(id);}

    public Field getField(Integer id){ return fields.get(id);}

    public int addField(Field field){
        int nextKey = getNextKey();
        fields.put(nextKey, field);
        return nextKey;
    }

    public void putField(Integer key, Field value){
        fields.put(key, value);
    }

    public ArrayList<PhotoField> getPhotos(){
        ArrayList<PhotoField> photoFields = new ArrayList<>();
        Integer[] keys = getKeys();

        for (Integer key: keys){
            Field field = fields.get(key);
            if (field.type == FieldType.PHOTO){
                photoFields.add((PhotoField) field);
            }
        }

        return photoFields;
    }

    public java.util.List<Field> getFieldValues(){ return (java.util.List<Field>) getValues(); }



    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap();
        java.util.List list = new ArrayList();
        Object[] keys = getKeys();

        int num = keys.length;
        for (int i = 0; i < num; ++i){
            list.add(fields.get(keys[i]).getProperties());
        }

        properties.put(FIELD_ENTRIES, list);
        return properties;
    }

    @Override
    public List setProperties(Map properties) {
        fields.clear();

        documentId = String.valueOf(properties.get(DOCUMENT_ID));

        java.util.List entries = (java.util.List) properties.get(FIELD_ENTRIES);

        if (entries != null) {
            int size = entries.size();
            for (int i = 0; i < size; ++i) {
                Map<String, Object> props = (Map<String, Object>) entries.get(i);
                FieldType type = FieldType.valueOf(props.get(FIELD_TYPE).toString());
                boolean permanent = Boolean.parseBoolean(String.valueOf(props.get(FIELD_PERMANENT)));

                if (type == FieldType.PHOTO){
                    fields.put(i, new PhotoField(permanent).setProperties(props));
                } else {
                    fields.put(i, new EntryField(permanent).setProperties(props));
                }
            }
        }

        return this;
    }

}
