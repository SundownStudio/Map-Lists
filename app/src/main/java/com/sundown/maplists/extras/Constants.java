package com.sundown.maplists.extras;

/**
 * Created by Sundown on 4/21/2015.
 */
public interface Constants {




    class LIMITS {
        public static final int MAX_ITEMS_PER_LIST = 9999;
    }

    class SPECS {
        public static final double PROP_HEIGHT = .41;
        public static final double PROP_WIDTH = .85;
        public static final float DEFAULT_ZOOM = 2.0f;
    }

    class FRAGMENT_TAGS {
        public static final String FRAGMENT_MAP = "MAP";
        public static final String FRAGMENT_LOCATION_LISTS = "LOCATION_LISTS";
        public static final String FRAGMENT_DELETE = "DELETE";
        public static final String FRAGMENT_ENTER_ADDRESS= "ENTER_ADDRESS";
        public static final String FRAGMENT_SELECT_FIELD = "SELECT_FIELD";
        public static final String FRAGMENT_EDIT_FIELD_TITLE = "EDIT_TITLE";
        public static final String FRAGMENT_ADD_LIST = "ADD_LIST";
        public static final String FRAGMENT_LOCATION_LIST = "LOCATION_LIST";

    }


    class ACTIVITY_CODES {
        public static final int ACTIVITY_CAMERA = 10;
        public static final int ACTIVITY_GALLERY = 20;
    }


    /* FIELD TITLES ARE LOCATED IN STRINGS.XML */
    class FIELDS {
        public static final int FIELD_NAME = 0;
        public static final int FIELD_TEXT = 1;
        public static final int FIELD_NUMBER = 2;
        public static final int FIELD_DECIMAL = 3;
        public static final int FIELD_DROPDOWN = 4;
        public static final int FIELD_DATE = 5;
        public static final int FIELD_TIME = 6;
        public static final int FIELD_CHECKED = 7;
        public static final int FIELD_PIC = 8;
        public static final int FIELD_URL = 9;
        public static final int FIELD_PHONE = 10;
        public static final int FIELD_RATING = 11;
    }

    class GEOCODE {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final int FROM_LATLNG = 50;
        public static final int FROM_ADDRESS = 150;
        public static final String GEO_OPERATION = "geo-op";
        public static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
        public static final String MAP_LATITUDE = "lat";
        public static final String MAP_LONGITUDE = "lon";
        public static final String MAP_ADDRESS = "address";
        public static final String MAP_ERROR = "error";
    }

}
