package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.utils.ViewUtils;

/**
 * Created by Sundown on 7/7/2015.
 */
public class EditTitleView extends LinearLayout {


    private EditText editTitleText;
    private CheckBox checkBox;


    public EditTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editTitleText = (EditText) findViewById(R.id.editTitleText);
        checkBox = (CheckBox) findViewById(R.id.editTitleShow);
    }

    public void setTitle(String title){
        editTitleText.setHint(title);
    }

    public String getTitle(){
        return ViewUtils.getStringFromEditText(editTitleText);
    }

    public boolean getChecked() { return checkBox.isChecked();}

    public void showCheckBox(boolean enableDisplayTitle, boolean isChecked){
        if (enableDisplayTitle){
            checkBox.setVisibility(VISIBLE);
        } else {
            checkBox.setVisibility(GONE);
        }
        checkBox.setChecked(isChecked);
    }


}
