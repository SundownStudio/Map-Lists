package com.sundown.maplists.storage;

/**
 * Created by Sundown on 6/16/2015.
 */
public final class JsonConstants {

    public static final String PARENT_DOC_ID = "parent_doc";
    public static final String DOCUMENT_ID = "_id"; //underscore REQUIRED for cb, do not use for our own fields

    public static final String MAP_ID = "mapId";
    public static final String MAP_LATITUDE = "lat";
    public static final String MAP_LONGITUDE = "lon";
    public static final String MAP_MULTIPLE_LISTS_ENABLED = "multiple_lists_enabled";


    public static final String LIST_ID = "listId";
    public static final String FIELDS = "fields";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_TITLE_SHOW = "showTitle";
    public static final String FIELD_ENTRY = "entry";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_PERMANENT = "permanent";
    public static final String IMAGE = "image";
    public static final String THUMB = "thumb";
    public static final String COLOR = "color";

    public static final String SCHEMA_ID = "schemaId";
    public static final String SCHEMA_NAME = "schemaName";

    public static final String TYPE = "type";
    public static final String TYPE_MAP_LIST = "mapList";
    public static final String TYPE_LOCATION_LIST = "locationList";
    public static final String TYPE_SCHEMA_LIST = "schemaList";
    public static final String OPERATION = "operation";
    public static final String COUNTS = "counts";
    public static final String COUNT_SCHEMAS = "schema_counts";
    public static final String COUNT_MAP_LISTS = "mapList_counts";
}
