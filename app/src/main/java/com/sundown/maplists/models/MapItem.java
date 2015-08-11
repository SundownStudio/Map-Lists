package com.sundown.maplists.models;

import com.google.android.gms.maps.model.LatLng;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;

/**
 * Created by Sundown on 4/13/2015.
 */
public class MapItem extends Item {/*, Parcelable*/ //NOTE: We shouldnt need Parcelable anymore (5/20).. but keeping it anyway for future example

    public boolean list;
    public LatLng latLng;


    public MapItem(){
        super(-1);
        super.addField(new EntryField(-1, "Name", "New Location", Constants.FIELDS.FIELD_TEXT, true));
        super.addField(new EntryField(-1, "Snippet", "Empty", Constants.FIELDS.FIELD_TEXT, true));
        super.addField(new PhotoField(-1, true));
        list = false;
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.TYPE, JsonConstants.TYPE_MAP_ITEM);
        properties.put(JsonConstants.MAP_LATITUDE, latLng.latitude);
        properties.put(JsonConstants.MAP_LONGITUDE, latLng.longitude);
        properties.put(JsonConstants.MAP_LIST, String.valueOf(list));
        return properties;
    }

    @Override
    public MapItem setProperties(Map properties) {
        super.setProperties(properties);
        list = Boolean.parseBoolean(String.valueOf(properties.get(JsonConstants.MAP_LIST)));
        latLng = new LatLng((Double) properties.get(JsonConstants.MAP_LATITUDE), (Double) properties.get(JsonConstants.MAP_LONGITUDE));
        return this;
    }

}

    /*
    private static final String KEY_ID = "_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SNIPPET = "snippet";
    private static final String KEY_LIST = "list";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_THUMBNAIL = "thumb";
    private static final String KEY_COLOR = "color";

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        Bundle bundle = new Bundle();

        bundle.putInt(KEY_ID, id);
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_SNIPPET, snippet);
        bundle.putInt(KEY_LIST, list);
        bundle.putString(KEY_IMAGE, image);
        bundle.putString(KEY_THUMBNAIL, thumbnail);
        bundle.putString(KEY_COLOR, color);

        dest.writeBundle(bundle);
    }


    /**
     * Creator required for class implementing the parcelable interface.
     */
    /*
    public static final Parcelable.Creator<MarkerItem> CREATOR = new Creator<MarkerItem>() {

        @Override
        public MarkerItem createFromParcel(Parcel source) {
            // read the bundle containing key value pairs from the parcel
            Bundle bundle = source.readBundle();

            // instantiate a person using values from the bundle
            return new MarkerItem(bundle.getInt(KEY_ID),
                    bundle.getString(KEY_TITLE),
                    bundle.getString(KEY_SNIPPET),
                    bundle.getInt(KEY_LIST),
                    bundle.getString(KEY_IMAGE),
                    bundle.getString(KEY_THUMBNAIL),
                    bundle.getString(KEY_COLOR));
        }

        @Override
        public MarkerItem[] newArray(int size) {
            return new MarkerItem[size];
        }

    }; */