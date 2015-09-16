package com.sundown.maplists.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.views.SelectNumberView;

/**
 * Created by Sundown on 9/11/2015.
 */
public class SelectNumberDialogFragment extends DialogFragment {

    private final static String FIELD_TYPE_NAME = "fieldTypeName";

    public interface SelectNumberListener {
        void numberSelected(int number);
    }

    private SelectNumberView view;
    private SelectNumberListener listener;
    private FieldType type;

    public static SelectNumberDialogFragment newInstance(String fieldTypeName, FieldType fieldType, SelectNumberListener listener){
        SelectNumberDialogFragment fragment = new SelectNumberDialogFragment();
        Bundle args = new Bundle();
        args.putString(FIELD_TYPE_NAME, fieldTypeName);
        fragment.setArguments(args);
        fragment.type = fieldType;
        fragment.listener = listener;
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        int max = Integer.parseInt(getResources().getString(R.string.seekbar_max_regular));
        if (type == FieldType.LIST_ITEMS)
            max = Integer.parseInt(getResources().getString(R.string.seekbar_max_list_items));

        view = (SelectNumberView) inflater.inflate(R.layout.dialog_select_number, null);
        view.init(getArguments().getString(FIELD_TYPE_NAME), max);

        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.how_many));

        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.numberSelected(view.getProgress());
                dialog.dismiss();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        return builder.create();

    }
}
