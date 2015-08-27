package com.sundown.maplists.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_SCHEMA_LIST;

/**
 * Created by Sundown on 8/25/2015.
 */
public class SchemaList extends List implements PropertiesHandler {

    public int schemaId;
    private String schemaName;
    public void setSchemaName(String schemaName){ this.schemaName = schemaName;}
    public String getSchemaName(){return schemaName;}

    public SchemaList(){}

    public SchemaList(LocationList locationList){
        super();
        Integer[] keys = locationList.getKeys();
        for (Integer key : keys) {
            SchemaField field = new SchemaField(locationList.getField(key));
            this.putField(key, field);
        }
    }

    public String getTitles(StringBuffer buffer){            //todo: condense this crap
        ArrayList<Field> fields = getValues();
        for (Field field: fields){
            buffer.append(field.getTitle());
            buffer.append("\n");
        }
        buffer.deleteCharAt(buffer.lastIndexOf("\n"));
        return buffer.toString();
    }

    public String getTypes(StringBuffer buffer){
        ArrayList<Field> fields = getValues();
        for (Field field: fields){
            buffer.append(field.getType());
            buffer.append("\n");
        }
        buffer.deleteCharAt(buffer.lastIndexOf("\n"));
        return buffer.toString();
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(TYPE, TYPE_SCHEMA_LIST);
        properties.put(SCHEMA_ID, schemaId);
        properties.put(SCHEMA_NAME, schemaName);
        return properties;
    }

    @Override
    public SchemaList setProperties(Map properties) {
        super.setProperties(properties);
        schemaId = (Integer) properties.get(SCHEMA_ID);
        schemaName = String.valueOf(properties.get(SCHEMA_NAME));
        return this;
    }


    @Override //note does not care about hashcode
    public boolean equals(Object o) {
        if (o instanceof SchemaList){
            SchemaList a = (SchemaList)o;
            Integer[] aKeys = a.getKeys();
            Integer[] bKeys = getKeys(); //they come ordered

            if (Arrays.equals(aKeys, bKeys)){
                ArrayList<Field> aFields = a.getValues();
                if (getValues().equals(aFields)){
                    return true;
                }
            }
        }
        return false;
    }

}
