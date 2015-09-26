package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 8/31/2015.
 */
public class ListItemDoubleView extends TableLayout { //TODO this should really extend TableRow.. if we end up keeping this design then fix this cuz right now Im not sure and pressed for time

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


    public void init(Integer resIdOne, Integer resIdTwo, String textOne, String textTwo){
        if (resIdOne == null){
            imageOne.setVisibility(GONE);
        } else {
            imageOne.setVisibility(VISIBLE);
            imageOne.setImageResource(resIdOne);
        }
        if (resIdTwo == null){
            imageTwo.setVisibility(GONE);
        } else {
            imageTwo.setVisibility(VISIBLE);
            imageTwo.setImageResource(resIdTwo);
        }
        contentsOne.setTypeface(null, Typeface.NORMAL);
        contentsTwo.setTypeface(null, Typeface.NORMAL);
        contentsOne.setText(Html.fromHtml(textOne));
        contentsTwo.setText(Html.fromHtml(textTwo));
        layoutParams.setMargins(20, 0, 20, 0);
        this.setLayoutParams(layoutParams);
    }
}
