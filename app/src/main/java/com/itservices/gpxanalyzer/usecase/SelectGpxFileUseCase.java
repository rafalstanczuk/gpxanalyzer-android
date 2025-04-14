package com.itservices.gpxanalyzer.usecase;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfoParser;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;
import com.itservices.gpxanalyzer.utils.files.FileProviderUtils;
import com.itservices.gpxanalyzer.utils.files.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Manages the selection, storage, and retrieval of GPX files.
 * This use case handles all operations related to file selection, including:
 * - Loading local GPX files from storage
 * - Requesting necessary file access permissions
 * - Opening the system file picker
 * - Copying selected files to the app's storage
 * - Tracking available GPX files
 * <p>
 * The class acts as a central point for managing GPX file selection and access.
 * It is implemented as a singleton to maintain consistent file selection state
 * across the application.
 */
@Singleton
public class SelectGpxFileUseCase {
    private static final String TAG = SelectGpxFileUseCase.class.getSimpleName();

    private static final String GPX_FILE_EXTENSION = ".gpx";
    private static final String[] MEDIA_STORE_SELECTION_ARGS = new String[]{"application/gpx+xml", "text/xml", "%.gpx"};
    private final PublishSubject<Boolean> isGpxFilePickedAndFound = PublishSubject.create();
    private final PublishSubject<File> gpxFilePickedAndFound = PublishSubject.create();
    private final PublishSubject<Boolean> permissionsGranted = PublishSubject.create();

    ActivityResultLauncher<String[]> filePickerLauncher;
    Disposable filePickerLauncherDisposable;
    ActivityResultLauncher<String[]> permissionLauncher;
    @Inject
    SearchFileUseCase searchFileUseCase;
    private File selectedFile = null;
    private List<File> fileFoundList = new ArrayList<>();
    private List<GpxFileInfo> gpxFileInfoList;

    /**
     * Creates a new SelectGpxFileUseCase.
     * Uses Dagger dependency injection.
     */
    @Inject
    public SelectGpxFileUseCase() {
    }

    /**
     * Returns an Observable that emits a boolean indicating whether a GPX file has been found.
     *
     * @return Observable emitting true when a GPX file is found, false otherwise
     */
    public Observable<Boolean> getIsGpxFilePickedAndFound() {
        return isGpxFilePickedAndFound;
    }

    /**
     * Returns an Observable that emits the permission grant status.
     *
     * @return Observable emitting true when permissions are granted, false otherwise
     */
    public Observable<Boolean> getPermissionsGranted() {
        return permissionsGranted;
    }

    public List<GpxFileInfo> getGpxFileInfoList() {
        return gpxFileInfoList;
    }

    public Single<List<GpxFileInfo>> searchAndParseGpxFilesRecursively(Context context) {
        return searchFileUseCase.searchAndParseFilesRecursively(
                        context, GpxFileInfoParser::parse,
                        GPX_FILE_EXTENSION, MEDIA_STORE_SELECTION_ARGS
                )
                .map(newParsedFileList -> {
                    List<GpxFileInfo> newGpxFileList = new ArrayList<>();
                    newParsedFileList.forEach(parsedFile -> newGpxFileList.add((GpxFileInfo) parsedFile));
                    gpxFileInfoList = newGpxFileList;
                    return newGpxFileList;
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gets the currently selected GPX file.
     *
     * @return The currently selected file, or null if none is selected
     */
    @Nullable
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Sets the currently selected GPX file.
     *
     * @param file The GPX file to set as selected
     */
    public void setSelectedFile(File file) {
        selectedFile = file;
    }

    /**
     * Gets the list of all GPX files found in the app's storage.
     *
     * @return List of GPX files
     */
    public List<File> getFileFoundList() {
        return fileFoundList;
    }

    /**
     * Loads all GPX files from the app's local storage.
     *
     * @param context The context used to access file storage
     * @return Single that emits the list of found GPX files
     */
    public Single<List<File>> loadLocalGpxFiles(Context context) {
        return Single.fromCallable(() -> {
            fileFoundList = FileProviderUtils.getFilesByExtension(context, GPX_FILE_EXTENSION);
            return fileFoundList;
        });
    }

    /**
     * Adds a file to the app's storage from a content URI.
     * This method copies the file from the URI to the app's private storage,
     * and adds it to the list of available GPX files.
     *
     * @param context The context used to access file storage
     * @param uri     The URI of the file to add
     * @return Single that emits the added file, or null if the file could not be added
     */
    public Single<File> addFile(Context context, Uri uri) {
        return Single.fromCallable(() -> {
            File file = FileProviderUtils.copyUriToAppStorage(context, uri, GPX_FILE_EXTENSION);
            if (file != null) {
                if (fileFoundList != null && !fileFoundList.contains(file)) {
                    fileFoundList.add(file);
                }
            }
            return file;
        });
    }

    /**
     * Registers activity result launchers for file picker and permissions with a FragmentActivity.
     * This method sets up launchers for handling file selection and permission requests.
     *
     * @param fragmentActivity The activity to register the launchers with
     */
    public void registerLauncherOn(FragmentActivity fragmentActivity) {
        permissionLauncher = fragmentActivity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = result.values().stream().allMatch(granted -> granted);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                // For Android 11+, we need to request MANAGE_EXTERNAL_STORAGE permission
                PermissionUtils.requestManageExternalStoragePermission(fragmentActivity);
            }

            permissionsGranted.onNext(allGranted);
        });

        filePickerLauncher = fragmentActivity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                ConcurrentUtil.tryToDispose(filePickerLauncherDisposable);

                filePickerLauncherDisposable = addFile(fragmentActivity, uri)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()).subscribe(file -> {
                            isGpxFilePickedAndFound.onNext(file != null);
                            if (file != null) {
                                gpxFilePickedAndFound.onNext(file);
                            }
                        });
            } else {
                Log.d("FileSelector", "No file selected");
            }
        });
    }

    /**
     * Checks for necessary file access permissions and requests them if not granted.
     *
     * @param requireActivity The activity to request permissions from
     * @return Observable that emits true when permissions are granted, false otherwise
     */
    public Observable<Boolean> checkAndRequestPermissions(FragmentActivity requireActivity) {
        if (PermissionUtils.hasFileAccessPermissions(requireActivity)) {
            return Observable.just(true);
        } else {
            PermissionUtils.requestFileAccessPermissions(permissionLauncher);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                PermissionUtils.requestManageExternalStoragePermission(requireActivity);
            }
            return permissionsGranted;
        }
    }

    /**
     * Opens the system file picker for selecting a GPX file.
     *
     * @return true if the file picker was launched successfully, false otherwise
     */
    public boolean openFilePicker() {
        if (filePickerLauncher != null) {
            String[] mimeTypes = new String[]{"application/gpx+xml", "text/xml"};
            filePickerLauncher.launch(mimeTypes);
            return true;
        }
        return false;
    }
}

