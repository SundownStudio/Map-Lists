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
import com.sundown.maplists.logging.Log;

import static com.sundown.maplists.views.PhotoView.Option.CANT_LOAD_IMAGE;
import static com.sundown.maplists.views.PhotoView.Option.CLEAR_CONTAINER;
import static com.sundown.maplists.views.PhotoView.Option.CLEAR_PROGRESS;
import static com.sundown.maplists.views.PhotoView.Option.NO_IMAGE_LOADED;
import static com.sundown.maplists.views.PhotoView.Option.NULL_BITMAP;
import static com.sundown.maplists.views.PhotoView.Option.SHOW_PROGRESS;

/**
 * Created by Sundown on 5/21/2015.
 */
public class PhotoView extends RelativeLayout implements View.OnClickListener {

    public interface PhotoViewListener{
        void takePicture();
        void deletePicture(boolean clearFiles);
        void loadPicture();
        void rotatePicture();
        void removeFragment();
    }


    private PhotoViewListener listener;
    private ImageView locationImage;
    private ProgressBar progressBar;
    private RelativeLayout imageContainer;
    private ImageButton takePicture, deletePicture, loadPicture, rotatePicture, removeFragment;
    private TextView noImageLoadedText;

    public enum Option {
        SHOW_PROGRESS, CLEAR_PROGRESS, NO_IMAGE_LOADED,
        CANT_LOAD_IMAGE, CLEAR_CONTAINER, NULL_BITMAP
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.m("PhotoView", " inside constructor ");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.m("PhotoView", " onFinishInflate");
        setup();
    }

    public void setup(){
        Log.m("PhotoView", " setup");

        //setTag(PHOTO_TAG);
        noImageLoadedText = (TextView) findViewById(R.id.noImageLoadedText);
        locationImage = (ImageView) findViewById(R.id.locationImage);
        locationImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null); //TADA.. disable hardware acceleration bitmap views so they actually get cleared when you recycle.. if you cant retain instance which we cant in nested frag..
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
    public ImageView getLocationImageView(){
        return locationImage;
    }

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
                listener.removeFragment();
                break;

        }
    }


    private void drawContainer(Option... options){

        for (Option x : options) {
            switch (x) {
                case SHOW_PROGRESS:{
                    progressBar.setVisibility(View.VISIBLE);
                    break;}

                case CLEAR_PROGRESS:{
                    progressBar.setVisibility(View.GONE);
                    break;}

                case NO_IMAGE_LOADED:{
                    resetContainer(getResources().getString(R.string.no_image_loaded));
                    break;}

                case CANT_LOAD_IMAGE:{
                    resetContainer(getResources().getString(R.string.couldnt_load_image));
                    break;}

                case CLEAR_CONTAINER:{
                    imageContainer.setBackgroundColor(getResources().getColor(R.color.dialogBackground));
                    noImageLoadedText.setVisibility(View.GONE);
                    break;}

                case NULL_BITMAP:{
                    locationImage.setImageDrawable(null);
                    break;}
            }
        }

    }

    private void resetContainer(String text){
        noImageLoadedText.setText(text);
        noImageLoadedText.setVisibility(View.VISIBLE);
        imageContainer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }


    public void setBitmap(Bitmap bitmap){
        if (bitmap == null){
            drawContainer(CLEAR_PROGRESS, CANT_LOAD_IMAGE);
        } else {
            drawContainer(CLEAR_PROGRESS, CLEAR_CONTAINER);
            locationImage.setImageBitmap(bitmap);
            imageContainer.bringToFront();
            Log.m("PhotoView", " setBitmap Image size: W:" + bitmap.getWidth() + " H:" + bitmap.getHeight());
        }
    }




    public void dispose(){
        Log.m("PhotoView", " dispose");
        locationImage.setImageBitmap(null);
    }
}
