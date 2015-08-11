package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sundown.maplists.R;
import com.sundown.maplists.adapters.AdapterGooglePlaces;

/**
 * Created by Sundown on 7/23/2015.
 */
public class EnterAddressView extends RelativeLayout implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView autoCompleteTextView;
    public String getEnteredText(){ return autoCompleteTextView.getText().toString().trim();}


    public EnterAddressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.enter_address_text);
        autoCompleteTextView.setAdapter(new AdapterGooglePlaces(getContext(), R.layout.list_address));
        autoCompleteTextView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String str = (String) parent.getItemAtPosition(position);
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }




}
