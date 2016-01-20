package com.sundown.maplists.views;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sundown.maplists.Constants;
import com.sundown.maplists.R;

/**
 * Created by Sundown on 9/11/2015.
 */
public class SelectNumberView extends RelativeLayout {

    private TextView seekBarIntroText;
    private TextView seekBarNumberText;
    private TextView seekBarMinText;
    private TextView seekBarMaxText;
    private SeekBar seekBar;
    private ImageButton seekBarSubtract;
    private ImageButton seekBarAdd;
    private Handler handler = new Handler();

    private Runnable minusTask = new Runnable(){
        public void run(){
            int progress = seekBar.getProgress();
            if (progress != 0)
                progress--;

            setProgress(progress);
            handler.postAtTime(this, SystemClock.uptimeMillis() + 100);
        }
    };

    private Runnable plusTask = new Runnable(){
        public void run(){
            int progress = seekBar.getProgress();
            if (progress != max)
                progress++;

            setProgress(progress);
            handler.postAtTime(this, SystemClock.uptimeMillis() + 100);
        }
    };

    private String type;
    private int max;

    public SelectNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        seekBarIntroText = (TextView) findViewById(R.id.seekBarIntroText);
        seekBarNumberText = (TextView) findViewById(R.id.seekBarNumberText);
        seekBarMinText = (TextView) findViewById(R.id.seekBarMinText);
        seekBarMaxText = (TextView) findViewById(R.id.seekBarMaxText);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBarSubtract = (ImageButton) findViewById(R.id.seekBarSubtract);
        seekBarAdd = (ImageButton) findViewById(R.id.seekBarAdd);

    }

    public void init(String type, int myMax){
        this.type = type;
        seekBarIntroText.setText(getResources().getString(R.string.select_number_instr_1) + " " + type + "s " + getResources().getString(R.string.select_number_instr_2));
        seekBarMinText.setText(Constants.SEEKBAR_MIN +"");
        seekBarMaxText.setText(myMax +"");
        this.max = myMax - 1;
        seekBar.setMax(max);
        setSeekBarNumberText(0);

        seekBarSubtract.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    handler.removeCallbacks(minusTask);
                    handler.postAtTime(minusTask, SystemClock.uptimeMillis() + 100);
                } else if (action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(minusTask);
                }
                return false;
            }
        });

        seekBarAdd.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    handler.removeCallbacks(plusTask);
                    handler.postAtTime(plusTask, SystemClock.uptimeMillis() + 100);
                } else if (action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(plusTask);
                }
                return false;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarNumberText(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void setProgress(int progress){
        seekBar.setProgress(progress);
        setSeekBarNumberText(progress);
    }

    public int getProgress(){
        return seekBar.getProgress();
    }

    private void setSeekBarNumberText(int progress){
        progress+= 1;
        char s = ' ';
        if (progress > 1)
            s = 's';

        seekBarNumberText.setText("Adding: " + progress + " " + type + s); //todo use a spannable and add html font color to progress..
    }

}
