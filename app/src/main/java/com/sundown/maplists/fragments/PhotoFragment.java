package com.sundown.maplists.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.fields.PhotoField;
import com.sundown.maplists.tasks.TaskOptimizeImage;
import com.sundown.maplists.tasks.TaskRotateImage;
import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.views.PhotoView;

import java.io.IOException;

/**
 * Created by Sundown on 5/27/2015.
 *
 *  3 issues with support library nested fragments discovered today
 *  1) Bitmaps in nested fragments not recycled because the imageview layer holds a ref to them on orientation change thanks to hardware acceleration introduced in 3.. have to nullify layer (see photoview)
 *  2) #1 wouldnt actually matter if we could retain nested fragments, but we cannot. So we need to load the bitmap each time unless you think of another way to save it on configuration change without
 *  sending it up to a retainable parent fragment cache.
 *  3) because nested fragments are not retainable, they do not get their onActivityResult called directly by OS
 */
public class PhotoFragment extends Fragment implements
        PhotoView.PhotoViewListener, TaskOptimizeImage.TaskOptimizeImageListener, TaskRotateImage.TaskRotateImageListener {




    public interface PhotoFragmentListener{
        void deleteImage(String imageName, String thumbName);
        void deletePhotoFragment(int id);
    }

    public static final int ACTIVITY_CAMERA = 10;
    public static final int ACTIVITY_GALLERY = 20;
    public static final String FRAGMENT_ID = "FRAGMENT_ID";
    private PreferenceManager preferenceManager;
    private PhotoView view;
    private PhotoField model;
    private PhotoFragmentListener listener;
    private TaskOptimizeImage taskOptimizeImage;
    private int id, height, width;

    public static PhotoFragment newInstance(){
        return new PhotoFragment();
    }

    public void setListenerAndImageData(int id, PhotoFragmentListener listener, PhotoField photoField, int width, int height){
        this.id = id;
        this.listener = listener;
        model = photoField;
        this.width = width;
        this.height = height;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (PhotoView) inflater.inflate(R.layout.photo_container, container, false);
        view.setListener(this);
        preferenceManager = PreferenceManager.getInstance();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.m("PhotoFragment", "onResume");
        try {
            model.loadExistingTempFiles();
            loadImage();
            if (model.isPermanent())
                view.makePermanent();
        } catch (Exception e){
            Log.e(e);
        }
    }



    @Override
    public void onDestroyView() {
        Log.m("PhotoFragment", "onDestroyView");
        if (taskOptimizeImage != null && taskOptimizeImage.getStatus() != AsyncTask.Status.FINISHED) {
            taskOptimizeImage.cancel(true);
            Log.m("PhotoFragment", "task cancelled!");
        }
        //System.gc();

        view.dispose();
        taskOptimizeImage = null;

        super.onDestroyView();
    }

    @Override
    public void takePicture() {
        PackageManager pm = getActivity().getPackageManager();
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        boolean deviceHasCameraFlag = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        if (deviceHasCameraFlag && intent.resolveActivity(pm) != null) {
            try {
                model.generateTemporaryFiles();
                Uri uri = model.getImageUri();

                if (uri != null)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                saveFragmentIdForActivityResult();
                getActivity().startActivityForResult(intent, ACTIVITY_CAMERA);

            } catch (IOException e) {
                Log.e(e);
            }
        } else {
            Log.Toast(getActivity(), "Camera not supported on your device", Log.TOAST_SHORT); //todo
        }
    }


    @Override
    public void deletePicture(boolean clearFiles) {
        model.recycle(clearFiles);
        listener.deleteImage(model.getImageName(), model.getThumbName());
    }

    @Override
    public void loadPicture() {
        try {
            model.generateTemporaryFiles();
            saveFragmentIdForActivityResult();
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(galleryIntent, ACTIVITY_GALLERY);
        } catch (IOException e) {
            Log.e(e); //todo
        }
    }

    @Override
    public void rotatePicture() {
        if (model.getImageBitmap() != null){
            new TaskRotateImage(model, this).execute();
        }
    }

    @Override
    public void onImageRotated(boolean success) {
        view.setBitmap(model.getImageBitmap());
    }

    @Override
    public void deleteFragment() {
        deletePicture(true);
        listener.deletePhotoFragment(id);
    }


    //Yippee.. due to a bug in the support library, nested fragments don't get this called.. have to manually direct result from activity
    //WARNING: THIS GETS CALLED BEFORE ONRESUME!!! DONT USE ANY PART OF LAYOUT THAT ISNT YET INFLATED AND GIVEN DIMENSION
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.m("PhotoFragment", "We have returned to PhotoFragment fragment");

        preferenceManager = PreferenceManager.getInstance();
        int callingId = preferenceManager.getInt(FRAGMENT_ID);

        if (resultCode == Activity.RESULT_OK && callingId == this.id){

            deletePicture(false);
            taskOptimizeImage = new TaskOptimizeImage(model, width, height, this);

            switch(requestCode){

                case ACTIVITY_CAMERA:
                    taskOptimizeImage.execute();
                    break;

                case ACTIVITY_GALLERY:
                    Uri uri = data.getData();
                    taskOptimizeImage.execute(FileManager.getInstance().getFilePathFromGallery(uri));
                    break;
            }
        }
    }


    @Override
    public void onImageOptimized(Bitmap image) {
        view.setBitmap(image);
    }


    //todo move this to asynctask..
    private void loadImage() throws CouchbaseLiteException {

        view.loadingImage();

        if (model.getImageBitmap() == null){
            model.loadImageFromFile();
        }

        Bitmap imageBitmap = model.getImageBitmap();
        if (imageBitmap != null) {
            view.setBitmap(imageBitmap);
        } else {
            view.reset();
        }

    }

    private void saveFragmentIdForActivityResult(){
        preferenceManager = PreferenceManager.getInstance();
        preferenceManager.putInt(FRAGMENT_ID, id);
        preferenceManager.apply();
    }

    public PhotoField getModel(){
        return model;
    }


}
