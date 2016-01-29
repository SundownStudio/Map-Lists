package com.sundown.maplists.models.lists;

import com.couchbase.lite.UnsavedRevision;
import com.sundown.maplists.models.PropertiesHandler;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.MAP_ID;

/**
 * Created by Sundown on 7/14/2015.
 */
public abstract class BaseList implements PropertiesHandler<BaseList> {

    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    protected void setDocumentId(String documentId) { this.documentId = documentId; }

    private int mapId;

    private void setMapId(int mapId) { this.mapId = mapId; }

    public int getMapId() {
        return mapId;
    }

    private Schema schema = new Schema();

    public Schema getSchema(){ return schema;}

    public void setSchema(Schema schema){ this.schema = schema;}

    protected BaseList(int mapId) {
        setMapId(mapId);
    }


    @Override
    public Map<String, Object> getProperties(Map<String, Object> properties, UnsavedRevision newRevision) {
        properties.put(MAP_ID, mapId);
        return schema.getProperties(properties, newRevision);
    }

    @Override
    public BaseList setProperties(Map properties) {
        setDocumentId(String.valueOf(properties.get(DOCUMENT_ID)));
        setMapId((Integer) properties.get(MAP_ID));
        schema.setProperties(properties);
        return this;
    }
}
