package com.sundown.maplists.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Sundown on 5/21/2015.
 */
public class Locations {

    private TreeMap<LatLng, PrimaryList> primaryLists = new TreeMap<>(new LatLngComparator()); //holding all data pertinent to locations
    public TreeMap<LatLng, PrimaryList> getPrimaryLists(){return primaryLists;}
    private HashMap<LatLng, Marker> markers = new HashMap<>(); //holding all markers so we can slide skip to next on map - todo separate from locations cuz those are used elsewhere
    //todo: merge these

    private static Locations instance;
    private Locations(){}

    public static Locations getInstance(){
        if (instance == null) instance = new Locations();
        return instance;
    }

    public void clear(){
        primaryLists.clear();
        markers.clear();
    }

    public PrimaryList getPrimaryList(LatLng latLng){
        return primaryLists.get(latLng);
    }

    public Marker getNextMarker(LatLng latLng, boolean forward){

        LatLng nextKey;
        if (latLng == null) {
            if (forward) {
                nextKey = primaryLists.firstKey();
            } else {
                nextKey = primaryLists.lastKey();
            }
        } else {
            if (forward) {
                nextKey = primaryLists.higherKey(latLng);
                if (nextKey == null) {
                    nextKey = primaryLists.firstKey();
                }
            } else {
                nextKey = primaryLists.lowerKey(latLng);
                if (nextKey == null) {
                    nextKey = primaryLists.lastKey();
                }
            }
        }

        return markers.get(nextKey);
    }

    public void storePrimaryList(LatLng latLng, PrimaryList list){
        primaryLists.put(latLng, list);
    }

    public void storeMarker(LatLng latLng, Marker marker){ //todo: see if we can put this all into the one item..
        markers.put(latLng, marker);
    }

    //used for dragging
    public void swap(LatLng oldLatLng, LatLng newLatLng){
        PrimaryList list = removeMapList(oldLatLng);
        Marker marker = removeMarker(oldLatLng);

        list.setLatLng(newLatLng);

        storePrimaryList(newLatLng, list);
        storeMarker(newLatLng, marker);
    }


    private PrimaryList removeMapList(LatLng latLng){
        return primaryLists.remove(latLng);
    }

    private Marker removeMarker(LatLng latLng){
        return markers.remove(latLng);
    }

    public int numLocations(){
        return primaryLists.size();
    }



    private class LatLngComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            LatLng l = (LatLng)lhs;
            LatLng r = (LatLng)rhs;
            double diff = l.longitude - r.longitude;
            if (diff > 0)
                return 1;
            else if (diff < 0)
                return -1;
            else {
                diff = l.latitude - r.latitude;
                if (diff > 0){
                    return 1;
                } else if (diff < 0){
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
