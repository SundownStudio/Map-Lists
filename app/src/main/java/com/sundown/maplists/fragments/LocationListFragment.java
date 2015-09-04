package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.views.ListItemSingleView;
import com.sundown.maplists.views.LocationListView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sundown on 7/21/2015.
 */
public class LocationListFragment extends Fragment {



    private LocationListView view;
    private SecondaryList model;
    private LinearLayout layout;
    private final static LinearLayout.LayoutParams layoutFillWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static LinearLayout.LayoutParams layoutWrapWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private StringBuffer stringBuffer;
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


    public static LocationListFragment newInstance(SecondaryList model) {
        LocationListFragment fragment = new LocationListFragment();
        fragment.model = model;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stringBuffer = new StringBuffer();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        view = (LocationListView) inflater.inflate(R.layout.location_list_view, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        initLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
    }


    private void initLayout(){

        if (layout != null)
            layout.removeAllViews();
        layout = new LinearLayout(getActivity());
        layout.setLayoutParams(layoutFillWidth);
        layout.setOrientation(LinearLayout.VERTICAL);

        Integer[] keys = model.getKeys();

        for (Integer k: keys){
            Field field = model.getField(k);
            field.setId(k);
            determineViewType(field);
        }
        view.updateView(layout);
    }



    private void determineViewType(Field field){
        FieldType type = field.type;

        switch (type) {
            case SUBJECT: {
                EntryField entryField = (EntryField) field;
                view.setSubject(entryField.entry, model.color);
                break;
            }
            case NAME:
            case PHONE:
            case EMAIL:
            case DATE:
            case TIME:
            case URL:
            case PRICE: {
                EntryField entryField = (EntryField) field;
                ListItemSingleView v1 = (ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, layout, false);
                ListItemSingleView v2 = (ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, layout, false);
                v1.initAsTitle(entryField.title);
                v2.initAsEntry(imageResources.get(type), entryField.entry, false);
                layout.addView(v1);
                layout.addView(v2);
                break;
            }

        }
    }






    private void addTitleView(Field field){
        String title = field.title;
        if (title != null && title.length() > 0) {
            TextView titleView = new TextView(getActivity());
            titleView.setText(field.title);
            layout.addView(titleView);
        }

    }


    public void addComponentView(Field field) {

        View view;

        switch (field.type) {
            case PHOTO:
                view = new ImageView(getActivity());
                break;

            case RATING: {
                EntryField entry = (EntryField) field;
                RatingBar bar = new RatingBar(getActivity());
                bar.setNumStars(5);
                bar.setEnabled(false);
                if (entry.entry != null) {
                    try {
                        bar.setRating(Float.parseFloat(entry.entry));
                    } catch (Exception e) {}
                }
                view = bar;
                break;
            }

            case CHECKBOX: {
                EntryField entryField = (EntryField) field;
                CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setEnabled(false);
                if (entryField.entry != null) {
                    try {
                        checkBox.setChecked(entryField.entry.equals("1"));
                    } catch (Exception e) {}
                }
                view = checkBox;
                break;
            }

            default:{
                EntryField entryField = (EntryField) field;
                TextView v = new TextView(getActivity());
                stringBuffer.setLength(0);

                String title = field.title;
                if (title != null && title.length() > 0) {
                    stringBuffer.append(title + ": ");
                }

                if (entryField.entry != null){
                    stringBuffer.append(entryField.entry);
                    v.setText(stringBuffer.toString());
                }
                view = v;
                break;
            }
        }

        if (field.type == FieldType.RATING){
            view.setLayoutParams(layoutWrapWidth);
        } else {
            view.setLayoutParams(layoutFillWidth);
        }

        layout.addView(view);

    }

}
