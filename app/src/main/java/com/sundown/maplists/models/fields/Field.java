package com.sundown.maplists.models.fields;

import com.couchbase.lite.UnsavedRevision;
import com.sundown.maplists.models.PropertiesHandler;
import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;

/**
 * Created by Sundown on 6/22/2015.
 */
public abstract class Field implements PropertiesHandler<Field> {

    public interface Observer {
        void setTitle(String title);
    }

    //fieldtypes
    public static final int NAME = 0;
    public static final int PHONE = 1;
    public static final int EMAIL = 2;
    public static final int DATE = 3;
    public static final int TIME = 4;
    public static final int DATE_TIME = 5;
    public static final int URL = 6;
    public static final int PRICE = 7;
    public static final int PHOTO = 8;
    public static final int ITEM_LIST = 9;
    public static final int PRICE_LIST = 10;
    public static final int TEXT = 11;
    public static final int MESSAGE = 12;
    public static final int NUMBER = 13;
    public static final int DECIMAL = 14;
    public static final int DROPDOWN = 15;
    public static final int CHECKBOX = 16;
    public static final int RATING = 17;
    public static final int SUBJECT = 18;

    /** return an array of constants */
    public static int[] getConstArray(){
        return new int[]{NAME, PHONE, EMAIL, DATE, TIME, DATE_TIME, URL, PRICE, PHOTO, ITEM_LIST, PRICE_LIST, TEXT, MESSAGE, NUMBER, DECIMAL, DROPDOWN, CHECKBOX, RATING, SUBJECT};
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
    private int type;

    public int getType() {
        return type;
    }

    private void setType(int type) {
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


    protected Field(String title, int type, boolean permanent) {
        setTitle(title);
        setType(type);
        setIsPermanent(permanent);
        setShowTitle(false);
    }

    @Override //note that this is not a true equals().. im just doing this to minimize code for schema comparison.. if you ever need a true equals() then put this into a diff method..
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
    public Map<String, Object> getProperties(Map<String, Object> properties, UnsavedRevision newRevision) {
        properties.put(JsonConstants.FIELD_TITLE, title);
        properties.put(JsonConstants.FIELD_TYPE, type);
        properties.put(JsonConstants.FIELD_PERMANENT, String.valueOf(permanent));
        properties.put(JsonConstants.FIELD_TITLE_SHOW, String.valueOf(showTitle));
        return properties;
    }

    @Override
    public Field setProperties(Map properties) {
        setTitle(String.valueOf(properties.get(JsonConstants.FIELD_TITLE)));
        setType((Integer) properties.get(JsonConstants.FIELD_TYPE));
        setIsPermanent(Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_PERMANENT))));
        setShowTitle(Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.FIELD_TITLE_SHOW))));
        return this;
    }

    public abstract Field copy();
    public abstract String getEntry(int element);

}
