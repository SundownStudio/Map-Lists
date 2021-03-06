package com.sundown.maplists.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.fields.FieldFactory;
import com.sundown.maplists.views.AddFieldView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 4/28/2015.
 */
public class AddFieldDialogFragment extends DialogFragment {

    /**
     * the list of current fields
     */
    private List<Field> list;

    /**
     * the view listener
     */
    private AddFieldView.FieldSelector listener;
    public void setListener(AddFieldView.FieldSelector listener){ this.listener = listener; }

    public static AddFieldDialogFragment newInstance(AddFieldView.FieldSelector listener) {
        AddFieldDialogFragment fragment = new AddFieldDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    public AddFieldDialogFragment() {
    }

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

        AddFieldView view = (AddFieldView) inflater.inflate(R.layout.dialog_add_field, null);

        ArrayList<Integer> arr = new ArrayList<>();
        addTypedArrayToArrayList(getResources().obtainTypedArray(R.array.primary_field_images), arr);
        addTypedArrayToArrayList(getResources().obtainTypedArray(R.array.secondary_field_images), arr);
        view.setAdapter(listener, list, arr);

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
    private void generateList() {
        list = new ArrayList<>();
        String[] fieldNames = getResources().getStringArray(R.array.all_field_names);
        int len = fieldNames.length;
        int[] types = Field.getConstArray();
        for (int i = 0; i < len; ++i) {
            list.add(FieldFactory.createField(fieldNames[i], "", types[i], false));
        }
    }

    private void addTypedArrayToArrayList(TypedArray typedArray, ArrayList arrayList) {
        int len = typedArray.length();
        for (int i = 0; i < len; ++i) {
            arrayList.add(typedArray.getResourceId(i, -1));
        }
        typedArray.recycle();
    }
}