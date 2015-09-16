package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.utils.ColorUtils;
import com.sundown.maplists.views.ListItemDoubleView;
import com.sundown.maplists.views.ListItemSingleView;
import com.sundown.maplists.views.LocationListView;
import com.sundown.maplists.views.PhotoView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sundown on 7/21/2015.
 */
public class LocationListFragment extends Fragment {



    private LocationListView view;
    private LocationList model;
    private LinearLayout layout;
    private final static LinearLayout.LayoutParams layoutWrapWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static LinearLayout.LayoutParams layoutFillWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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


    public static LocationListFragment newInstance() {
        return new LocationListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
    }

    public void setModel(LocationList model){
        this.model = model;
        initLayout();
    }

    private void initLayout(){

        if (layout != null)
            layout.removeAllViews();
        layout = new LinearLayout(getActivity());
        layout.setLayoutParams(layoutFillWidth);
        layout.setOrientation(LinearLayout.VERTICAL);

        int ids = 0;
        List<Field> fields = model.getFields();
        for (Field field: fields){
            field.setId(ids++);
            determineViewType(field);
        }
        view.updateView(layout);
    }



    private void determineViewType(Field field){
        FieldType type = field.getType();

        switch (type) {
            case SUBJECT: {
                EntryField entryField = (EntryField) field;
                view.setSubject(entryField.getEntry(0), model.getColor());
                break;
            }
            case NAME:
            case PHONE:
            case EMAIL:
            case DATE:
            case TIME:
            case DATE_TIME:
            case URL:
            case PRICE: {
                drawViewsWithIcons((EntryField) field);
                break;
            }

            case RATING: {
                EntryField entryField = (EntryField) field;
                RatingBar bar = new RatingBar(getActivity());
                bar.setNumStars(5);
                bar.setEnabled(false);
                drawTitleView(entryField.getTitle());
                String entry = entryField.getEntry(0);
                if (entry != null) {
                    try {
                        bar.setRating(Float.parseFloat(entry));
                    } catch (Exception e) {
                        Log.e(e);
                    }
                }
                bar.setLayoutParams(layoutWrapWidth);
                layout.addView(bar);
                break;
            }

            case CHECKBOX: {
                EntryField entryField = (EntryField) field;
                CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setEnabled(false);
                drawTitleView(entryField.getTitle());
                String entry = entryField.getEntry(0);
                if (entry != null) {
                    try {
                        checkBox.setChecked(entry.equals("1"));
                    } catch (Exception e) {
                        Log.e(e);
                    }
                }
                checkBox.setLayoutParams(layoutFillWidth);
                layout.addView(checkBox);
                break;
            }

            case PHOTO: {
                PhotoField photoField = (PhotoField) field;
                photoField.loadBitmaps(model.getDocumentId());
                PhotoView photoView = (PhotoView)inflater.inflate(R.layout.photo_container, layout, false);
                photoView.setBitmap(photoField.getImageBitmap());
                photoView.disableAllButtons();
                layout.addView(photoView);
                break;
            }

            default: {
                EntryField entryField = (EntryField) field;
                drawTitleView(entryField.getTitle());
                drawSingleView(entryField.getEntry(0), type, false);
            }
        }
    }

    private void drawViewsWithIcons(EntryField entryField){
        int size = entryField.getNumEntries();
        drawTitleView(entryField.getTitle());
        for (int i = 0; i < size; ++i){
            if (size > i+1) {
                drawDoubleView(entryField.getEntry(i), entryField.getEntry(i + 1), entryField.getType());
                i++;
            } else {
                drawSingleView(entryField.getEntry(i), entryField.getType(), true);
            }
        }
    }

    private void drawTitleView(String title){
        ListItemSingleView view = getSingleView();
        view.initAsTitle(title);
        layout.addView(view);
    }


    private void drawDoubleView(String entry1, String entry2, FieldType type){
        ListItemDoubleView view = (ListItemDoubleView)inflater.inflate(R.layout.list_item_double_view, layout, false);
        if (type == FieldType.DATE_TIME) {
            view.initWithIcon(imageResources.get(FieldType.DATE), imageResources.get(FieldType.TIME), entry1, entry2);
        } else if (type == FieldType.PRICE){
            entry1 = ColorUtils.determineColorText(entry1);
            entry2 = ColorUtils.determineColorText(entry2);
            view.initWithIcon(imageResources.get(type), imageResources.get(type), entry1, entry2);

        } else {
            view.initWithIcon(imageResources.get(type), imageResources.get(type), entry1, entry2);
        }
        layout.addView(view);
    }


    private void drawSingleView(String entry1, FieldType type, boolean drawWithIcon){
        ListItemSingleView view = getSingleView();
        if (drawWithIcon) {
            view.initWithIcon(imageResources.get(type), entry1);
        } else {
            view.initWithoutIcon(entry1);
        }
        layout.addView(view);
    }

    private ListItemSingleView getSingleView(){
        return  (ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, layout, false);
    }

}
