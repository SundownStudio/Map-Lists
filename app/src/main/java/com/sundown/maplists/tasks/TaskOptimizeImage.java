package com.sundown.maplists.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.PhotoField;

/**
 * Created by Sundown on 5/27/2015.
 */
public class TaskOptimizeImage extends AsyncTask<String, Void, Boolean> {

    public interface TaskOptimizeImageListener {
        void onImageOptimized(Bitmap image);
    }


    private TaskOptimizeImageListener listener;
    private int width, height;
    public PhotoField model;


    public TaskOptimizeImage(PhotoField model, int width, int height, TaskOptimizeImageListener listener){

        // Use a WeakReference to ensure the ImageView can be garbage collected todo not anymore.. but relearn weakreferences so we know them cuz i kinda forget how to..
        this.model = model;
        this.width = width;
        this.height = height;
        this.listener = listener;
        Log.m("TaskLoadResizeImage: W: " + width + " H: " + height);
    }


    @Override
    protected Boolean doInBackground(String... params) {

        String selectedImagePath = null;
        if (params != null && params.length > 0) {
            selectedImagePath = params[0];
        }

        try { //refuse the inclination to chain!!
            model.resizeAndRotate(selectedImagePath, width, height);
            model.extractThumb();
            model.saveContentsToFiles();

            return true;
        } catch (Exception e){
            Log.e(e);

        }

        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        //todo optimize the logic here at some point..
        if (isCancelled()) {
            success = false;
        }

        if (model.image != null) {
            listener.onImageOptimized(model.image);

        } else {
            Log.m("TaskLoadBitmap","Task broke, null bitmap");
            listener.onImageOptimized(null);
            success = false;
        }

        if (!success){
            Log.m("TaskLoadBitmap","Task cancelled");
            model.cleanupTemporaryFiles();
        }
    }
}