package com.sundown.maplists;

/**
 * Created by Sundown on 1/19/2016.
 */
public class Constants {

    private Constants() {}

    //operations
    public static final int OP_INSERT = 0;
    public static final int OP_UPDATE = 1;
    public static final int OP_DELETE_LOCATION = 2;
    public static final int OP_DELETE_SECONDARY_LIST = 3;
    public static final int OP_DELETE_SCHEMA = 4;


    public static final String MARKER_COLOR_DEFAULT = "#303F9F";
    public static final int SEEKBAR_MIN = 1;
    public static final int SEEKBAR_MAX_REGULAR = 5;
    public static final int SEEKBAR_MAX_LIST_ITEMS = 10;
    public static final int SCHEMA_ID_DEFAULT = 0;
    public static final int LIST_ID_DEFAULT = -1;
    public static final int MAX_ITEMS_PER_LIST = 9999;

}
