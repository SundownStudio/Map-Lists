package com.sundown.maplists.models;

import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_SCHEMA_LIST;

/**
 * Created by Sundown on 8/25/2015.
 */
public class SchemaList extends AbstractList implements PropertiesHandler {

    public int schemaId;
    private String schemaName;
    public void setSchemaName(String schemaName){ this.schemaName = schemaName;}
    public String getSchemaName(){return schemaName;}

    public SchemaList(){}

    public SchemaList(LocationList locationList){
        super();
        for (Field field : locationList.fields) {
            SchemaField schemaField = new SchemaField(field);
            this.fields.add(schemaField);
        }
    }

    public String getTitles(StringBuffer buffer){            //todo: condense this crap
        for (Field field: fields){
            buffer.append(field.getTitle());
            buffer.append("\n");
        }
        buffer.deleteCharAt(buffer.lastIndexOf("\n"));
        return buffer.toString();
    }

    public String getTypes(StringBuffer buffer){
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
            List<Field> listA = a.fields;
            List<Field> listB = this.fields;
            return listA.equals(listB);
        }
        return false;
    }

}
