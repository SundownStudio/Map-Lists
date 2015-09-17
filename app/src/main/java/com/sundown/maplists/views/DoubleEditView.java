package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.utils.ViewUtils;

/**
 * Created by Sundown on 9/17/2015.
 */
public class DoubleEditView extends LinearLayout {

    private EditText editText1;
    private EditText editText2;

    public DoubleEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editText1 = (EditText)findViewById(R.id.editText1);
        editText2 = (EditText)findViewById(R.id.editText2);
    }

    private EditText getEditText(int num){
        if (num == 2){
            return editText2;
        }
        return editText1;
    }

    public void setHint(int num, String text){
        getEditText(num).setHint(text);
    }

    public String getText(int num){
        return ViewUtils.getStringFromEditText(getEditText(num));
    }
}
