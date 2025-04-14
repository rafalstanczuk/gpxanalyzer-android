package com.itservices.gpxanalyzer.usecase;

import static android.os.Environment.DIRECTORY_ALARMS;
import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.DIRECTORY_NOTIFICATIONS;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.DIRECTORY_PODCASTS;
import static android.os.Environment.DIRECTORY_RINGTONES;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SearchFileUseCase {
    private final String TAG = SearchFileUseCase.class.getSimpleName();
    private static final String STORAGE = "storage";
    private static final String EMULATED = "emulated";
    private static final String SELF = "self";
    private static final String ORDER_ASC = " ASC";
    private static final String EXTERNAL_STORAGE = "external";
    private static final String INTERNAL_STORAGE = "internal";
    private static final String[] MEDIA_STORE_PROJECTION = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.RELATIVE_PATH, MediaStore.Files.FileColumns.VOLUME_NAME};
    private static final String MEDIA_STORE_SELECTION_QUERY = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?";
    private final List<String> KNOWN_DIRECTORIES_LIST = Arrays.asList(
            DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES,
            DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES,
            DIRECTORY_MOVIES, DIRECTORY_DOWNLOADS, DIRECTORY_DCIM, DIRECTORY_DOCUMENTS);

    @Inject
    GlobalEventWrapper globalEventWrapper;

    private Function<File, Object> parserFunction;
    private String fileExtension = "";
    private String[] mediaStoreSelectionArgs = new String[]{""};

    private final List<Object> parsedFileList = new ArrayList<>();

    @Inject
    public SearchFileUseCase() {
    }

    private static boolean isExists(File dir) {
        return dir != null && dir.exists();
    }

    private static boolean isEquals(File fileItem, File file) {

        return fileItem.equals(file);
    }

    private boolean isFileWithExtensionName(String displayName, String fileExtension) {
        return displayName != null && displayName.toLowerCase().endsWith(fileExtension);
    }

    public Single<List<Object>> searchAndParseFilesRecursively(Context context, Function<File, Object> parserFunction, String fileExtension, String[] mediaStoreSelectionArgs) {
        this.parserFunction = parserFunction;
        this.fileExtension = fileExtension;
        this.mediaStoreSelectionArgs = mediaStoreSelectionArgs;
        parsedFileList.clear();

        return Single.fromCallable(() -> {
            Log.d(TAG, "Starting file search");
            List<File> fileList = new ArrayList<>();

            AtomicInteger totalFiles = new AtomicInteger(0);
            AtomicInteger processedFiles = new AtomicInteger(0);

            searchInMediaStore(context, fileList);

            searchFileSystemDirect(context, totalFiles, processedFiles, fileList);

            return parsedFileList;
        }).subscribeOn(Schedulers.io());
    }

    private List<File> getExternalStorageDirectories(Context context) {
        Log.d(TAG, "Detecting external storage directories...");
        List<File> storageDirs = new ArrayList<>();

        KNOWN_DIRECTORIES_LIST.forEach(extDir -> {
            File dir = Environment.getExternalStoragePublicDirectory(extDir);
            if (isExists(dir)) {
                storageDirs.add(dir);
            }
        });

        File externalStorage = Environment.getExternalStorageDirectory();
        if (isExists(externalStorage)) {
            //Log.d(TAG, "Found standard external storage: " + externalStorage.getAbsolutePath());
            storageDirs.add(externalStorage);
        }

        // Add secondary storage (SD cards)
        File[] externalFilesDirs = context.getExternalFilesDirs(null);
        if (externalFilesDirs != null) {
            //Log.d(TAG, "Found " + externalFilesDirs.length + " external files dirs.");
            for (File dir : externalFilesDirs) {
                if (dir != null) {
                    //Log.d(TAG, "Processing external files dir: " + dir.getAbsolutePath());
                    // Get the parent directory (actual external storage)
                    File parent = dir.getParentFile();
                    if (parent != null) {
                        //Log.d(TAG, "Parent of external files dir: " + parent.getAbsolutePath());
                        // Navigate up to the mount point (typically /storage)
                        File storageRoot = parent;
                        File childStorageRoot = parent;
                        while (storageRoot != null && !storageRoot.getName().equals(STORAGE) && storageRoot.getParentFile() != null) {
                            childStorageRoot = storageRoot;
                            storageRoot = storageRoot.getParentFile();
                        }
                        //Log.d(TAG, "Found childStorageRoot root: " + childStorageRoot.getAbsolutePath());

                        if (storageRoot != null && storageRoot.getName().equals(STORAGE)) {

                            //Log.d(TAG, "Found childStorageRoot root: " + childStorageRoot.getAbsolutePath());
                            //Log.d(TAG, "Found storage root: " + storageRoot.getAbsolutePath());

                            // Add all directories under /storage that aren't the primary storage or self-reference
                            File[] potentialStorageDirs = childStorageRoot.listFiles();
                            //Log.d(TAG, "Found storage root file potentialStorageDirs: " + Arrays.toString(potentialStorageDirs));
                            if (potentialStorageDirs != null) {
                                for (File storageDir : potentialStorageDirs) {
                                    //Log.d(TAG, "Found storage root file name: " + storageDir.getName());
                                    if (storageDir.isDirectory() && !storageDir.getName().equals(EMULATED)
                                            && !storageDir.getName().equals(SELF) && !storageDirs.contains(storageDir)) { // Avoid duplicates
                                        //Log.d(TAG, "Found potential SD card/secondary storage: " + storageDir.getAbsolutePath());
                                        storageDirs.add(storageDir);
                                    }
                                }
                            }
                        } else {
                            //Log.w(TAG, "Could not navigate to /storage root from: " + parent.getAbsolutePath());
                        }
                    } else {
                        //Log.w(TAG, "Parent of external files dir is null: " + dir.getAbsolutePath());
                    }
                }
            }
        }

        Log.d(TAG, "Final list of storage directories to scan: "
                + storageDirs.stream()
                .map(File::getAbsolutePath)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("None"));
        return storageDirs;
    }

    private void searchFileSystemDirect(Context context, AtomicInteger totalFiles, AtomicInteger processedFiles, List<File> fileList) {
        // 2. Direct file system search in all storage locations
        Log.d(TAG, "Starting direct file system search...");

        // Get all external storage directories (including SD cards)
        List<File> storageDirs = getExternalStorageDirectories(context);

        // Count total files first for progress tracking (only for direct scan)
        totalFiles.set(0); // Reset count for direct scan
        Log.d(TAG, "Counting files for progress calculation...");
        for (File storageDir : storageDirs) {
            countFilesRecursively(storageDir, totalFiles);
        }
        Log.d(TAG, "Total files to process (direct scan): " + totalFiles.get());

        // Search in all storage directories
        processedFiles.set(0); // Reset progress for direct scan
        for (File storageDir : storageDirs) {
            searchAndParseFilesRecursively(storageDir, fileList, totalFiles, processedFiles);
        }

        Log.i(TAG, fileExtension + " file search finished. Total unique files found: " + parsedFileList.size());
    }

    private void searchInMediaStore(Context context, List<File> fileList) {
        // 1. Search using MediaStore (for indexed files)
        Log.d(TAG, "Searching MediaStore for " + fileExtension + " files");
        ContentResolver contentResolver = context.getContentResolver();

        searchByContentResolver(fileList, contentResolver);

        Log.d(TAG, "MediaStore search finished. Found " + fileList.size() + " unique " + fileExtension + " files so far.");
    }

    private void searchByContentResolver(List<File> fileList, ContentResolver contentResolver) {
        // Search in both internal and external storage
        Uri[] contentUris = {MediaStore.Files.getContentUri(EXTERNAL_STORAGE), MediaStore.Files.getContentUri(INTERNAL_STORAGE)};

        for (Uri contentUri : contentUris) {
            searchInContentUri(fileList, contentUri, contentResolver,
                    MEDIA_STORE_PROJECTION, MEDIA_STORE_SELECTION_QUERY, mediaStoreSelectionArgs
            );
        }
    }

    private void searchInContentUri(List<File> fileList, Uri contentUri, ContentResolver contentResolver, String[] projection, String selection, String[] selectionArgs) {
        try (Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, MediaStore.Files.FileColumns.DISPLAY_NAME + ORDER_ASC)) {
            if (cursor != null) {
                searchFilesOnCursor(fileList, contentUri, cursor);
            } else {
                Log.d(TAG, "MediaStore query returned null cursor for URI: " + contentUri);
            }
        } catch (Exception ignore) {
            //Log.e(TAG, "Error searching MediaStore for " + fileExtension + " files", e);
        }
    }

    private void searchFilesOnCursor(List<File> fileList, Uri contentUri, Cursor cursor) throws Exception {
        Log.d(TAG, "MediaStore query returned " + cursor.getCount() + " files for URI: " + contentUri);

        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
        int relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH);
        int volumeNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.VOLUME_NAME);
        // Don't add MediaStore results to total count initially, as direct search might find them too
        // totalFiles.addAndGet(cursor.getCount());

        while (cursor.moveToNext()) {
            String filePath = cursor.getString(dataColumn);
            String displayName = cursor.getString(displayNameColumn);
            String relativePath = cursor.getString(relativePathColumn);
            String volumeName = cursor.getString(volumeNameColumn);

            if (filePath != null && isFileWithExtensionName(displayName, fileExtension)) {
                addFileFrom(fileList, filePath);
            }
            //Log.i(TAG, "MediaStore file exists: " + displayName);
            // We don't increment processedFiles here, only during direct scan
        }
    }

    private void addFileFrom(List<File> fileList, String filePath) throws Exception {
        Log.d(TAG, "MediaStore found potential " + fileExtension + ": " + filePath);
        File file = new File(filePath);
        if (file.exists()) {
            Log.i(TAG, "MediaStore confirmed " + fileExtension + " file exists: " + file.getAbsolutePath());

            // Avoid adding duplicates found by direct search later
            if (fileList.stream().noneMatch(fileItem -> isEquals(fileItem, file))) {
                fileList.add(file);
                parsedFileList.add(parserFunction.apply(file));
            }
        } else {
            Log.w(TAG, "File from MediaStore does not exist or is inaccessible: " + filePath);
        }
    }

    private void countFilesRecursively(File directory, AtomicInteger counter) {
        if (directory == null || !directory.exists()) {
            //Log.v(TAG, "Skipping count in non-existent directory: " + (directory == null ? "null" : directory.getAbsolutePath()));
            return;
        }
        if (!directory.isDirectory()) {
            //Log.v(TAG, "Skipping count for non-directory: " + directory.getAbsolutePath());
            return;
        }

        //Log.v(TAG, "Counting files in directory: " + directory.getAbsolutePath());
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        countFilesRecursively(file, counter);
                    } else {
                        counter.incrementAndGet();
                    }
                }
            } else {
                //Log.w(TAG, "listFiles() returned null for directory (possibly access denied?): " + directory.getAbsolutePath());
            }
        } catch (SecurityException e) {
            //Log.w(TAG, "Access denied during count in directory: " + directory.getAbsolutePath());
        }
    }

    private void searchAndParseFilesRecursively(File directory, List<File> fileList, AtomicInteger totalFiles, AtomicInteger processedFiles) {
        if (directory == null || !directory.exists()) {
            //Log.v(TAG, "Skipping search in non-existent directory: " + (directory == null ? "null" : directory.getAbsolutePath()));
            return;
        }
        if (!directory.isDirectory()) {
            //Log.v(TAG, "Skipping search for non-directory: " + directory.getAbsolutePath());
            return;
        }

        //Log.d(TAG, "Searching directory: " + directory.getAbsolutePath());
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        searchAndParseFilesRecursively(file, fileList, totalFiles, processedFiles);
                    } else {
                        processFile(fileList, totalFiles, processedFiles, file);
                    }
                }
            } else {
                //Log.w(TAG, "listFiles() returned null for directory (possibly access denied?): " + directory.getAbsolutePath());
            }
        } catch (SecurityException e) {
            //Log.w(TAG, "Access denied during search in directory: " + directory.getAbsolutePath());
        } catch (Exception ignored) {

        }
    }

    private void processFile(List<File> fileList, AtomicInteger totalFiles, AtomicInteger processedFiles, File file) throws Exception {
        if (file.getName().toLowerCase().endsWith(fileExtension)) {
            Log.i(TAG, "Direct search found " + fileExtension + " file: " + file.getAbsolutePath());

            // Avoid adding duplicates found by MediaStore
            if (fileList.stream().noneMatch(fileItem -> isEquals(fileItem, file))) {
                fileList.add(file);
                parsedFileList.add(parserFunction.apply(file));
            } else {
                Log.d(TAG, "Skipping duplicate " + fileExtension + " file found by direct search: " + file.getAbsolutePath());
            }
        }
        //Log.i(TAG, "Direct search file: " + file.getName());
        processedFiles.incrementAndGet();
        updateProgress(processedFiles, totalFiles);
    }

    private void updateProgress(AtomicInteger processedFiles, AtomicInteger totalFiles) {
        if (totalFiles.get() > 0) {
            int progress = (int) ((processedFiles.get() * 100.0) / totalFiles.get());
            // Log.d(TAG, "Search progress: " + progress + "% (" + processedFiles.get() + "/" + totalFiles.get() + " files)");
            globalEventWrapper.onNext(new EventProgress(SearchFileUseCase.class, progress));
        }
    }
}
