package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
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
    }

    public void initAsEntry(int resId, String text){
        image.setVisibility(VISIBLE);
        image.setImageResource(resId);
        contents.setTypeface(null, Typeface.NORMAL);
        contents.setText(text);
    }

    public void initAsComment(String comment){
        image.setVisibility(GONE);
        contents.setTypeface(null, Typeface.NORMAL);
        contents.setText(comment);
    }
}
