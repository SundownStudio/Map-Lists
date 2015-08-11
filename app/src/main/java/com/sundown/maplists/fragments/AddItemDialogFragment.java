package com.sundown.maplists.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.extras.Operation;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.Item;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.views.AddFieldView;
import com.sundown.maplists.views.AddItemView;
import com.sundown.maplists.views.FieldView;

import java.util.HashMap;
import java.util.Set;

import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_PIC;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_EDIT_FIELD_TITLE;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_SELECT_FIELD;

/**
 * Created by Sundown on 7/15/2015.
 */
public class AddItemDialogFragment extends DialogFragment implements AddItemView.AddItemViewListener,
        AddFieldView.FieldSelector, FieldView.FieldViewListener, PhotoFragment.PhotoFragmentListener{



    public interface AddItemListener {
        void itemAdded(Item item, Operation operation);
    }


    private FragmentManager fm;
    private AddFieldDialogFragment addFieldDialogFragment;
    private EditTitleDialogFragment editTitleDialogFragment;

    private AddItemListener listener;
    private AddItemView view;
    private HashMap<Integer, PhotoFragment> photoFragments = new HashMap<>();
    private Item model;
    private LinearLayout layout;
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private String title;
    private Operation operation;
    private int width, height;
    private static boolean setupCalled;


    public static AddItemDialogFragment newInstance(Item model, String title, Operation operation) {
        AddItemDialogFragment fragment = new AddItemDialogFragment();
        fragment.model = model;
        fragment.title = title;
        fragment.operation = operation;
        return fragment;
    }

    public AddItemDialogFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override //so if user rotates in camera/gallery, upon returning to this app THIS method gets called BEFORE the main activity's onResult... however
    //if user DOESNT rotate in camera/gallery, then this doesn't get called AT ALL..
    public View onCreateView(final LayoutInflater inflater, final ViewGroup viewGroup, Bundle savedInstanceState) {
        view = (AddItemView) inflater.inflate(R.layout.dialog_add_item, viewGroup);
        view.setListener(this);
        listener = (AddItemListener) getActivity();
        fm = getChildFragmentManager();
        setupCalled = false;

        getDialog().setTitle(title);
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setup();
            }
        });
        Log.m("AddItemDialogFragment finished onCreateView");
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        if (height > 0 && width > 0) { //we only want to init photofrags if height/width is set, otherwise let the onShowListener call it cuz height/width is never 0 then
            setup();
        }
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
        setupCalled = false;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        //needed due to a bug with the compatibility library but only if you do onRetainInstance
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    @Override
    public void dismiss() {
        photoFragments.clear();
        super.dismiss();
    }

    //called by main activity, hand these results off to each photofragment for potential processing
    public void handleActivityResults(ActivityResult result){
        PreferenceManager preferenceManager = PreferenceManager.getInstance();
        int callingId = preferenceManager.getInt(PhotoFragment.FRAGMENT_ID);
        photoFragments.get(callingId).onActivityResult(result.requestCode, result.resultCode, result.data);
    }


    private void setup(){
        if (!setupCalled) {
            setupCalled = true;

            if (height == 0) {height = (int) (getDialog().getWindow().getDecorView().getHeight() * Constants.SPECS.PROP_HEIGHT);}
            if (width == 0) {width = (int) (getDialog().getWindow().getDecorView().getWidth() * Constants.SPECS.PROP_WIDTH);}

            initLayout();
        }
    }


    private void initLayout(){

        if (layout != null)
            layout.removeAllViews();
        layout = new LinearLayout(getActivity());
        layout.setLayoutParams(layoutParams);
        layout.setOrientation(LinearLayout.VERTICAL);

        Integer[] keys = model.getKeys();

        for (Integer k: keys){
            addToLayout(k, model.getField(k));
        }
        view.updateView(layout);
    }

    private void addToLayout(int id, Field field){
        field.setId(id);
        field.setObserver(addFieldView(field));
        if (field.type == FIELD_PIC) {
            addPhotoFragment(id, field);
        }
    }

    private FieldView addFieldView(Field field) {

        FieldView fieldView = (FieldView) getActivity().getLayoutInflater().inflate(R.layout.fieldview, null, false);
        fieldView.init(getActivity(), this, field);
        fieldView.setTag(field.id);

        layout.addView(fieldView);
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

    //VIEW BUTTONS

    @Override
    public void cancelPressed(){
        listener.itemAdded(null, Operation.NOTHING);
        dismiss();
    }

    @Override
    public void addFieldPressed(){
        addFieldDialogFragment = AddFieldDialogFragment.newInstance(this);
        addFieldDialogFragment.show(fm, FRAGMENT_SELECT_FIELD);
    }

    @Override
    public void enterPressed(){
        listener.itemAdded(refreshModel(), operation);
        dismiss();
    }

    @Override
    public void addNewField(Field field) {
        addFieldDialogFragment.dismiss();
        int id = model.addField(field); //todo put listener on model to addToLayout whenever new entry is added..
        addToLayout(id, field);
    }

    @Override
    public void editFieldTitle(int tag) {
        Field field = model.getField(tag);
        editTitleDialogFragment = EditTitleDialogFragment.newInstance(field);
        editTitleDialogFragment.show(fm, FRAGMENT_EDIT_FIELD_TITLE);

    }

    @Override
    public void deleteField(int tag) {
        model.removeField(tag); //remove from model
        FieldView fieldView = (FieldView) layout.findViewWithTag(tag); //and from view
        int index = layout.indexOfChild(fieldView);
        layout.removeViewAt(index);

    }

    @Override
    public void entryTyped(int tag, String entry) {
        EntryField entryField = (EntryField) model.getField(tag);
        entryField.entry = entry;
        Log.m("Entry arrived: " + entry);
    }


    //why? because TextWatcher is expensive if updating model every time char is typed.. and OnFocusChangeListener may not always trigger if
    //focus doesnt leave before button pressed
    private Item refreshModel() {

        Integer[] keys = model.getKeys();
        for (Integer k : keys){
            FieldView fieldView = (FieldView) layout.findViewWithTag(k);
            if (fieldView.getType() != FIELD_PIC) {
                EntryField entryField = (EntryField) model.getField(k);
                entryField.entry = fieldView.getEntry();
            }
        }
        return model;
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
