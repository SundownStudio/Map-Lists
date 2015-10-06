package com.sundown.maplists.models.lists;

import com.sundown.maplists.models.PropertiesHandler;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.FieldFactory;
import com.sundown.maplists.models.fields.FieldType;
import com.sundown.maplists.models.fields.PhotoField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.FIELDS;
import static com.sundown.maplists.storage.JsonConstants.FIELD_PERMANENT;
import static com.sundown.maplists.storage.JsonConstants.FIELD_TYPE;

/**
 * Created by Sundown on 7/14/2015.
 */
public abstract class AbstractList implements PropertiesHandler {

    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    private List<Field> fields;

    public List<Field>getFields() { return fields; }

    protected AbstractList() {
        fields = new ArrayList<>();
    }


    public void removeField(int id) {
        fields.remove(id);
    }

    public Field getField(Integer id) {
        return fields.get(id);
    }

    public int addField(Field field) {
        fields.add(field);
        return fields.size();
    }

    public ArrayList<PhotoField> getPhotos() {
        ArrayList<PhotoField> photoFields = new ArrayList<>();

        for (Field field : fields) {
            if (field.getType() == FieldType.PHOTO) {
                photoFields.add((PhotoField) field);
            }
        }
        return photoFields;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap();
        List list = new ArrayList();

        for (Field field : fields) {
            list.add(field.getProperties());
        }

        properties.put(FIELDS, list);
        return properties;
    }

    @Override
    public AbstractList setProperties(Map properties) {
        fields.clear();
        documentId = String.valueOf(properties.get(DOCUMENT_ID));
        List list = (List) properties.get(FIELDS);

        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Map<String, Object> props = (Map<String, Object>) list.get(i);
                FieldType type = FieldType.valueOf(props.get(FIELD_TYPE).toString());
                boolean permanent = Boolean.parseBoolean(String.valueOf(props.get(FIELD_PERMANENT)));
                fields.add(FieldFactory.createField("", "", type, permanent).setProperties(props));
            }
        }
        return this;
    }

}
