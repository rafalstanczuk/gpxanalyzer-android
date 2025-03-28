package com.itservices.gpxanalyzer.usecase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.itservices.gpxanalyzer.utils.FileProviderUtils;
import com.itservices.gpxanalyzer.utils.PermissionUtils;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

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
 * 
 * The class acts as a central point for managing GPX file selection and access.
 * It is implemented as a singleton to maintain consistent file selection state
 * across the application.
 */
@Singleton
public class SelectGpxFileUseCase {
    public static final String GPX_FILE_EXTENSION = ".gpx";
    private File selectedFile = null;
    private final PublishSubject<Boolean> isGpxFileFound = PublishSubject.create();
    private final PublishSubject<File> gpxFileFound = PublishSubject.create();

    private final PublishSubject<Boolean> permissionsGranted = PublishSubject.create();
    private List<File> fileFoundList = new ArrayList<>();

    private final PublishSubject<List<File>> gpxFileFoundList = PublishSubject.create();

    ActivityResultLauncher<String[]> filePickerLauncher;
    Disposable filePickerLauncherDisposable;

    ActivityResultLauncher<String[]> permissionLauncher;

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
    public Observable<Boolean> getIsGpxFileFound() {
        return isGpxFileFound;
    }

    /**
     * Returns an Observable that emits the permission grant status.
     * 
     * @return Observable emitting true when permissions are granted, false otherwise
     */
    public Observable<Boolean> getPermissionsGranted() {
        return permissionsGranted;
    }

    /**
     * Returns an Observable that emits the list of found GPX files whenever it changes.
     * 
     * @return Observable emitting the current list of GPX files
     */
    public Observable<List<File>> getGpxFileFoundList() {
        return gpxFileFoundList;
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
     * Gets the currently selected GPX file.
     * 
     * @return The currently selected file, or null if none is selected
     */
    @Nullable
    public File getSelectedFile() {
        return selectedFile;
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
     * @param uri The URI of the file to add
     * @return Single that emits the added file, or null if the file could not be added
     */
    public Single<File> addFile(Context context, Uri uri) {
        return Single.fromCallable(() -> {
            File file = FileProviderUtils.copyUriToAppStorage(context, uri, GPX_FILE_EXTENSION);
            if (file != null) {
                if (fileFoundList != null && !fileFoundList.contains(file)) {
                    fileFoundList.add(file);
                    gpxFileFoundList.onNext(fileFoundList);
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
        permissionLauncher =
                fragmentActivity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);

                    permissionsGranted.onNext(allGranted);
                });

        filePickerLauncher = fragmentActivity.registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                ConcurrentUtil.tryToDispose(filePickerLauncherDisposable);

                filePickerLauncherDisposable = addFile(fragmentActivity, uri)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(file -> {

                            isGpxFileFound.onNext(file != null);

                            if (file != null) {
                                gpxFileFound.onNext(file);
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
        // Check and request storage permissions if needed
        if (PermissionUtils.hasFileAccessPermissions(requireActivity)) {
            return Observable.just(true);
        } else {
            PermissionUtils.requestFileAccessPermissions(permissionLauncher);
        }

        return permissionsGranted;
    }

    /**
     * Opens the system file picker for selecting a GPX file.
     * 
     * @return true if the file picker was launched successfully, false otherwise
     */
    public boolean openFilePicker() {
        if (filePickerLauncher != null) {
            String[] mimeTypes = new String[]{
                    "*/*"
            };
            // Launch the file picker with MIME type filters
            filePickerLauncher.launch(mimeTypes);
        }

        return filePickerLauncher != null;
    }
}
