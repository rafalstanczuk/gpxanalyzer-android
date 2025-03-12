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
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

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

    @Inject
    public SelectGpxFileUseCase() {
    }

    public Observable<Boolean> getIsGpxFileFound() {
        return isGpxFileFound;
    }

    public Observable<Boolean> getPermissionsGranted() {
        return permissionsGranted;
    }

    public Observable<List<File>> getGpxFileFoundList() {
        return gpxFileFoundList;
    }

    public void setSelectedFile(File file) {
        selectedFile = file;
    }

    @Nullable
    public File getSelectedFile() {
        return selectedFile;
    }

    public List<File> getFileFoundList() {
        return fileFoundList;
    }

    public Observable<List<File>> loadLocalGpxFiles(Context context) {
        return Observable.fromCallable(() -> {
            fileFoundList = FileProviderUtils.getFilesByExtension(context, GPX_FILE_EXTENSION);
            return fileFoundList;
        });
    }

    public Observable<File> addFile(Context context, Uri uri) {
        return Observable.fromCallable(() -> {
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

    public Observable<Boolean> checkAndRequestPermissions(FragmentActivity requireActivity) {
        // Check and request storage permissions if needed
        if (PermissionUtils.hasFileAccessPermissions(requireActivity)) {
            return Observable.just(true);
        } else {
            PermissionUtils.requestFileAccessPermissions(permissionLauncher);
        }

        return permissionsGranted;
    }

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
