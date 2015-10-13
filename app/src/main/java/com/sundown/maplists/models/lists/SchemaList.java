package com.sundown.maplists.models.lists;

import com.sundown.maplists.models.Copyable;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.PhotoField;

import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.LIST_TYPE;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;

/**
 * Created by Sundown on 8/25/2015.
 */
public class SchemaList extends AbstractList implements Copyable{

    private int schemaId;

    public int getSchemaId(){ return schemaId; }

    public void setSchemaId(int schemaId) {
        this.schemaId = schemaId;
    }

    private ListType listType;

    protected void setListType(ListType listType) {
        this.listType = listType;
    }

    private String schemaName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    protected SchemaList() {
        super();
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
    public SchemaList copy() {
        SchemaList schemaList = new SchemaList();
        schemaList.setColor(getColor());
        schemaList.setSchemaId(getSchemaId());
        schemaList.setSchemaName(getSchemaName());
        schemaList.setDocumentId(getDocumentId());
        List<Field> fields = getFields();
        for (Field field : fields) {
            if (field instanceof EntryField) {
                schemaList.addField((EntryField) field.copy());

            } else if (field instanceof PhotoField) {
                schemaList.addField((PhotoField) field.copy());
            }
        }
        return schemaList;
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
        setSchemaId((Integer) properties.get(SCHEMA_ID));
        setListType(ListType.valueOf(properties.get(LIST_TYPE).toString()));
        setSchemaName(String.valueOf(properties.get(SCHEMA_NAME)));
        setColor((Integer) properties.get(COLOR));
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
