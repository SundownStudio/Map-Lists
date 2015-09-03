package com.sundown.maplists.models;

import com.sundown.maplists.storage.JsonConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 6/22/2015.
 */
public abstract class Field implements PropertiesHandler/*, Cloneable*/ {

    public interface Observer{
        void updateTitle(String title);
    }

    public int id; //used for view tagging
    public void setId(int id){ this.id = id;}
    public FieldType type;
    public boolean permanent;
    public String title;
    public void setTitle(String title){
        this.title = title;
        observer.updateTitle(title);
    }
    public boolean showTitle;
    public void setShowTitle(boolean show){
        this.showTitle = show;
    }

    public Observer observer; //the corresponding fieldview object
    public void setObserver(Observer observer){this.observer = observer;}


    public Field(int id, String title, FieldType type, boolean permanent){
        this.id = id;
        this.title = title;
        this.type = type;
        this.permanent = permanent;
        this.showTitle = false;
    }

    public String getTitle(){
        return title;
    }

    public String getType(){
        return type.name();
    }

    @Override //NOTE does not override hashCode because we dont care about that for our purposes
    public boolean equals(Object o) {
        if (o instanceof Field){
            Field a = (Field)o;
            if (a.type == this.type){
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

    /*
        @Override
    protected Field clone() {
        Field clone = null;
        try{
            clone = (Field) super.clone();
            if (clone instanceof EntryField){
                ((EntryField) clone).entry = null;
                ((EntryField) clone).observer = null;

            } else if (clone instanceof PhotoField){
                ((PhotoField) clone).observer = null;
            }

        }catch(CloneNotSupportedException e){
            Log.e(e);
        }

        return clone;
    }
     */
}
