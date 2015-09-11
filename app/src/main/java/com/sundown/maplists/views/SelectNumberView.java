package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 9/11/2015.
 */
public class SelectNumberView extends LinearLayout {

    private TextView seekBarText;
    private SeekBar seekBar;


    public SelectNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        seekBarText = (TextView) findViewById(R.id.seekBarText);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }
}
