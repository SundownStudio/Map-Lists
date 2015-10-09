package com.sundown.maplists.views;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sundown.maplists.R;

import java.util.ArrayList;

/**
 * Created by Sundown on 10/7/2015.
 */
public class ToolbarWithSpinner extends Toolbar {

    public interface Listener {
        void onSchemaSelected(int position);
    }

    private Context context;
    private Spinner spinner;
    private Listener listener;


    public ToolbarWithSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        spinner = (Spinner) findViewById(R.id.spinner_schema);
    }


    public void setSpinner(ArrayList<String> list, int selectedIndex, final Listener listener){
        this.listener = listener;

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        spinner.setSelection(selectedIndex, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onSchemaSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public int getSelectedIndex(){
        return spinner.getSelectedItemPosition();
    }
}
