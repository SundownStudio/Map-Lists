package com.sundown.maplists.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.sundown.maplists.R;
import com.sundown.maplists.views.ColorPickerView;

/**
 * Created by Sundown on 9/2/2015.
 */
public class ColorPickerDialogFragment extends DialogFragment {

    public interface ColorPickerListener{
        void colorPicked(String color);
    }

    public static ColorPickerDialogFragment newInstance(ColorPickerListener listener, int color){
        ColorPickerDialogFragment frag = new ColorPickerDialogFragment();
        frag.listener = listener;
        frag.color = color;
        return frag;
    }

    private ColorPickerView view;
    private ColorPickerListener listener;
    private int color;




    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = (ColorPickerView) inflater.inflate(R.layout.dialog_color_picker, null);
        view.setColor(color);

        builder.setView(view);
        builder.setTitle(getString(R.string.pick_color));
        builder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.colorPicked(view.getColor());
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
