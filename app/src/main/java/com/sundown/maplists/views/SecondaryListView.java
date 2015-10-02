package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 7/21/2015.
 */
public class SecondaryListView extends RelativeLayout {

    private LinearLayout container;
    private TextView subject;

    public SecondaryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        container = (LinearLayout) findViewById(R.id.listViewContainer);
        subject = (TextView) findViewById(R.id.listViewSubject);
    }

    public void setSubject(ShapeDrawable roundedCornersDrawable, String subjectText){
        if (roundedCornersDrawable != null) subject.setBackgroundDrawable(roundedCornersDrawable);
        subject.setText(subjectText);
    }

    public void updateView(LinearLayout layout){
        try { ((ViewGroup) layout.getParent()).removeView(layout); } catch (NullPointerException e){}
        container.addView(layout);
    }

}
