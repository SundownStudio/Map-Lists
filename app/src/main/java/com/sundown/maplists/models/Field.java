package com.sundown.maplists.models;

import com.sundown.maplists.storage.JsonConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 6/22/2015.
 */
public abstract class Field implements PropertiesHandler {

    public interface Observer {
        void setTitle(String title);
    }

    /** used for view tagging */
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private FieldType type;

    public FieldType getType() {
        return type;
    }

    private boolean permanent;

    public boolean isPermanent() {
        return permanent;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        observer.setTitle(title);
    }

    private boolean showTitle;

    public void setShowTitle(boolean show) {
        this.showTitle = show;
    }

    public boolean isTitleShown(){
        return showTitle;
    }

    /** the corresponding fieldview object */
    private Observer observer;

    public void setObserver(Observer observer) {
        this.observer = observer;
    }


    protected Field(String title, FieldType type, boolean permanent) {
        this.title = title;
        this.type = type;
        this.permanent = permanent;
        this.showTitle = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Field) {
            Field a = (Field) o;
            if (a.type == this.type) {
                if (a.title != null && this.title != null && a.title.equals(this.title))
                    if (a.permanent == this.permanent)
                        return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap();
        properties.put(JsonConstants.FIELD_TITLE, title);
        properties.put(JsonConstants.FIELD_TYPE, type.name());
        properties.put(JsonConstants.FIELD_PERMANENT, String.valueOf(permanent));
        properties.put(JsonConstants.FIELD_TITLE_SHOW, String.valueOf(showTitle));
        return properties;
    }

    @Override
    public Field setProperties(Map properties) {
        title = String.valueOf(properties.get(JsonConstants.FIELD_TITLE));
        type = FieldType.valueOf(properties.get(JsonConstants.FIELD_TYPE).toString());
        permanent = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_PERMANENT)));
        showTitle = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_TITLE_SHOW)));
        return this;
    }

}
