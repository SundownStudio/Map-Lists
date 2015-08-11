package com.sundown.maplists.pojo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.sundown.maplists.tasks.TaskOptimizeImage;

import java.lang.ref.WeakReference;

/**
 * Created by Sundown on 5/13/2015.
 */
public class AsyncDrawable extends BitmapDrawable {

    private final WeakReference<TaskOptimizeImage> task;

    public AsyncDrawable(Resources res, Bitmap bitmap, TaskOptimizeImage bitmapWorkerTask) {
        super(res, bitmap);
        task = new WeakReference<TaskOptimizeImage>(bitmapWorkerTask);
    }

    public TaskOptimizeImage getBitmapTask() {
        return task.get();
    }

    public static TaskOptimizeImage getBitmapTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapTask();
            }
        }
        return null;
    }

    /* todo do we ever need this?
    public static boolean cancelPotentialWork(String file, ImageView imageView) {
        final TaskOptimizeImage task = getBitmapTask(imageView);

        if (task != null) {
            String fileName = task.sImagePath;
            // If bitmapData is not yet set or it differs from the new data
            if (fileName == null || !fileName.equals(file)) {
                // Cancel previous task
                task.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    } */
}
