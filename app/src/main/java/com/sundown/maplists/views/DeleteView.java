package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 5/21/2015.
 */
public class DeleteView extends RelativeLayout {


    private TextView textView;


    public DeleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        textView = (TextView) findViewById(R.id.delete_confirm_text);
    }

    public void setText(String text){
        textView.setText(text);
    }

}
