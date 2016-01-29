package com.sundown.maplists.models;

import com.couchbase.lite.UnsavedRevision;

import java.util.Map;

/**
 * Created by Sundown on 6/16/2015.
 */
public interface PropertiesHandler<T> {
    Map<String, Object> getProperties(Map<String, Object> properties, UnsavedRevision newRevision);
    T setProperties(Map<String, Object> properties);
}
