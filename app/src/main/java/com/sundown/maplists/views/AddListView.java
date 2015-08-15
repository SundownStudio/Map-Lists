package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;

/**
 * Created by Sundown on 7/15/2015.
 */
public class AddListView extends LinearLayout implements View.OnClickListener {


    public interface AddItemViewListener{
        void cancelPressed();
        void addFieldPressed();
        void enterPressed();
    }

    private AddItemViewListener listener;
    public void setListener(AddItemViewListener listener){this.listener = listener;}

    private LinearLayout container;
    public LinearLayout getContainer(){return container;}

    private Button cancel, enter, add;

    public AddListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        container = (LinearLayout) findViewById(R.id.add_list_container);

        cancel = (Button) findViewById(R.id.cancel);
        enter = (Button) findViewById(R.id.enter);
        add = (Button) findViewById(R.id.add);
        cancel.setOnClickListener(this);
        enter.setOnClickListener(this);
        add.setOnClickListener(this);
        Log.m("AddItemView finished inflating");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                listener.cancelPressed();
                break;
            case R.id.add:
                listener.addFieldPressed();
                break;
            case R.id.enter:
                listener.enterPressed();
                break;
        }
    }

    public void updateView(LinearLayout layout){
        try { ((ViewGroup) layout.getParent()).removeView(layout); } catch (NullPointerException e){}
        container.addView(layout);
    }

}
