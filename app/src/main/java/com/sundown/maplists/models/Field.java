package com.sundown.maplists.models;

import com.sundown.maplists.interfaces.PropertiesHandler;
import com.sundown.maplists.storage.JsonConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 6/22/2015.
 */
public abstract class Field implements PropertiesHandler {

    public interface Observer{
        void updateTitle(String title);
    }

    public int id;
    public void setId(int id){ this.id = id;}
    public Observer observer;
    public void setObserver(Observer observer){this.observer = observer;}

    public String title;
    public void setTitle(String title){
        this.title = title;
        observer.updateTitle(title);
    }
    public int type;
    public boolean permanent;

    public Field(int id, String title, int type, boolean permanent){
        this.id = id;
        this.title = title;
        this.type = type;
        this.permanent = permanent;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap();
        properties.put(JsonConstants.FIELD_TITLE, title);
        properties.put(JsonConstants.FIELD_TYPE, type);
        properties.put(JsonConstants.FIELD_PERMANENT, String.valueOf(permanent));
        return properties;
    }

    @Override
    public Field setProperties(Map properties) {
        title = String.valueOf(properties.get(JsonConstants.FIELD_TITLE));
        type = (Integer) properties.get(JsonConstants.FIELD_TYPE);
        permanent = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_PERMANENT)));
        return this;
    }
}
