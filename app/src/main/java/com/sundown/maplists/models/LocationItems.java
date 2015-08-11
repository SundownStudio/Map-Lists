package com.sundown.maplists.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 6/22/2015.
 */
public class LocationItems {

    private List<LocationItem> locationItems = new ArrayList<>();
    public List<LocationItem> getList(){return locationItems;}

    public LocationItems(){}

    public void addItem(LocationItem item){locationItems.add(item);}

    public void clear(){locationItems.clear();}

}
