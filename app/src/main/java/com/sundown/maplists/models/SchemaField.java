package com.sundown.maplists.models;

/**
 * Created by Sundown on 8/26/2015.
 */
public class SchemaField extends Field {

    public SchemaField(Field field) {
        super(field.getTitle(), field.getType(), field.isPermanent());
    }

}
