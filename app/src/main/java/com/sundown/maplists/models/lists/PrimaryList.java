package com.sundown.maplists.models.lists;

import com.google.android.gms.maps.model.LatLng;
import com.sundown.maplists.storage.JsonConstants;

import java.util.Map;

/**
 * These are the lists associated with each marker, each primary list denotes a marker on the map
 */
public class PrimaryList extends MapList {

    private LatLng latLng;

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    protected PrimaryList(int mapId) {
        super(mapId);
        setListType(ListType.PRIMARY);
    }

    @Override
    public SchemaList copySchema() {
        SchemaList schemaList = super.copy();
        schemaList.setListType(ListType.PRIMARY_SCHEMA);
        return schemaList;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.MAP_LATITUDE, latLng.latitude);
        properties.put(JsonConstants.MAP_LONGITUDE, latLng.longitude);
        return properties;
    }

    @Override
    public PrimaryList setProperties(Map properties) {
        super.setProperties(properties);
        setLatLng(new LatLng((Double) properties.get(JsonConstants.MAP_LATITUDE), (Double) properties.get(JsonConstants.MAP_LONGITUDE)));
        return this;
    }

}
