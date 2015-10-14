package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 9/2/2015.
 */
public class ColorPickerView extends LinearLayout {

    private GradientView top;
    private GradientView bottom;
    private TextView colorText;



    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        top = (GradientView)findViewById(R.id.top);
        bottom = (GradientView)findViewById(R.id.bottom);
        top.setBrightnessGradientView(bottom);

        colorText = (TextView)findViewById(R.id.colorText);

        bottom.setOnColorChangedListener(new GradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(GradientView view, int color) {
                colorText.setTextColor(color);
                colorText.setText(String.format("#%06X", (0xFFFFFF & color)));
            }
        });
    }

    public String getColor(){
        return colorText.getText().toString();
    }

    public void setColor(int color){ top.setColor(color);}
}
