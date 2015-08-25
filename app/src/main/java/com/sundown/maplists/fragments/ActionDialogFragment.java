package com.sundown.maplists.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.views.ConfirmActionView;

/**
 * Created by Sundown on 8/25/2015.
 */
public class ActionDialogFragment extends DialogFragment {

    public interface ConfirmActionListener {
        void confirmAction(boolean confirmed);
    }

    private final static String TEXT = "text";
    private final static String TITLE = "title";
    private ConfirmActionListener listener;

    public static ActionDialogFragment newInstance(String confirmTitle, String confirmText) {
        ActionDialogFragment fragment = new ActionDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, confirmTitle);
        args.putString(TEXT, confirmText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        listener = (ConfirmActionListener) getActivity();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ConfirmActionView view = (ConfirmActionView) inflater.inflate(R.layout.dialog_confirm_action, null);
        view.setText(getArguments().getString(TEXT));


        builder.setView(view);


        builder.setTitle(getArguments().getString(TITLE));
        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.confirmAction(true);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.confirmAction(false);
            }
        });

        return builder.create();

    }

    @Override
    public void onDestroyView() {
        //needed due to a bug with the compatibility library but only if you do onRetainInstance
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


}
