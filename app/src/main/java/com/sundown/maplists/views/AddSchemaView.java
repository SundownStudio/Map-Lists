package com.sundown.maplists.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 8/21/2015.
 */
public class AddSchemaView extends LinearLayout {

    private TextView messageText;
    private EditText editText;

    public AddSchemaView(Context context, AttributeSet attrs) {super(context, attrs);}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        messageText = (TextView) findViewById(R.id.add_schema_message);
        editText = (EditText) findViewById(R.id.add_schema_name);
    }

    public void setHint(String hint){
        editText.setHint(hint);
    }
    public void setMessage(String message) { messageText.setText(message);
    }


    public String getEnteredText(){
        String text = String.valueOf(editText.getText()).trim();
        if (text.length() == 0) {
            if (editText.getHint() != null) {
                text = editText.getHint().toString().trim();
            }
        }
        return text;
    }

}
