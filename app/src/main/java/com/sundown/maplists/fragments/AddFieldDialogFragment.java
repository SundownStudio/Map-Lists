package com.sundown.maplists.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.views.AddFieldView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 4/28/2015.
 */
public class AddFieldDialogFragment extends DialogFragment {

    /** the list of current fields */
    private List<Field> list;

    /** the view listener */
    private AddFieldView.FieldSelector listener;


    public static AddFieldDialogFragment newInstance(AddFieldView.FieldSelector listener){
        AddFieldDialogFragment fragment = new AddFieldDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    public AddFieldDialogFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generateList();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AddFieldView view = (AddFieldView) inflater.inflate(R.layout.dialog_select_field, null);
        view.setAdapter(listener, list, getResources().obtainTypedArray(R.array.add_field_images));

        builder.setView(view);
        builder.setTitle(getString(R.string.add_field));
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        return builder.create();

    }


    //needed due to a bug with the compatibility library but only if you do onRetainInstance
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    /**
     * Generate the list of potential fields for this view
     */
    private void generateList(){
        list = new ArrayList<>();
        String[] fieldNames = getResources().getStringArray(R.array.add_field_names);
        int len = fieldNames.length;
        FieldType[] types = FieldType.values();
        for (int i = 0; i < len; ++i){
            list.add(new EntryField(-1, fieldNames[i], "", types[i], false));
        }
    }

}