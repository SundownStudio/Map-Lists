package com.sundown.maplists.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.sundown.maplists.MapLists;
import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sundown on 5/12/2015.
 */
public class PhotoUtils {

    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String THUMBNAIL_PREFIX = "THM_";
    public static final String IMAGE_PREFIX = "IMG_";

    private PhotoUtils(){
        thumbnailDimens =  (int) (MapLists.getContext().getResources().getDimension(R.dimen.thumbnail_image_size) / MapLists.getContext().getResources().getDisplayMetrics().density);
        dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        options = new BitmapFactory.Options();
        //options.inSampleSize = 8;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    private static PhotoUtils instance;
    public static int thumbnailDimens;
    private SimpleDateFormat dateFormat;
    private File storageDir;
    private BitmapFactory.Options options;

    public static PhotoUtils getInstance(){ //todo: do we ever need to reinit the context?
        if (instance == null)
            instance = new PhotoUtils();
        return instance;
    }

    public Bitmap resizeThumbnail(Bitmap image) {
        Bitmap resized = Bitmap.createScaledBitmap(image, thumbnailDimens, thumbnailDimens, true);
        Log.m("New Picture specs - height: " + resized.getHeight() + " width: " + resized.getWidth() + " imageSize: " + thumbnailDimens);
        return resized;
    }

    public File createImageFile(String prefix, int id) throws IOException {
        // Create an image file name
        String imageFileName = generateFileName(prefix, id);
        Log.m("destination file: " + imageFileName);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                IMAGE_EXTENSION, /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public String generateFileName(String prefix, int id){
        return prefix + dateFormat.format(new Date()) + "_" + id;
    }


    public final Bitmap getImageFromFile(String fileName){
        return BitmapFactory.decodeFile(getFullPath(fileName), options);
    }


    public String getFullPath(String fileName){
        return storageDir + "/" + fileName;
    }

    public boolean deleteImage(String fileName){
        File file = new File(storageDir + "/" + fileName);
        if (file.exists())
            return file.delete();
        return false;
    }


    public Bitmap resizeImage(String path, int targetWidth, int targetHeight){

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true; // avoids memory allocation, returning null for the bitmap object but setting outWidth, outHeight and outMimeType
        BitmapFactory.decodeFile(path, bmOptions);

        int scaleFactor = calculateInSampleSize(bmOptions, targetWidth, targetHeight); //better method, gets lower outputs than Math.min(photoW / targetWidth, photoH / targetHeight)
        Log.m("PHOTO", "Second scale factor: " + scaleFactor + " targetWidth: " + targetWidth + " targetHeight: " + targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false; //we want the image this time..
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path, bmOptions);

    }



    //use this for loading pics that are loaded from somewhere else where we cant resize
    //To tell the decoder to subsample the image, loading a smaller version into memory, set inSampleSize in BitmapFactory.Options
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
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
        ExifInterface ei;

        ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.m("EditLocationDialogFragment", "ROTATING 90 DEGREES");
                return 90;

            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.m("EditLocationDialogFragment", "ROTATING 180 DEGREES");
                return 180;

            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.m("EditLocationDialogFragment", "ROTATING 270 DEGREES");
                return 270;

        }

        return 0;
    }


    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public String getPathFromGallery(Uri selectedImage){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        // Get the cursor
        Cursor cursor = MapLists.getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();

        return imgDecodableString;
    }


    public void saveImage(File destinationFile, Bitmap image) throws IOException { //todo: handle lack of space issues
        FileOutputStream out = new FileOutputStream(destinationFile);
        image.compress(Bitmap.CompressFormat.JPEG, 100, out); //todo: switched from png..
        out.flush();
        out.close();
    }


    /* use this if otherway is memory hit
    public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


        public boolean oneOfOurImages(String path){
        String parent = path.substring(0, path.lastIndexOf("/"));
        String prefix = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("/") + 5);
        if (parent.equals(storageDir.getAbsolutePath()) && prefix.equals(PhotoManager.IMAGE_PREFIX)){
            return true;
        }
        return false;
    }


    public final Bitmap getImageFromPath(String fullpath){
        return BitmapFactory.decodeFile(fullpath);
    }

    */

}
