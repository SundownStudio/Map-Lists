package com.sundown.maplists.models;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_SCHEMA_LIST;

/**
 * Created by Sundown on 8/25/2015.
 */
public class Schema extends List implements PropertiesHandler {

    public int schemaId;
    public String schemaName;

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(TYPE, TYPE_SCHEMA_LIST);
        properties.put(SCHEMA_ID, schemaId);
        properties.put(SCHEMA_NAME, schemaName);
        return properties;
    }

    @Override
    public Schema setProperties(Map properties) {
        super.setProperties(properties);
        schemaId = (Integer) properties.get(SCHEMA_ID);
        schemaName = String.valueOf(properties.get(SCHEMA_NAME));
        return this;
    }

    public java.util.List<Field> getSchema(){ return (java.util.List<Field>) getValues(); }

    public boolean sameSchema(java.util.List<Field> schema){
        boolean sameSchema = false;
        if (schema.size() == fields.size()){
            //todo
        }


        return sameSchema;
    }
}
