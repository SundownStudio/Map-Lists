package com.sundown.maplists.utils;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;

import com.sundown.maplists.views.DoubleEditView;

import java.util.List;

/**
 * Created by Sundown on 9/17/2015.
 */
public class ViewUtils {

    public static String getStringFromEditText(EditText editText){
        String text = editText.getText().toString();
        if (text.length() == 0) {
            //nothing was entered..
            if (editText.getHint() != null) {
                text = editText.getHint().toString();
            }
        }
        return text.trim();
    }

    public static String getStringFromCheckBox(CheckBox checkBox){
        return String.valueOf(checkBox.isChecked() ? 1 : 0);
    }

    public static String getStringFromRatingBar(RatingBar ratingBar){
        return String.valueOf(ratingBar.getRating());
    }

    public static List<String> getStringsFromDoubleEditView(List<String> list, DoubleEditView doubleEditView){
        list.add(doubleEditView.getText(1));
        list.add(doubleEditView.getText(2));
        return list;
    }

    public static ShapeDrawable getTopRoundedCornersDrawable(float dimens, int color){
        ShapeDrawable roundedCornersDrawable = new ShapeDrawable();
        float[] rads = new float[8];
        rads[0] = dimens; //top-left x
        rads[1] = dimens; //top-left y
        rads[2] = dimens; //top-right x
        rads[3] = dimens; //top-right y
        roundedCornersDrawable.setShape(new RoundRectShape(rads, null, null));
        roundedCornersDrawable.getPaint().setColor(color);

        return roundedCornersDrawable;
    }

}
