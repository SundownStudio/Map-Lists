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
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.PhotoField;
import com.sundown.maplists.models.lists.SchemaList;
import com.sundown.maplists.utils.SecondaryListViewManager;
import com.sundown.maplists.utils.ViewUtils;
import com.sundown.maplists.views.ListItemSingleView;
import com.sundown.maplists.views.PhotoView;
import com.sundown.maplists.views.SecondaryListView;

import java.util.List;

/**
 * Created by Sundown on 7/21/2015.
 */
public class SecondaryListFragment extends Fragment {



    private SecondaryListView view;
    private SchemaList model;
    private LinearLayout layout;
    private final static LinearLayout.LayoutParams layoutWrapWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final static LinearLayout.LayoutParams layoutFillWidth = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private LayoutInflater inflater;
    private SecondaryListViewManager secondaryListViewManager;

    public static SecondaryListFragment newInstance() {
        return new SecondaryListFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        view = (SecondaryListView) inflater.inflate(R.layout.secondary_list_view, container, false);
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

    public void setModel(SchemaList model){
        this.model = model;
        initLayout();
    }

    private void initLayout(){
        secondaryListViewManager = SecondaryListViewManager.getInstance().reset(getContext());

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
        int type = field.getType();

        switch (type) {
            case Field.SUBJECT: {
                EntryField entryField = (EntryField) field;
                view.setSubject(ViewUtils.getTopRoundedCornersDrawable(getActivity().getResources().getDimension(R.dimen.rounded_corners), model.getColor()), entryField.getEntry(0));
                break;
            }
            case Field.NAME:
            case Field.PHONE:
            case Field.EMAIL:
            case Field.DATE:
            case Field.TIME:
            case Field.DATE_TIME:
            case Field.URL:
            case Field.PRICE: {
                EntryField entryField = (EntryField) field;
                addTitleView(entryField);

                int size = entryField.getNumEntries();
                for (int i = 0; i < size; ++i) {
                    if (size > i + 1) {

                        if (type == Field.DATE_TIME){
                            layout.addView(secondaryListViewManager.drawDoubleView(Field.DATE, Field.TIME, entryField.getEntry(i), entryField.getEntry(++i)));
                        } else if (type == Field.PRICE){
                            layout.addView(secondaryListViewManager.drawDoubleView(type, type, entryField.getEntry(i), entryField.getEntry(++i)));

                        } else {
                            layout.addView(secondaryListViewManager.drawDoubleView(type, type, entryField.getEntry(i), entryField.getEntry(++i)));
                        }

                    } else {
                        ListItemSingleView view = secondaryListViewManager.drawSingleView(entryField.getType(), entryField.getEntry(i), false);
                        layout.addView(view);
                    }
                }
                break;
            }

            case Field.RATING: {
                EntryField entryField = (EntryField) field;
                RatingBar bar = new RatingBar(getActivity());
                bar.setNumStars(5);
                bar.setEnabled(false);
                addTitleView(entryField);
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

            case Field.CHECKBOX: {
                EntryField entryField = (EntryField) field;
                CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setEnabled(false);
                addTitleView(entryField);
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

            case Field.PHOTO: {
                PhotoField photoField = (PhotoField) field;
                photoField.loadBitmaps(model.getDocumentId());
                PhotoView photoView = (PhotoView)inflater.inflate(R.layout.photo_container, layout, false);
                photoView.setBitmap(photoField.getImageBitmap());
                photoView.disableAllButtons();
                layout.addView(photoView);
                break;
            }

            case Field.ITEM_LIST:{
                EntryField entryField = (EntryField) field;
                addTitleView(entryField);
                addAllDoubleViews(entryField, -1, -1);
                break;
            }

            case Field.PRICE_LIST:{
                EntryField entryField = (EntryField) field;
                addTitleView(entryField);
                addAllDoubleViews(entryField, -1, Field.PRICE);
                break;
            }

            default: {
                drawViewsWithoutIcons((EntryField) field);
            }
        }
    }

    private void addTitleView(EntryField entryField){
        ListItemSingleView v = secondaryListViewManager.drawSingleView(-1, entryField.getTitle(), true);
        layout.addView(v);
    }

    private void addAllDoubleViews(EntryField entryField, int type1, int type2){
        int size = entryField.getNumEntries();
        for (int i = 0; i < size; ++i) {
            if (size > i + 1) {
                layout.addView(secondaryListViewManager.drawDoubleView(type1, type2, entryField.getEntry(i), entryField.getEntry(++i)));
            }
        }
    }

    private void drawViewsWithoutIcons(EntryField entryField){
        addTitleView(entryField);
        int size = entryField.getNumEntries();
        for (int i = 0; i < size; ++i) {
            ListItemSingleView view = secondaryListViewManager.drawSingleView(entryField.getType(), entryField.getEntry(i), false);
            layout.addView(view);
        }
    }

}
