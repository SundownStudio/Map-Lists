package com.sundown.maplists.events;



/**
 * Created by Sundown on 5/20/2015.
 */
public abstract class Dispatcher {
    abstract void addListener(String type, EventListener listener);
    abstract void removeListener(String type, EventListener listener);
    abstract boolean hasListener(String type, EventListener listener);
    abstract void dispatchEvent(Event event);
}
