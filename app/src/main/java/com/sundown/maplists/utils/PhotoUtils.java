package com.sundown.maplists.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;

import com.sundown.maplists.MapListsApp;
import com.sundown.maplists.R;

import java.io.IOException;

/**
 * Created by Sundown on 5/12/2015.
 */
public class PhotoUtils {


    private static PhotoUtils instance;
    private BitmapFactory.Options options;
    private int thumbnailDimens;


    public static PhotoUtils getInstance() {
        if (instance == null)
            instance = new PhotoUtils();
        return instance;
    }

    private PhotoUtils() {
        thumbnailDimens = (int) (MapListsApp.getContext().getResources().getDimension(R.dimen.thumbnail_image_size) / MapListsApp.getContext().getResources().getDisplayMetrics().density);
        options = new BitmapFactory.Options();       //options.inSampleSize = 8;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }


    public Bitmap resizeImage(String path, int targetWidth, int targetHeight) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true; // avoids memory allocation, returning null for the bitmap object but setting outWidth, outHeight and outMimeType
        BitmapFactory.decodeFile(path, bmOptions);

        int scaleFactor = calculateInSampleSize(bmOptions, targetWidth, targetHeight); //better method, gets lower outputs than Math.min(photoW / targetWidth, photoH / targetHeight)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false; //we want the image this time..
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path, bmOptions);

    }


    //use this for loading pics that are loaded from somewhere else where we cant resize
    //To tell the decoder to subsample the image, loading a smaller version into memory, set inSampleSize in BitmapFactory.Options
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    public int imageOrientationValidator(String path) throws IOException {
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;

            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;

            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;

        }

        return 0;
    }


    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    public Bitmap extractThumbnail(Bitmap image) {
        return ThumbnailUtils.extractThumbnail(image, thumbnailDimens, thumbnailDimens);
    }
}
