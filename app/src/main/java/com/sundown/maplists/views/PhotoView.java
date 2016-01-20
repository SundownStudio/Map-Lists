package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;



/**
 * Created by Sundown on 5/21/2015.
 */
public class PhotoView extends RelativeLayout implements View.OnClickListener {

    public interface PhotoViewListener{
        void takePicture();
        void deletePicture(boolean clearFiles);
        void loadPicture();
        void rotatePicture();
        void deleteFragment();
    }

    private static final int SHOW_PROGRESS = 0;
    private static final int CLEAR_PROGRESS = 1;
    private static final int NO_IMAGE_LOADED = 2;
    private static final int CANT_LOAD_IMAGE = 3;
    private static final int CLEAR_CONTAINER = 4;
    private static final int NULL_BITMAP = 5;

    private PhotoViewListener listener;
    private ImageView locationImage;
    private ProgressBar progressBar;
    private RelativeLayout imageContainer;
    private ImageButton takePicture, deletePicture, loadPicture, rotatePicture, removeFragment;
    private TextView noImageLoadedText;



    public PhotoView(Context context, AttributeSet attrs) {super(context, attrs);}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setup();
    }

    public void setup(){

        noImageLoadedText = (TextView) findViewById(R.id.noImageLoadedText);
        locationImage = (ImageView) findViewById(R.id.locationImage);
        locationImage.setLayerType(LAYER_TYPE_SOFTWARE, null); //TADA.. disable hardware acceleration bitmap views so they actually get cleared when you recycle.. if you cant retain instance which we cant in nested frag..
        //this affects SDKs after 3.0 all differently.. unbelievable how there is no documentation on clearing bitmaps in nested fragments on configuration changes..
        progressBar = (ProgressBar) findViewById(R.id.imgProgress);
        imageContainer = (RelativeLayout) findViewById(R.id.imageContainer);
        takePicture = (ImageButton) findViewById(R.id.takePicture);
        deletePicture = (ImageButton) findViewById(R.id.deletePicture);
        loadPicture = (ImageButton) findViewById(R.id.loadPicture);
        rotatePicture = (ImageButton) findViewById(R.id.rotatePicture);
        removeFragment = (ImageButton) findViewById(R.id.removeFragment);
        takePicture.setOnClickListener(this);
        deletePicture.setOnClickListener(this);
        loadPicture.setOnClickListener(this);
        rotatePicture.setOnClickListener(this);
        removeFragment.setOnClickListener(this);
    }

    public void setListener(PhotoViewListener listener) { this.listener = listener;}

    public void loadingImage(){
        drawContainer(SHOW_PROGRESS, CLEAR_CONTAINER);
    }

    public void reset(){
        drawContainer(CLEAR_PROGRESS, NO_IMAGE_LOADED, NULL_BITMAP);
    }

    public void makePermanent(){removeFragment.setVisibility(INVISIBLE);}

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.takePicture:
                drawContainer(SHOW_PROGRESS, CLEAR_CONTAINER);
                listener.takePicture();
                break;

            case R.id.deletePicture:
                dispose();
                drawContainer(CLEAR_PROGRESS, NO_IMAGE_LOADED, NULL_BITMAP);
                listener.deletePicture(true);
                break;

            case R.id.loadPicture:
                dispose();
                drawContainer(SHOW_PROGRESS, CLEAR_CONTAINER);
                listener.loadPicture();
                break;

            case R.id.rotatePicture:
                if (locationImage.getDrawable() != null) {
                    dispose();
                    drawContainer(SHOW_PROGRESS, CLEAR_CONTAINER);
                    listener.rotatePicture();
                }
                break;

            case R.id.removeFragment:
                listener.deleteFragment();
                break;

        }
    }


    private void drawContainer(int... options){
        int len = options.length;

        for (int i = 0; i < len; ++i) {
            switch (options[i]) {
                case SHOW_PROGRESS:{
                    progressBar.setVisibility(VISIBLE);
                    break;}

                case CLEAR_PROGRESS:{
                    progressBar.setVisibility(GONE);
                    break;}

                case NO_IMAGE_LOADED:{
                    resetContainer(getResources().getString(R.string.no_image_loaded));
                    break;}

                case CANT_LOAD_IMAGE:{
                    resetContainer(getResources().getString(R.string.couldnt_load_image));
                    break;}

                case CLEAR_CONTAINER:{
                    imageContainer.setBackgroundColor(getResources().getColor(R.color.dialogBackground));
                    noImageLoadedText.setVisibility(GONE);
                    break;}

                case NULL_BITMAP:{
                    locationImage.setImageDrawable(null);
                    break;}
            }
        }

    }

    private void resetContainer(String text){
        noImageLoadedText.setText(text);
        noImageLoadedText.setVisibility(VISIBLE);
        imageContainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }


    public void setBitmap(Bitmap bitmap){
        if (bitmap == null){
            drawContainer(CLEAR_PROGRESS, CANT_LOAD_IMAGE);
        } else {
            drawContainer(CLEAR_PROGRESS, CLEAR_CONTAINER);
            locationImage.setImageBitmap(bitmap);
            imageContainer.bringToFront();
        }
    }

    public void dispose(){
        locationImage.setImageBitmap(null);
    }

    public void disableAllButtons(){
        takePicture.setOnClickListener(null);
        deletePicture.setOnClickListener(null);
        loadPicture.setOnClickListener(null);
        rotatePicture.setOnClickListener(null);
        removeFragment.setOnClickListener(null);
        takePicture.setVisibility(GONE);
        deletePicture.setVisibility(GONE);
        loadPicture.setVisibility(GONE);
        rotatePicture.setVisibility(GONE);
        removeFragment.setVisibility(GONE);
    }
}
