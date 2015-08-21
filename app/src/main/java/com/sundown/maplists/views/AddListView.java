package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 7/15/2015.
 */
public class AddListView extends LinearLayout {

    private ScrollView scrollView;
    private LinearLayout container;
    public LinearLayout getContainer(){return container;}


    public AddListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        container = (LinearLayout) findViewById(R.id.add_list_container);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
    }

    public void updateView(LinearLayout layout){
        try { ((ViewGroup) layout.getParent()).removeView(layout); } catch (NullPointerException e){}
        container.addView(layout);
    }

    public void scrollToBottom(){
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

}
