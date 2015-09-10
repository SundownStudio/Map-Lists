package com.sundown.maplists.models;

import com.sundown.maplists.storage.JsonConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sundown on 6/16/2015.
 */
public class EntryField extends Field {


    private List<String> entries;


    protected EntryField(String title, String entry, FieldType type, boolean permanent){
        super(title, type, permanent);
        entries = new ArrayList<>();
        entries.add(entry);
    }

    public int getNumEntries(){
        return entries.size();
    }

    public String getEntry(int element){
        return entries.get(element);
    }

    public void addEntry(String entry){
        entries.add(entry);
    }

    public void clearEntries(){ entries.clear(); }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        List list = new ArrayList();

        for (String entry: entries){
            Map<String, Object> props = new HashMap(1);
            props.put(JsonConstants.FIELD_ENTRY, entry);
            list.add(props);
        }

        properties.put(JsonConstants.FIELD_ENTRIES, list);
        return properties;
    }

    @Override
    public EntryField setProperties(Map properties) {
        super.setProperties(properties);
        entries.clear();
        List list = (List) properties.get(JsonConstants.FIELD_ENTRIES);

        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Map<String, Object> props = (Map<String, Object>) list.get(i);
                addEntry(String.valueOf(props.get(JsonConstants.FIELD_ENTRY)));
            }
        }
        return this;
    }

}
