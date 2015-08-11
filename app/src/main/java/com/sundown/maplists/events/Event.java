package com.sundown.maplists.events;

/**
 * Created by Sundown on 5/20/2015.
 */
public abstract class Event {
    abstract String getType();
    abstract Object getSource();
    abstract void setSource(Object source);
}
