package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.usecase.MultipleSyncedGpxChartUseCase;
import com.itservices.gpxanalyzer.utils.FileProviderUtils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FileSelectorViewModel extends ViewModel {

    @Inject
    MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;
    private final MutableLiveData<List<File>> filesLiveData = new MutableLiveData<>();

    public LiveData<List<File>> getFiles() {
        return filesLiveData;
    }

    @Inject
    public FileSelectorViewModel() {
    }

    /**
     * Loads files with the specified extension.
     * Delegates file loading to FileProviderUtils.
     */
    public void loadFiles(Context context, String extension) {
        List<File> files = FileProviderUtils.getFilesByExtension(context, extension);
        filesLiveData.setValue(files);
    }

    public void selectFile(File gpxFile) {
        multipleSyncedGpxChartUseCase.selectFile(gpxFile);
    }

    /**
     * Adds a file from a given URI.
     * Delegates file handling to FileProviderUtils.
     */
    public File addFile(Context context, Uri uri, String fileExtension) {
        File file = FileProviderUtils.copyUriToAppStorage(context, uri, fileExtension);
        if (file != null) {
            List<File> currentFiles = filesLiveData.getValue();
            if (currentFiles != null && !currentFiles.contains(file)) {
                currentFiles.add(file);
                filesLiveData.setValue(currentFiles);
            }
        }

        return file;
    }
}
