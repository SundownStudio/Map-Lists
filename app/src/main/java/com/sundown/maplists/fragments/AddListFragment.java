package com.sundown.maplists.fragments;

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
import java.util.Set;

import static com.sundown.maplists.models.FieldType.PHOTO;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListFragment extends Fragment implements FieldView.FieldViewListener, PhotoFragment.PhotoFragmentListener {



    private static final String FRAGMENT_EDIT_FIELD_TITLE = "EDIT_TITLE";
    private static final double PROP_HEIGHT = .41;
    private static final double PROP_WIDTH = .85;
    private FragmentManager fm;


    /** Fragment for editing the title of a current field in this list */
    private EditTitleDialogFragment editTitleDialogFragment;

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
        Set<Integer> keys = photoFragments.keySet();
        for (Integer key: keys){
            fm.beginTransaction().remove(photoFragments.get(key)).commit();
        }
        photoFragments.clear();
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


    private void drawForm(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = (int) (displaymetrics.heightPixels * PROP_HEIGHT);
        width = (int) (displaymetrics.widthPixels * PROP_WIDTH);

        if (form != null)
            form.removeAllViews();
        form = new LinearLayout(getActivity());
        form.setLayoutParams(layoutParams);
        form.setOrientation(LinearLayout.VERTICAL);

        Integer[] keys = model.getKeys();

        for (Integer k: keys){
            addToForm(k, model.getField(k));
        }
        view.updateView(form);
        handleActivityResult();
    }

    public void addToForm(int id, Field field){
        field.setId(id);
        field.setObserver(addFieldView(field));
        if (field.type == PHOTO) {
            addPhotoFragment(id, field);
        }
    }

    public void addNewField(Field field){
        int id = model.addField(field);
        addToForm(id, field);
        view.scrollToBottom();
    }


    private FieldView addFieldView(Field field) {

        FieldView fieldView = (FieldView) getActivity().getLayoutInflater().inflate(R.layout.fieldview, null, false);
        fieldView.init(getActivity(), this, field);
        fieldView.setTag(field.id);

        form.addView(fieldView);
        return fieldView;

    }

    private void addPhotoFragment(int containerViewId, Field field) {
        PhotoFragment photoFragment = photoFragments.get(containerViewId);
        if (photoFragment == null) {
            photoFragment = PhotoFragment.newInstance();
        }

        photoFragment.setListenerAndImageData(containerViewId, this, (PhotoField) field, width, height, model.documentId);
        photoFragments.put(containerViewId, photoFragment);
        fm.beginTransaction().replace(containerViewId, photoFragment).commit();
    }

    public LocationList refreshModel() {

        Integer[] keys = model.getKeys();
        for (Integer k : keys){
            FieldView fieldView = (FieldView) form.findViewWithTag(k);
            if (fieldView.getType() != PHOTO) {
                EntryField entryField = (EntryField) model.getField(k);
                entryField.entry = fieldView.getEntry();
            }
        }
        return model;
    }

    @Override
    public void editFieldTitle(int tag) {
        Field field = model.getField(tag);
        editTitleDialogFragment = EditTitleDialogFragment.newInstance(field, (model instanceof SecondaryList)? true : false);
        editTitleDialogFragment.show(fm, FRAGMENT_EDIT_FIELD_TITLE);
    }

    @Override
    public void deleteField(int tag) {
        model.removeField(tag); //remove from model
        FieldView fieldView = (FieldView) form.findViewWithTag(tag); //and from view
        int index = form.indexOfChild(fieldView);
        form.removeViewAt(index);
    }

    @Override
    public void entryTyped(int tag, String entry) {
        EntryField entryField = (EntryField) model.getField(tag);
        entryField.entry = entry;
    }

    @Override
    public void deleteImage(String imageName, String thumbName) {
        if (imageName != null && imageName.length() > 0) {
            DatabaseCommunicator db = DatabaseCommunicator.getInstance();
            db.deleteAttachment(model.documentId, imageName);
            db.deleteAttachment(model.documentId, thumbName);
        }
    }

    @Override
    public void removePhotoFragment(int id) {
        PhotoFragment photoFragment = photoFragments.remove(id);
        fm.beginTransaction().remove(photoFragment).commit();
        model.removeField(id);
    }
}
