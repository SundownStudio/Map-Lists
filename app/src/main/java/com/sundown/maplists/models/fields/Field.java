package com.sundown.maplists.models.fields;

import com.sundown.maplists.models.Copyable;
import com.sundown.maplists.models.PropertiesHandler;
import com.sundown.maplists.storage.JsonConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 6/22/2015.
 */
public abstract class Field implements PropertiesHandler, Copyable {

    public interface Observer {
        void setTitle(String title);
    }

    /**
     * used for view tagging
     */
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * indicates type of field
     */
    private FieldType type;

    public FieldType getType() {
        return type;
    }

    private void setFieldType(FieldType type) {
        this.type = type;
    }

    /**
     * indicates whether user can remove this field
     */
    private boolean permanent;

    public boolean isPermanent() {
        return permanent;
    }

    private void setIsPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    /**
     * indicates the title for this field
     */
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (observer != null)
            observer.setTitle(title);
    }

    /**
     * indicates whether or not this title is visible to user while in list-mode
     */
    private boolean showTitle;

    public void setShowTitle(boolean show) {
        this.showTitle = show;
    }

    public boolean isTitleShown() {
        return showTitle;
    }

    /**
     * the corresponding fieldview object from AddList
     */
    private Observer observer;

    public void setObserver(Observer observer) {
        this.observer = observer;
    }


    protected Field(String title, FieldType type, boolean permanent) {
        setTitle(title);
        setFieldType(type);
        setIsPermanent(permanent);
        setShowTitle(false);
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
        setTitle(String.valueOf(properties.get(JsonConstants.FIELD_TITLE)));
        setFieldType(FieldType.valueOf(properties.get(JsonConstants.FIELD_TYPE).toString()));
        setIsPermanent(Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_PERMANENT))));
        setShowTitle(Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_TITLE_SHOW))));
        return this;
    }

}
