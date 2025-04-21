package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.data.provider.GpxFileInfoProvider.GPX_FILE_EXTENSION;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.RequestStatus;
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
 * Use case dedicated to handling the selection of GPX files by the user.
 * This includes:
 * <ul>
 *     <li>Registering and managing Android Activity Result Launchers for file picking and permission requests.</li>
 *     <li>Checking and requesting necessary file access permissions (including special handling for Android R+).</li>
 *     <li>Launching the system file picker.</li>
 *     <li>Handling the selected file URI, copying it to the app's internal storage.</li>
 *     <li>Maintaining a list of locally available/found GPX files.</li>
 *     <li>Notifying other components about permission status changes and newly selected files via RxJava Subjects.</li>
 *     <li>Managing the lifecycle of related asynchronous operations (e.g., file copying).</li>
 * </ul>
 */
@Singleton
public class SelectGpxFileUseCase {
    private static final String TAG = SelectGpxFileUseCase.class.getSimpleName();

    /**
     * Subject that emits the {@link File} object when a GPX file is successfully picked by the user
     * and copied/found in the app's storage.
     */
    final PublishSubject<File> gpxFilePickedAndFound = PublishSubject.create();
    /**
     * Subject that emits a boolean indicating whether the required file access permissions have been granted.
     * Emits `true` if granted, `false` otherwise.
     */
    private final PublishSubject<Boolean> permissionsGranted = PublishSubject.create();

    /**
     * Activity Result Launcher for the system's file picker (OpenDocument contract).
     */
    ActivityResultLauncher<String[]> filePickerLauncher;
    /**
     * Disposable for the asynchronous operation initiated after the file picker returns a result (e.g., copying the file).
     */
    Disposable filePickerLauncherDisposable;
    /**
     * Activity Result Launcher for requesting runtime permissions.
     */
    ActivityResultLauncher<String[]> permissionLauncher;

    /**
     * Global event bus for emitting status updates, like {@link RequestStatus#SELECTED_FILE}.
     */
    @Inject
    GlobalEventWrapper globalEventWrapper;

    /**
     * Holds the most recently selected GPX file by the user.
     */
    private File selectedFile = null;
    /**
     * List of GPX files found or added to the application's accessible storage.
     */
    List<File> fileFoundList = new ArrayList<>();


    /**
     * Creates a new {@code SelectGpxFileUseCase}.
     * Constructor used by Dagger for dependency injection.
     */
    @Inject
    public SelectGpxFileUseCase() {
    }

    /**
     * Returns an {@link Observable} that emits the status of file access permission grants.
     * Subscribers will be notified when the user grants or denies the required permissions.
     *
     * @return Observable emitting true if permissions are granted, false otherwise.
     */
    public Observable<Boolean> getPermissionsGranted() {
        return permissionsGranted;
    }

    /**
     * Gets the currently selected GPX file.
     *
     * @return The most recently selected {@link File}, or {@code null} if no file has been selected yet.
     */
    @Nullable
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Sets the currently selected GPX file and notifies listeners via the global event bus.
     *
     * @param file The GPX {@link File} to set as selected.
     */
    public void setSelectedFile(File file) {
        selectedFile = file;
        globalEventWrapper.onNext(RequestStatus.SELECTED_FILE);
    }

    /**
     * Gets the list of GPX files currently known to the application (found in local storage or added by the user).
     *
     * @return A {@link List} of {@link File} objects representing GPX files.
     */
    public List<File> getFileFoundList() {
        return fileFoundList;
    }

    /**
     * Scans the application's local storage for files with the GPX extension
     * and updates the internal {@link #fileFoundList}.
     *
     * @param context The application {@link Context} used to access file storage.
     * @return A {@link Single} that emits the updated list of found GPX files upon completion.
     */
    public Single<List<File>> loadLocalGpxFiles(Context context) {
        return Single.fromCallable(() -> {
            fileFoundList = FileProviderUtils.getFilesByExtension(context, GPX_FILE_EXTENSION);
            return fileFoundList;
        });
    }

    /**
     * Copies a file specified by a content {@link Uri} (typically obtained from the file picker)
     * to the application's internal storage. If successful, the new {@link File} is added
     * to the {@link #fileFoundList}.
     *
     * @param context The application {@link Context} used for file operations.
     * @param uri     The {@link Uri} of the file to copy.
     * @return A {@link Single} that emits the newly created {@link File} in app storage upon success,
     *         or emits {@code null} if the copy operation failed.
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
     * Registers the necessary Activity Result Launchers (`permissionLauncher` and `filePickerLauncher`)
     * with the provided {@link FragmentActivity}. This setup is required before permissions can be
     * requested or the file picker can be launched.
     *
     * It configures the callbacks for both launchers:
     * - Permission launcher: Checks if all requested permissions were granted and handles the special
     *   `MANAGE_EXTERNAL_STORAGE` case for Android R+.
     * - File picker launcher: Handles the returned URI, initiates the file copying process using {@link #addFile(Context, Uri)},
     *   and emits the resulting {@link File} via {@link #gpxFilePickedAndFound}.
     *
     * @param fragmentActivity The {@link FragmentActivity} context to register the launchers with.
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

