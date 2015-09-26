package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 8/31/2015.
 */
public class ListItemSingleView extends RelativeLayout {

    private ImageView image;
    private TextView contents;

    /** note this is necessary even though we now using RelativeLayout */
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


    public void init(Integer resId, String text){
        if (resId == null) { //init without image
            image.setVisibility(GONE);
            layoutParams.setMargins(10, 0, 10, 0);

        } else if (resId == 0){ //init as title without image
            image.setVisibility(GONE);
            contents.setTypeface(null, Typeface.BOLD);
            layoutParams.setMargins(10, 25, 10, 0);

        } else { //init as regular text with image
            image.setVisibility(VISIBLE);
            image.setImageResource(resId);
            contents.setTypeface(null, Typeface.NORMAL);
            layoutParams.setMargins(20, 0, 20, 0);
        }
        contents.setText(Html.fromHtml(text));
        this.setLayoutParams(layoutParams);
    }

}
