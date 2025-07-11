package com.itservices.gpxanalyzer.core.utils.files;

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

/**
 * Utility class providing helper methods for file operations, especially interaction with
 * content URIs and the application's internal file storage.
 */
public class FileProviderUtils {

    /**
     * Retrieves a list of files with a specified extension from the application's internal files directory.
     *
     * @param context   The application context.
     * @param extension The desired file extension (e.g., ".gpx"). Should include the dot.
     * @return A {@link List} of {@link File} objects matching the extension found in the app's internal files directory.
     *         Returns an empty list if the directory doesn't exist or no matching files are found.
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
     * Copies the content from a given content {@link Uri} (e.g., from a file picker) into a new file
     * within the application's internal files directory.
     * The new file will have the same name as the original file specified by the URI.
     * If the original file name does not end with the specified `fileExtension`, the copy is aborted and null is returned.
     *
     * @param context       The application context.
     * @param uri           The {@link Uri} pointing to the source content.
     * @param fileExtension The required file extension for the source file (e.g., ".gpx").
     * @return The newly created {@link File} in the app's internal storage upon successful copy,
     *         or {@code null} if an error occurs (e.g., InputStream error, I/O exception, incorrect extension).
     */
    public static File copyUriToAppStorage(Context context, Uri uri, String fileExtension) {
        ContentResolver contentResolver = context.getContentResolver();
        String fileName = getFileName(contentResolver, uri);

        if(!fileName.endsWith(fileExtension)) {
            return null;
        }

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
     * Retrieves the display name of the file associated with a content {@link Uri}.
     * Uses the {@link ContentResolver} and {@link OpenableColumns#DISPLAY_NAME}.
     *
     * @param contentResolver The {@link ContentResolver} instance.
     * @param uri             The {@link Uri} to query.
     * @return The file name, or "unknown_file" if the name cannot be retrieved or an error occurs.
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
     * Saves a given string content into a specified file within the application's internal files directory.
     * The content is written using UTF-8 encoding.
     *
     * @param context    The application context.
     * @param fileName   The desired name for the destination file (e.g., "mytrack.gpx").
     * @param gpxContent The string content to be written to the file.
     * @return {@code true} if the file was successfully saved, {@code false} otherwise (e.g., due to an IOException).
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