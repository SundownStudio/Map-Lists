package com.sundown.maplists.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.views.EnterAddressView;

/**
 * Created by Sundown on 7/23/2015.
 */
public class EnterAddressDialogFragment extends DialogFragment {


    public interface EnterAddressListener{
        void onAddressAdded(String address);
    }

    private EnterAddressView view;
    private EnterAddressListener listener;
    public void setListener(EnterAddressListener listener){ this.listener = listener;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = (EnterAddressView) inflater.inflate(R.layout.dialog_enter_address, null);

        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.enter_address));
        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onAddressAdded(view.getEnteredText());
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


    @Override
    public void onDestroyView() {
        //needed due to a bug with the compatibility library but only if you do onRetainInstance
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }







}
