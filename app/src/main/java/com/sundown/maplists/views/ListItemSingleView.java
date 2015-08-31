package com.sundown.maplists.views;

import android.content.Context;
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

    public void init(int resId, String text){
        image.setImageResource(resId);
        contents.setText(text);
    }
}
