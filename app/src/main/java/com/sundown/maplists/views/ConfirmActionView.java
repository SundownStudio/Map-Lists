package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 8/25/2015.
 */
public class ConfirmActionView extends LinearLayout {


    private TextView textView;


    public ConfirmActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        textView = (TextView) findViewById(R.id.confirm_action_text);
    }

    public void setText(String text){
        textView.setText(text);
    }

}
