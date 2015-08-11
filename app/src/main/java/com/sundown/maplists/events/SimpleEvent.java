package com.sundown.maplists.events;

/**
 * Created by Sundown on 5/20/2015.
 */
public class SimpleEvent extends Event {

    private String type;
    private Object source;

    public SimpleEvent(String type) {
        this.type = type;
    }


    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
    }
}
