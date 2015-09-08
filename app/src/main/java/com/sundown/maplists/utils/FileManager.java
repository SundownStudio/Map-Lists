package com.sundown.maplists.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.sundown.maplists.MapListsApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sundown on 9/7/2015.
 */
public class FileManager {

    private static FileManager instance;
    private SimpleDateFormat dateFormat;
    private File storageDir;

    public static FileManager getInstance(){
        if (instance == null)
            instance = new FileManager();
        return instance;
    }

    private FileManager(){
        dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }


    public File createFile(String prefix, String extension, int id) throws IOException {

        String fileName = generateFileName(prefix, id);
        return File.createTempFile(
                fileName,       /* prefix */
                extension,      /* suffix */
                storageDir      /* directory */
        );
    }

    public void saveImageToFile(File destinationFile, Bitmap image) throws IOException { //todo: handle lack of space issues
        FileOutputStream out = new FileOutputStream(destinationFile);
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public String getFilePathFromGallery(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = MapListsApp.getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();

        return imgDecodableString;
    }


    private String generateFileName(String prefix, int id){
        return prefix + dateFormat.format(new Date()) + "_" + id;
    }

}
