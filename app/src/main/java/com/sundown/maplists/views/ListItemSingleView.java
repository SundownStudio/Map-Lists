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
public class ListItemSingleView extends LinearLayout{

    private ImageView image;
    private TextView contents;
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


    public ListItemSingleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        image = (ImageView) findViewById(R.id.listItemImage);
        contents = (TextView) findViewById(R.id.listItemContents);
    }

    public void initAsTitle(String title){
        image.setVisibility(GONE);
        contents.setTypeface(null, Typeface.BOLD);
        contents.setText(title);
        layoutParams.setMargins(10,20,10,0);
        this.setLayoutParams(layoutParams);
    }

    public void initAsEntry(int resId, String text, boolean addTopMargin){
        image.setVisibility(VISIBLE);
        image.setImageResource(resId);
        contents.setTypeface(null, Typeface.NORMAL);
        contents.setText(text);
        if (addTopMargin)
            layoutParams.setMargins(20, 20, 20, 0);
        else
            layoutParams.setMargins(20, 0, 20, 0);
        this.setLayoutParams(layoutParams);
    }

    public void initAsComment(String comment){
        image.setVisibility(GONE);
        contents.setTypeface(null, Typeface.NORMAL);
        contents.setText(comment);
        layoutParams.setMargins(10, 20, 10,0);
        this.setLayoutParams(layoutParams);
    }
}
