package com.sundown.maplists.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.views.AddSchemaView;


/**
 * Created by Sundown on 8/21/2015.
 */
public class AddSchemaDialogFragment extends DialogFragment {

    public interface AddSchemaListener {
        void schemaAdded(String schema);
    }

    private final static String HINT = "hint";
    private AddSchemaListener listener;

    public static AddSchemaDialogFragment getInstance(String hint){
        AddSchemaDialogFragment fragment = new AddSchemaDialogFragment();
        Bundle args = new Bundle();
        args.putString(HINT, hint);
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
        listener = (AddSchemaListener) getActivity();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AddSchemaView view = (AddSchemaView) inflater.inflate(R.layout.dialog_add_schema, null);
        view.setHint(getArguments().getString(HINT));

        builder.setView(view);
        builder.setTitle(getString(R.string.save_schema));
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notice();
                dialog.dismiss();
                listener.schemaAdded(view.getEnteredText());
            }
        });
        builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.schemaAdded(null);
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

    private void notice(){
        Log.Toast(getActivity(), "Saving schemas not implemented yet", Log.TOAST_SHORT);
    }

}
