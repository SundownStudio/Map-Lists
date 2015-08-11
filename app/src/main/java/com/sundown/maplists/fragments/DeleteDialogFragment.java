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
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Item;
import com.sundown.maplists.views.DeleteView;

/**
 * Created by Sundown on 4/17/2015.
 */
public class DeleteDialogFragment extends DialogFragment {

    public interface ConfirmDeleter {
        void confirmDelete(Item item);
    }

    private final static String TEXT = "text";
    private ConfirmDeleter confirmDeleter;
    private Item item;


    public static DeleteDialogFragment newInstance(Item item, String confirmText) {
        DeleteDialogFragment d = new DeleteDialogFragment();
        EntryField entryField = (EntryField) item.getField(0);

        Bundle args = new Bundle();
        args.putString(TEXT, entryField.entry + " " + confirmText);
        d.setArguments(args);
        d.item = item;
        return d;
    }

    //EMPTY CONSTRUCTOR REQUIRED!!!
    public DeleteDialogFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.m("DeleteDialogFragment onCreate");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        confirmDeleter = (ConfirmDeleter) getActivity();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        DeleteView view = (DeleteView) inflater.inflate(R.layout.dialog_confirm_delete, null);
        view.setText(getArguments().getString(TEXT));

        builder.setView(view);
        builder.setTitle("Delete Location");
        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                confirmDeleter.confirmDelete(item);
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
