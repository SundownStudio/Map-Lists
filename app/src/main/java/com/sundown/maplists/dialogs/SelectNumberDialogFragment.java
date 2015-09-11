package com.sundown.maplists.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.views.SelectNumberView;

/**
 * Created by Sundown on 9/11/2015.
 */
public class SelectNumberDialogFragment extends DialogFragment {

    private SelectNumberView view;

    public static SelectNumberDialogFragment newInstance(){
        return new SelectNumberDialogFragment();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = (SelectNumberView) inflater.inflate(R.layout.dialog_select_number, null);

        builder.setView(view);
        builder.setTitle("How many?");

        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
