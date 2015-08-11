package com.sundown.maplists.interfaces;

import java.util.Map;

/**
 * Created by Sundown on 6/16/2015.
 */
public interface PropertiesHandler</*S, */T> {
    Map<String, Object> getProperties(/*S s*/);
    T setProperties(Map<String, Object> properties);
}
