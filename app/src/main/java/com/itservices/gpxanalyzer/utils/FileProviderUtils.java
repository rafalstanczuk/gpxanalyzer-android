package com.itservices.gpxanalyzer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileProviderUtils {

    /**
     * Retrieves a list of files with a specified extension.
     */
    public static List<File> getFilesByExtension(Context context, String extension) {
        List<File> fileList = new ArrayList<>();
        File directory = context.getFilesDir(); // App's private directory
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(extension));
            if (files != null) {
                fileList.addAll(Arrays.asList(files));
            }
        }
        return fileList;
    }

    /**
     * Copies the file from a given URI into the app's private storage.
     */
    public static File copyUriToAppStorage(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String fileName = getFileName(contentResolver, uri);

        File destinationFile = new File(context.getFilesDir(), fileName);
        try (InputStream inputStream = contentResolver.openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return destinationFile;
        } catch (Exception e) {
            Log.e("FileProviderUtils", "Error copying file", e);
            return null;
        }
    }

    /**
     * Retrieves the original file name from the URI.
     */
    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String name = "unknown_file";
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e("FileProviderUtils", "Error retrieving file name", e);
        }
        return name;
    }


    /**
     * Saves a GPX file from the provided string content to the specified file in internal storage.
     *
     * @param context The application context.
     * @param fileName The name of the file to save.
     * @param gpxContent The string content of the GPX file to save.
     * @return True if the file was successfully saved, false otherwise.
     */
    public static boolean saveGpxFileFromString(Context context, String fileName, String gpxContent) {
        // Define the file where the GPX content will be saved
        File destinationFile = new File(context.getFilesDir(), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            // Write the string content to the OutputStream
            outputStream.write(gpxContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush(); // Ensure all data is written
            return true;
        } catch (IOException e) {
            Log.e("FileProviderUtils", "Error saving GPX file from string content", e);
            return false;
        }
    }
}