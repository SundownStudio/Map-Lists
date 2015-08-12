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
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.List;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.views.AddFieldView;
import com.sundown.maplists.views.AddListView;
import com.sundown.maplists.views.FieldView;

import java.util.HashMap;
import java.util.Set;

import static com.sundown.maplists.extras.Constants.FIELDS.FIELD_PIC;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_EDIT_FIELD_TITLE;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_SELECT_FIELD;

/**
 * Created by Sundown on 7/15/2015.
 */
public class AddListDialogFragment extends DialogFragment implements AddListView.AddItemViewListener,
        AddFieldView.FieldSelector, FieldView.FieldViewListener, PhotoFragment.PhotoFragmentListener{


    public interface AddListListener {
        void listAdded(List list, Operation operation);
    }


    private FragmentManager fm;

    /** Fragment for select a new field to add onto this list */
    private AddFieldDialogFragment addFieldDialogFragment;

    /** Fragment for editing the title of a current field in this list */
    private EditTitleDialogFragment editTitleDialogFragment;

    /** serves as a container for our photo fragments */
    private HashMap<Integer, PhotoFragment> photoFragments = new HashMap<>();

    /** the listener for this fragment */
    private AddListListener listener;
    public void setListener(AddListListener listener){ this.listener = listener;}

    /** the view for this fragment */
    private AddListView view;

    /** our model, can either be a MapList or a LocationList */
    private List model;

    /** the form of fields used to populate our view */
    private LinearLayout form;
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    /** the title for this dialog */
    private String title;

    /** the operation to be performed once the user has accepted modifications to this list */
    private Operation operation;

    /** the width/height of this dialog fragment */
    private int width, height;

    /** denotes whether view has been shown yet and thus whether screen-dimensions already defined  */
    private static boolean setupCalled;

    /**
     * Always create a new instance this way, leave the empty public constructor
     *
     * @param model the current list be displayed as a form
     * @param title for this dialog
     * @param operation to be performed upon the DB after user modifies list
     * */
    public static AddListDialogFragment newInstance(List model, String title, Operation operation) {
        AddListDialogFragment fragment = new AddListDialogFragment();
        fragment.model = model;
        fragment.title = title;
        fragment.operation = operation;
        return fragment;
    }

    /** NEVER CALL THIS */
    public AddListDialogFragment(){}



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup viewGroup, Bundle savedInstanceState) {

        //NOTE: if user rotates in camera/gallery, upon returning to our app THIS method gets called BEFORE the main activity's onResult... however
        //if user DOESNT rotate in camera/gallery, then this doesn't get called AT ALL..

        setRetainInstance(true);
        fm = getChildFragmentManager();
        listener = (AddListListener) getActivity();

        view = (AddListView) inflater.inflate(R.layout.dialog_add_list, viewGroup);
        view.setListener(this);
        setupCalled = false;

        getDialog().setTitle(title);
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setup();
            }
        });

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

    /**
     * Called by main activity, hand these results off to the relevant photo-fragment
     *
     * @param result results either from camera or gallery
     */
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

            drawForm();
        }
    }


    private void drawForm(){

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
    }

    private void addToForm(int id, Field field){
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

    //VIEW BUTTONS

    @Override
    public void cancelPressed(){
        listener.listAdded(null, Operation.NOTHING);
        dismiss();
    }

    @Override
    public void addFieldPressed(){
        addFieldDialogFragment = AddFieldDialogFragment.newInstance(this);
        addFieldDialogFragment.show(fm, FRAGMENT_SELECT_FIELD);
    }

    @Override
    public void enterPressed(){
        listener.listAdded(refreshModel(), operation);
        dismiss();
    }

    @Override
    public void addNewField(Field field) {
        addFieldDialogFragment.dismiss();
        int id = model.addField(field);
        addToForm(id, field);
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
        FieldView fieldView = (FieldView) form.findViewWithTag(tag); //and from view
        int index = form.indexOfChild(fieldView);
        form.removeViewAt(index);

    }

    @Override
    public void entryTyped(int tag, String entry) {
        EntryField entryField = (EntryField) model.getField(tag);
        entryField.entry = entry;
    }


    //why refresh all at once upon leaving the fragment? why not update it continually along the way?
    //because TextWatcher is expensive if updating model every time char is typed.. and OnFocusChangeListener may not always trigger if
    //focus doesnt leave it before button pressed to leave fragment
    private List refreshModel() {

        Integer[] keys = model.getKeys();
        for (Integer k : keys){
            FieldView fieldView = (FieldView) form.findViewWithTag(k);
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
