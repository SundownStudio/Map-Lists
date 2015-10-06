package com.sundown.maplists.tasks;

import android.os.AsyncTask;

import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.fields.PhotoField;

/**
 * Created by Sundown on 8/15/2015.
 */
public class TaskRotateImage extends AsyncTask<Void, Void, Boolean> {


    public interface TaskRotateImageListener {
        void onImageRotated(boolean success);
    }

    private TaskRotateImageListener listener;
    private PhotoField model;

    public TaskRotateImage(PhotoField model, TaskRotateImageListener listener){
        this.model = model;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            model.rotateImages();
            return true;
        } catch (Exception e){
            Log.e(e);
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        listener.onImageRotated(success);
    }

}
