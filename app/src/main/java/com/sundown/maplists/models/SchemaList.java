package com.sundown.maplists.models;

import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.LIST_TYPE;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;

/**
 * Created by Sundown on 8/25/2015.
 */
public class SchemaList extends AbstractList implements PropertiesHandler {

    private int schemaId;

    private ListType listType;

    private String schemaName;

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }


    public SchemaList(ListType listType, int color) {
        super();
        this.listType = listType;
        this.color = color;
    }

    public String getTitlesString(StringBuffer buffer) {
        List<Field> fields = getFields();

        for (Field field : fields) {
            buffer.append(field.getTitle());
            buffer.append("\n");
        }
        buffer.deleteCharAt(buffer.lastIndexOf("\n"));
        return buffer.toString();
    }

    public String getFieldTypesString(StringBuffer buffer) {
        List<Field> fields = getFields();

        for (Field field : fields) {
            buffer.append(field.getType());
            buffer.append("\n");
        }
        buffer.deleteCharAt(buffer.lastIndexOf("\n"));
        return buffer.toString();
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(SCHEMA_ID, schemaId);
        properties.put(LIST_TYPE, listType.name());
        properties.put(SCHEMA_NAME, schemaName);
        properties.put(COLOR, color);
        return properties;
    }

    @Override
    public SchemaList setProperties(Map properties) {
        super.setProperties(properties);
        schemaId = (Integer) properties.get(SCHEMA_ID);
        listType = ListType.valueOf(properties.get(LIST_TYPE).toString());
        schemaName = String.valueOf(properties.get(SCHEMA_NAME));
        color = (Integer) properties.get(COLOR);
        return this;
    }


    @Override //note does not care about hashcode, used for seeing if schema changed
    public boolean equals(Object o) {
        if (o instanceof SchemaList) {
            SchemaList a = (SchemaList) o;
            List<Field> listA = a.getFields();
            List<Field> listB = this.getFields();
            return listA.equals(listB);
        }
        return false;
    }

}
