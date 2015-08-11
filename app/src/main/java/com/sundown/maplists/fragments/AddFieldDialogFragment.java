package com.sundown.maplists.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.views.AddFieldView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 4/28/2015.
 */
public class AddFieldDialogFragment extends DialogFragment {


    private List<Field> list;
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
        Log.m("SelectFieldDialogFragment onCreate");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        AddFieldView view = (AddFieldView) inflater.inflate(R.layout.dialog_select_field, null);
        view.setAdapter(listener, list);
        builder.setView(view);
        builder.setTitle("Add Field");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        Log.m("SelectFieldDialogFragment returning builder.create");
        return builder.create();

    }


    //supposedly needed due to a bug with the compatibility library but only if you do onRetainInstance
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    private void generateList(){
        list = new ArrayList<>();
        String[] fieldNames = getResources().getStringArray(R.array.add_field_names);
        int len = fieldNames.length;
        for (int i = 0; i < len; ++i){
            list.add(new EntryField(-1, fieldNames[i], "", i, false));
        }
    }

}