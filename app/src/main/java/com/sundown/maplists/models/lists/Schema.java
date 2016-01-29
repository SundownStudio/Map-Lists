package com.sundown.maplists.models.lists;

import com.couchbase.lite.UnsavedRevision;
import com.sundown.maplists.models.PropertiesHandler;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.FieldFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.FIELDS;
import static com.sundown.maplists.storage.JsonConstants.FIELD_PERMANENT;
import static com.sundown.maplists.storage.JsonConstants.FIELD_TYPE;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
/**
 * Created by Sundown on 1/28/2016.
 */
public class Schema implements PropertiesHandler<Schema>{

    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    protected void setDocumentId(String documentId) { this.documentId = documentId; }

    private int schemaId;

    public int getSchemaId(){ return schemaId; }

    public void setSchemaId(int schemaId) {
        this.schemaId = schemaId;
    }

    private int type;

    public void setType(int type) {
        this.type = type;
    }

    public int getType(){return type;}

    private List<Field> fields = new ArrayList<>();

    public List<Field> getFields() { return fields; }

    public Field getField(int id) { return fields.get(id); }

    public void removeField(int id) { fields.remove(id); }

    public int addField(Field field) {
        fields.add(field);
        return fields.size();
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

    public Schema(){}

    public Schema(Schema schema){
        setColor(schema.getColor());
        setSchemaId(schema.getSchemaId());
        setSchemaName(schema.getSchemaName());

        List<Field> fields = schema.getFields();
        for (Field field : fields) {
            addField(field.copy());
        }
    }

    //this is where we distinguish between different schemas..
    public boolean hasSameAttributes(Object o) {
        if (o instanceof Schema) {
            Schema a = (Schema) o;
            if (a.getColor() != getColor()) //all elements of a schema should have the same color
                return false;

            List<Field> listA = a.getFields();
            List<Field> listB = this.getFields();
            return listA.equals(listB);
        }
        return false;
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
    public Map<String, Object> getProperties(Map properties, UnsavedRevision newRevision) {
        properties.put(SCHEMA_ID, schemaId);
        properties.put(SCHEMA_NAME, schemaName);
        properties.put(TYPE, type);
        properties.put(COLOR, color);

        List list = new ArrayList();

        for (Field field : fields) {
            list.add(field.getProperties(new HashMap<String, Object>(), newRevision));
        }

        properties.put(FIELDS, list);

        return properties;
    }

    @Override
    public Schema setProperties(Map properties) {
        fields.clear();
        setDocumentId(String.valueOf(properties.get(DOCUMENT_ID)));
        setSchemaId((Integer) properties.get(SCHEMA_ID));
        setSchemaName(String.valueOf(properties.get(SCHEMA_NAME)));
        setType((Integer) properties.get(TYPE));
        setColor((Integer) properties.get(COLOR));
        List list = (List) properties.get(FIELDS);

        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Map<String, Object> props = (Map<String, Object>) list.get(i);
                int type = (Integer.parseInt(String.valueOf(props.get(FIELD_TYPE))));
                boolean permanent = Boolean.parseBoolean(String.valueOf(props.get(FIELD_PERMANENT)));
                fields.add(FieldFactory.createField("", "", type, permanent).setProperties(props));
            }
        }
        return this;
    }
}
