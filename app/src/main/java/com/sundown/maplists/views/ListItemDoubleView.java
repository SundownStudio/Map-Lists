package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 8/31/2015.
 */
public class ListItemDoubleView extends LinearLayout {

    private ImageView imageOne;
    private TextView contentsOne;
    private ImageView imageTwo;
    private TextView contentsTwo;
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    public ListItemDoubleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        imageOne = (ImageView) findViewById(R.id.listItemImageOne);
        contentsOne = (TextView) findViewById(R.id.listItemContentsOne);
        imageTwo = (ImageView) findViewById(R.id.listItemImageTwo);
        contentsTwo = (TextView) findViewById(R.id.listItemContentsTwo);
    }

    public void initAsTitle(String textOne, String textTwo){
        imageOne.setVisibility(GONE);
        imageTwo.setVisibility(GONE);
        contentsOne.setTypeface(null, Typeface.BOLD);
        contentsTwo.setTypeface(null, Typeface.BOLD);
        contentsOne.setText(textOne);
        contentsTwo.setText(textTwo);
        layoutParams.setMargins(10, 20, 10, 0);
        this.setLayoutParams(layoutParams);
    }

    public void initAsEntry(int resIdOne, int resIdTwo, String textOne, String textTwo, boolean addTopMargin){
        imageOne.setVisibility(VISIBLE);
        imageTwo.setVisibility(VISIBLE);
        imageOne.setImageResource(resIdOne);
        imageTwo.setImageResource(resIdTwo);
        contentsOne.setTypeface(null, Typeface.NORMAL);
        contentsTwo.setTypeface(null, Typeface.NORMAL);
        contentsOne.setText(textOne);
        contentsTwo.setText(textTwo);
        if (addTopMargin)
            layoutParams.setMargins(20, 20, 20, 0);
        else
            layoutParams.setMargins(20, 0, 20, 0);
        this.setLayoutParams(layoutParams);
    }

}
