package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 7/21/2015.
 */
public class LocationListView extends RelativeLayout {

    private LinearLayout container;


    public LocationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        container = (LinearLayout) findViewById(R.id.itemViewContainer);
    }

    public void updateView(LinearLayout layout){
        try { ((ViewGroup) layout.getParent()).removeView(layout); } catch (NullPointerException e){}
        container.addView(layout);
    }

}
