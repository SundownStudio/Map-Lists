package com.sundown.maplists.models;

import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;
/**
 * Created by Sundown on 6/16/2015.
 */
public class EntryField extends Field {


    public String entry;


    protected EntryField(String title, String entry, FieldType type, boolean permanent){
        super(title, type, permanent);
        this.entry = entry;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.FIELD_ENTRY, entry);
        return properties;
    }

    @Override
    public EntryField setProperties(Map properties) {
        super.setProperties(properties);
        entry = String.valueOf(properties.get(JsonConstants.FIELD_ENTRY));
        return this;
    }

}
