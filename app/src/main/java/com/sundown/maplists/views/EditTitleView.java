package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 7/7/2015.
 */
public class EditTitleView extends LinearLayout {


    private EditText editTitleText;


    public EditTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editTitleText = (EditText) findViewById(R.id.editTitleText);
    }

    public void setTitle(String title){
        editTitleText.setHint(title);
    }

    public String getTitle(){
        return String.valueOf(editTitleText.getText());
    }


}
