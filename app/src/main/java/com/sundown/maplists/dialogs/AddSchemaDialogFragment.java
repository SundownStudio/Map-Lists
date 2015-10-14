package com.sundown.maplists.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.views.AddSchemaView;


/**
 * Created by Sundown on 8/21/2015.
 */
public class AddSchemaDialogFragment extends DialogFragment {

    public interface AddSchemaListener {
        void schemaAdded(Operation operation, String schemaName, int indexToUpdate);
    }

    private final static String HINT = "hint";
    private final static String MESSAGE = "message";
    private AddSchemaListener listener;
    private Operation operation;
    private int indexToUpdate; //the index to update, used only for updates //todo refactor

    public static AddSchemaDialogFragment getInstance(Operation operation, String message, String hint, int indexToUpdate){
        AddSchemaDialogFragment fragment = new AddSchemaDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        args.putString(HINT, hint);
        fragment.setArguments(args);
        fragment.operation = operation;
        fragment.indexToUpdate = indexToUpdate;
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
        listener = (AddSchemaListener) getActivity();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AddSchemaView view = (AddSchemaView) inflater.inflate(R.layout.dialog_add_schema, null);
        view.setHint(getArguments().getString(HINT));
        view.setMessage(getArguments().getString(MESSAGE));
        String title = getString(R.string.save_schema);
        if (operation == Operation.UPDATE){
            title = getString(R.string.schema_detected);
        }

        builder.setView(view);
        builder.setTitle(title);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (operation == Operation.UPDATE){
                    if (!getArguments().getString(HINT).equals(view.getEnteredText())){ //we were gonna update but user entered a new name so insert under new name
                        operation = Operation.INSERT;
                    }
                }
                dialog.dismiss();
                listener.schemaAdded(operation, view.getEnteredText(), indexToUpdate);
            }
        });
        builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.schemaAdded(operation, null, indexToUpdate);
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
