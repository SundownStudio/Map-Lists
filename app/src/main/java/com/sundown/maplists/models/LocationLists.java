package com.sundown.maplists.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 6/22/2015.
 */
public class LocationLists {

    private List<LocationList> locationItems = new ArrayList<>();
    public List<LocationList> getList(){return locationItems;}

    public LocationLists(){}

    public void addItem(LocationList item){locationItems.add(item);}

    public void clear(){locationItems.clear();}

}
