package com.sundown.maplists.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.dialogs.ColorPickerDialogFragment;
import com.sundown.maplists.dialogs.EditTitleDialogFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.views.AddListView;
import com.sundown.maplists.views.FieldView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.sundown.maplists.models.FieldType.PHOTO;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListFragment extends Fragment implements FieldView.FieldViewListener, PhotoFragment.PhotoFragmentListener, ColorPickerDialogFragment.ColorPickerListener {



    private static final String FRAGMENT_EDIT_FIELD_TITLE = "EDIT_TITLE";
    private static final String FRAGMENT_PICK_COLOR = "PICK_COLOR";
    private static final double PROP_HEIGHT = .41;
    private static final double PROP_WIDTH = .85;
    private FragmentManager fm;


    /** Fragment for editing the title of a current field in this list */
    private EditTitleDialogFragment editTitleDialogFragment;

    /** Pick a color for this item */
    private ColorPickerDialogFragment colorPickerDialogFragment;

    /** serves as a container for our photo fragments */
    private HashMap<Integer, PhotoFragment> photoFragments = new HashMap<>();

    /** the view for this fragment */
    private AddListView view;

    /** our model, can either be a MapList or a SecondaryList */
    private LocationList model;

    /** the form of fields used to populate our view */
    private LinearLayout form;
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    /** the width/height of this dialog fragment */
    private int width, height;

    private ActivityResult result;


    public static AddListFragment newInstance(LocationList model) {
        AddListFragment fragment = new AddListFragment();
        fragment.model = model;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //NOTE: if user rotates in camera/gallery, upon returning to our app THIS method gets called BEFORE the main activity's onResult... however
        //if user DOESNT rotate in camera/gallery, then this doesn't get called AT ALL.. //todo see if this is true now..

        setRetainInstance(true);
        fm = getChildFragmentManager();
        view = (AddListView) inflater.inflate(R.layout.fragment_add_list, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        drawForm();

    }

    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);

    }

    @Override //cant do transactions after this method is called.. leads to that wonderful crash.. but must remove fragments for them to display on reload and cant do it when putting together layout
    public void onSaveInstanceState(Bundle outState) {
        clearForm();
        super.onSaveInstanceState(outState);
    }


    public void setActivityResult(ActivityResult result){ this.result = result;}


    public void handleActivityResult(){
        if (result != null) {
            PreferenceManager preferenceManager = PreferenceManager.getInstance();
            int callingId = preferenceManager.getInt(PhotoFragment.FRAGMENT_ID);
            photoFragments.get(callingId).onActivityResult(result.requestCode, result.resultCode, result.data);
            result = null;
        }
    }

    private void clearForm(){
        Set<Integer> keys = photoFragments.keySet();
        for (Integer key: keys){
            fm.beginTransaction().remove(photoFragments.get(key)).commit();
        }
        photoFragments.clear();
        form.removeAllViews();
    }


    private void drawForm(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = (int) (displaymetrics.heightPixels * PROP_HEIGHT);
        width = (int) (displaymetrics.widthPixels * PROP_WIDTH);

        if (form != null)
            clearForm();
        form = new LinearLayout(getActivity());
        form.setLayoutParams(layoutParams);
        form.setOrientation(LinearLayout.VERTICAL);

        int ids = 0;
        List<Field> fields = model.getFields();
        for (Field field: fields){
            addToForm(ids++, field);
        }
        view.updateView(form);
        handleActivityResult();
    }

    public void addToForm(int id, Field field) {
        field.setId(id);
        field.setObserver(addFieldView(field));
        if (field.getType() == PHOTO) {
            addPhotoFragment(id, (PhotoField) field);
        }
    }

    public void addNewField(Field field){
        int id = model.addField(field)-1;
        addToForm(id, field);
        view.scrollToBottom();
    }


    private FieldView addFieldView(Field field) {
        FieldView fieldView = (FieldView) getActivity().getLayoutInflater().inflate(R.layout.fieldview, null, false);
        fieldView.init(getActivity(), this, field);
        fieldView.setTag(field.getId());
        form.addView(fieldView);
        return fieldView;
    }

    private void addPhotoFragment(int containerViewId, PhotoField field) {
        PhotoFragment photoFragment = photoFragments.get(containerViewId);
        if (photoFragment == null) {
            photoFragment = PhotoFragment.newInstance();
        }

        field.loadBitmaps(model.getDocumentId());
        photoFragment.setListenerAndImageData(containerViewId, this, field, width, height);
        photoFragments.put(containerViewId, photoFragment);
        fm.beginTransaction().replace(containerViewId, photoFragment).commit();
    }

    public LocationList refreshModel() {

        int numFields = model.getFields().size();
        for (int i = 0; i < numFields; ++i){
            FieldView fieldView = (FieldView) form.findViewWithTag(i);
            try {
                if (fieldView.getType() != PHOTO) {
                    EntryField entryField = (EntryField) model.getField(i);
                    int numEntries = entryField.getNumEntries();
                    entryField.clearEntries();
                    for (int j = 0; j < numEntries; ++j)
                        entryField.addEntry(fieldView.getEntry(j));
                }
            } catch (NullPointerException e){
                Log.e(e);
            }
        }
        return model;
    }

    private boolean isFieldValidForTitleDisplay(Field field){
        if (model instanceof SecondaryList) {
            switch (field.getType()) {
                case NAME:
                case PHONE:
                case EMAIL:
                case DATE:
                case TIME:
                case URL:
                case PRICE: {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void editFieldTitle(int tag) {
        Field field = model.getField(tag);
        editTitleDialogFragment = EditTitleDialogFragment.newInstance(field, isFieldValidForTitleDisplay(field));
        editTitleDialogFragment.show(fm, FRAGMENT_EDIT_FIELD_TITLE);
    }

    @Override
    public void deleteField(int tag) {
        model.removeField(tag); //remove from model
        drawForm(); //and redraw the form
    }

    @Override
    public void colorField(int tag) {
        colorPickerDialogFragment = ColorPickerDialogFragment.newInstance(this);
        colorPickerDialogFragment.show(fm, FRAGMENT_PICK_COLOR);
    }


    @Override
    public void deleteImage(String imageName, String thumbName) {
        if (imageName != null && imageName.length() > 0) {
            DatabaseCommunicator db = DatabaseCommunicator.getInstance();
            db.deleteAttachment(model.getDocumentId(), imageName);
            db.deleteAttachment(model.getDocumentId(), thumbName);
        }
    }

    @Override
    public void deletePhotoFragment(int id) {
        PhotoFragment photoFragment = photoFragments.remove(id);
        fm.beginTransaction().remove(photoFragment).commit();
        deleteField(id);
    }

    @Override
    public void colorPicked(String color) {
        model.setColor(Color.parseColor(color));
    }
}
