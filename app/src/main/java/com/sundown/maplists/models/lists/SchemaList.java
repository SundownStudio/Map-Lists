package com.sundown.maplists.models.lists;

import android.content.res.Resources;
import android.graphics.Color;

import com.sundown.maplists.R;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;

import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.COLOR;
import static com.sundown.maplists.storage.JsonConstants.LIST_TYPE;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_ID;
import static com.sundown.maplists.storage.JsonConstants.SCHEMA_NAME;

/**
 * Created by Sundown on 8/25/2015.
 */
public class SchemaList extends AbstractList {

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


    public SchemaList() {
        super();
    }


    public SchemaList(Resources resources, SchemaList list) {
        super(list);

        if (list instanceof PrimaryList) {
            setListType(ListType.PRIMARY_SCHEMA);
            ((EntryField) getField(0)).setEntry(0, resources.getString(R.string.new_location));
            ((EntryField) getField(1)).setEntry(0, resources.getString(R.string.empty));

        } else if (list instanceof SecondaryList) {
            setListType(ListType.SECONDARY_SCHEMA);
            getField(0).setTitle(resources.getString(R.string.subject));
        }
        setColor(list.getColor());
        setSchemaId(list.getSchemaId());
        setSchemaName(list.getSchemaName());
    }

    public SchemaList(Resources resources, ListType listType) {
        super();
        setListType(listType);
        setColor(Color.parseColor(resources.getString(R.string.default_marker_color)));
        setSchemaId(Integer.parseInt(resources.getString(R.string.default_schema_id)));
        setSchemaName(resources.getString(R.string.default_schema_name));
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
