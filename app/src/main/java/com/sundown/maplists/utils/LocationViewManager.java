package com.sundown.maplists.utils;

import android.content.Context;
import android.view.LayoutInflater;

import com.sundown.maplists.MapListsApp;
import com.sundown.maplists.R;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.views.ListItemDoubleView;
import com.sundown.maplists.views.ListItemSingleView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 9/17/2015.
 */
public class LocationViewManager {

    private static LocationViewManager instance;
    private LayoutInflater inflater;
    private static final Map<FieldType, Integer> imageResources;
    static
    {
        imageResources = new HashMap<>();
        imageResources.put(FieldType.NAME, R.drawable.ic_name);
        imageResources.put(FieldType.PHONE, R.drawable.ic_phonenumber);
        imageResources.put(FieldType.EMAIL, R.drawable.ic_email);
        imageResources.put(FieldType.DATE, R.drawable.ic_date);
        imageResources.put(FieldType.TIME, R.drawable.ic_time);
        imageResources.put(FieldType.URL, R.drawable.ic_url);
        imageResources.put(FieldType.PRICE, R.drawable.ic_price);
    }

    public static LocationViewManager getInstance(){
        if (instance == null){
            instance = new LocationViewManager(MapListsApp.getContext());
        }
        return instance;
    }

    private LocationViewManager(Context context){
        inflater = LayoutInflater.from(context);
    }

    public LocationViewManager reset(Context context){
        inflater = LayoutInflater.from(context);
        return this;
    }

    private ListItemSingleView createListItemSingleView(){
        return (ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, null, false);
    }

    private ListItemDoubleView createListItemDoubleView(){
        return (ListItemDoubleView)inflater.inflate(R.layout.list_item_double_view, null, false);
    }

    public ListItemSingleView drawSingleView(FieldType type1, String entry1, boolean showAsTitle){
        ListItemSingleView view = createListItemSingleView();
        Integer resource = imageResources.get(type1);
        if (showAsTitle)
            resource = 0;
        view.init(resource, entry1);
        return view;
    }

    public ListItemDoubleView drawDoubleView(FieldType type1, FieldType type2, String entry1, String entry2){
        ListItemDoubleView view = createListItemDoubleView();
        view.init(imageResources.get(type1), imageResources.get(type2), entry1, entry2);
        return view;
    }
}
