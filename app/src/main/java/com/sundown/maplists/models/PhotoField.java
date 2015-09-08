package com.sundown.maplists.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sundown.maplists.logging.Log;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.FileManager;
import com.sundown.maplists.utils.PhotoUtils;
import com.sundown.maplists.utils.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Sundown on 6/25/2015.
 */
public class PhotoField extends Field {

    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String THUMBNAIL_PREFIX = "THM_";
    private static final String IMAGE_PREFIX = "IMG_";
    private static final String IMAGE_FILE = "IMAGE_FILE";
    private static final String THUMB_FILE = "THUMB_FILE";

    private PhotoUtils photoUtils;
    private FileManager fileManager;
    private PreferenceManager preferenceManager;
    public File imageTempFile, thumbTempFile;
    public String imageName, thumbName;
    public Bitmap image, thumb;

    public PhotoField(boolean permanent){
        super(0, "Photo", FieldType.PHOTO, permanent);
        init();
    }


    public PhotoField(int id, boolean permanent){
        super(id, "Photo", FieldType.PHOTO, permanent);
        init();
    }

    private void init(){
        preferenceManager = PreferenceManager.getInstance();
        photoUtils = PhotoUtils.getInstance();
        fileManager = FileManager.getInstance();
    }


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(JsonConstants.IMAGE, imageName);
        properties.put(JsonConstants.THUMB, thumbName);
        return properties;
    }

    @Override
    public PhotoField setProperties(Map properties) {
        super.setProperties(properties);
        imageName = String.valueOf(properties.get(JsonConstants.IMAGE));
        thumbName = String.valueOf(properties.get(JsonConstants.THUMB));
        return this;
    }

    public void recycle(boolean clearFiles) {
        if (image != null) {
            image.recycle();
            image = null;
        }
        if (thumb != null){
            thumb.recycle();
            thumb = null;
        }

        if (clearFiles) {
            cleanupTemporaryFiles();
        }
    }

    public void cleanupTemporaryFiles(){
        if (imageTempFile != null && imageTempFile.exists())
            imageTempFile.delete();
        if (thumbTempFile != null && thumbTempFile.exists())
            thumbTempFile.delete();
        image = null; //DO NOT RECYCLE THESE HERE.. WE MAY STILL NEED THEM TO COMPRESS TO DB.. IF RECYCLING USE recycle() INSTEAD..
        thumb = null;
        preferenceManager.remove(IMAGE_FILE + id);
        preferenceManager.remove(THUMB_FILE + id);
        preferenceManager.commit();
    }


    public void setImage(Bitmap defaultImage){
        this.image = defaultImage;
    }


    public void loadImageFromFile(){
        if (imageTempFile != null){ //load it from file if it exists..
            image = BitmapFactory.decodeFile(imageTempFile.getAbsolutePath());
        }
        if (thumbTempFile != null){
            thumb = BitmapFactory.decodeFile(thumbTempFile.getAbsolutePath());
        }
    }

    public void generateTemporaryFiles() throws IOException {
        if (imageTempFile == null)
            imageTempFile = fileManager.createFile(IMAGE_PREFIX, IMAGE_EXTENSION, id);
        if (thumbTempFile == null)
            thumbTempFile = fileManager.createFile(THUMBNAIL_PREFIX, IMAGE_EXTENSION, id);
    }

    public void loadExistingTempFiles(){
        //load temp files from prefs if they dont exist yet..
        String imageFilepath = preferenceManager.getString(IMAGE_FILE + id);
        String thumbFilepath = preferenceManager.getString(THUMB_FILE + id);

        if (imageTempFile == null && imageFilepath.length() > 0)
            imageTempFile = new File(imageFilepath);

        if (thumbTempFile == null && thumbFilepath.length() > 0)
            thumbTempFile = new File(thumbFilepath);
    }

    public void resizeAndRotate(String selectedImagePath, int width, int height) throws IOException {
        if (selectedImagePath == null)
            selectedImagePath = imageTempFile.getAbsolutePath();

        //resizing image removes its Exif, so to get the orientation we need to process that first..
        int rotate = photoUtils.imageOrientationValidator(selectedImagePath);

        //now resize image to fit container because we dont need all that extra memory for big image
        image = photoUtils.resizeImage(selectedImagePath, width, height);
        image = photoUtils.rotateImage(image, rotate);
        Log.m("Photo: bitmap resized! W: " + width + " H: " + height);
    }

    public void rotateImages(){
        image = photoUtils.rotateImage(image, 90);
        thumb = photoUtils.rotateImage(thumb, 90);
    }

    public void extractThumb(){
        thumb = photoUtils.extractThumbnail(image);
    }

    public void saveContentsToFiles() throws IOException {
        fileManager.saveImageToFile(imageTempFile, image);
        fileManager.saveImageToFile(thumbTempFile, thumb);

        if (imageTempFile != null){
            imageName = imageTempFile.getName();
            preferenceManager.putString(IMAGE_FILE + id, imageTempFile.getAbsolutePath());
        }
        if (thumbTempFile != null){
            thumbName = thumbTempFile.getName();
            preferenceManager.putString(THUMB_FILE + id, thumbTempFile.getAbsolutePath());
        }
        preferenceManager.commit();
    }

}


