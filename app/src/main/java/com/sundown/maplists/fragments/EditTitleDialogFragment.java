package com.sundown.maplists.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.views.EditTitleView;

/**
 * Created by Sundown on 7/7/2015.
 */
public class EditTitleDialogFragment extends DialogFragment {


    private EditTitleView view;
    private Field field;
    private boolean enableDisplayTitle;

    public static EditTitleDialogFragment newInstance(Field field, boolean enableDisplayTitle) {
        EditTitleDialogFragment frag = new EditTitleDialogFragment();
        frag.field = field;
        frag.enableDisplayTitle = enableDisplayTitle;
        return frag;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = (EditTitleView) inflater.inflate(R.layout.dialog_edit_title, null);
        view.setTitle(field.title);
        view.showCheckBox(enableDisplayTitle);

        builder.setView(view);
        builder.setTitle(R.string.edit_title);
        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                field.setTitle(view.getTitle());
                field.setShowTitle(view.getChecked());
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
