package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.utils.FileProviderUtils;

import java.io.File;
import java.util.List;

public class FileSelectorViewModel extends ViewModel {

    private final MutableLiveData<List<File>> filesLiveData = new MutableLiveData<>();

    public LiveData<List<File>> getFiles() {
        return filesLiveData;
    }

    /**
     * Loads files with the specified extension.
     * Delegates file loading to FileProviderUtils.
     */
    public void loadFiles(Context context, String extension) {
        List<File> files = FileProviderUtils.getFilesByExtension(context, extension);
        filesLiveData.setValue(files);
    }

    /**
     * Adds a file from a given URI.
     * Delegates file handling to FileProviderUtils.
     */
    public void addFile(Context context, Uri uri) {
        File file = FileProviderUtils.copyUriToAppStorage(context, uri);
        if (file != null) {
            List<File> currentFiles = filesLiveData.getValue();
            if (currentFiles != null && !currentFiles.contains(file)) {
                currentFiles.add(file);
                filesLiveData.setValue(currentFiles);
            }
        }
    }
}
