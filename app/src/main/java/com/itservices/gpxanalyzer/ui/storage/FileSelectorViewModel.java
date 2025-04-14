package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.usecase.SearchFileUseCase;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class FileSelectorViewModel extends ViewModel {

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;
    @Inject
    GlobalEventWrapper globalEventWrapper;

    private final MutableLiveData<Boolean> requestPermissionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> searchFilesProgressLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<GpxFileInfo>> filesInfoLiveData = new MutableLiveData<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private Disposable disposableRequestPermissions;
    private Disposable disposableCheckAndRequestPermissions;
    private Disposable disposableSearchProgress;

    @Inject
    public FileSelectorViewModel() {
    }

    public LiveData<List<GpxFileInfo>> getFoundFileListLiveData() {
        return filesInfoLiveData;
    }

    public void receiveRecentFoundFileList() {
        filesInfoLiveData.setValue(selectGpxFileUseCase.getGpxFileInfoList());
    }

    public LiveData<Integer> getSearchFilesProgress() {
        return searchFilesProgressLiveData;
    }

    public void searchGpxFilesRecursively(Context context) {
        Log.d(FileSelectorViewModel.class.getSimpleName(), "searchGpxFilesRecursively() called with: context = [" + context + "]");

        // Clear previous search progress
        searchFilesProgressLiveData.setValue(0);

        // Subscribe to search progress
        disposableSearchProgress = globalEventWrapper.getEventProgressFrom(SearchFileUseCase.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::accept);

        // Start the recursive search
        disposables.add(selectGpxFileUseCase.searchAndParseGpxFilesRecursively(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        filesInfoLiveData::setValue,
                        error -> {
                            Log.e("FileSelectorViewModel", "Error searching for GPX files", error);
                            filesInfoLiveData.setValue(null);
                        }
                ));
    }

    @NonNull
    public LiveData<Boolean> getPermissionsGrantedLiveData() {
        return requestPermissionsLiveData;
    }

    public Boolean getPermissionsGranted() {
        return requestPermissionsLiveData.getValue() != null ? requestPermissionsLiveData.getValue() : false;
    }

    public void selectFile(File gpxFile) {
        selectGpxFileUseCase.setSelectedFile(gpxFile);
    }

    public void checkAndRequestPermissions(FragmentActivity requireActivity) {
        ConcurrentUtil.tryToDispose(disposableCheckAndRequestPermissions);
        disposableCheckAndRequestPermissions = selectGpxFileUseCase.checkAndRequestPermissions(requireActivity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestPermissionsLiveData::setValue);
    }

    public void openFilePicker() {
        selectGpxFileUseCase.openFilePicker();
    }

    public void init() {
        ConcurrentUtil.tryToDispose(disposableRequestPermissions);
        disposableRequestPermissions = selectGpxFileUseCase.getPermissionsGranted()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestPermissionsLiveData::setValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        ConcurrentUtil.tryToDispose(disposableRequestPermissions);
        ConcurrentUtil.tryToDispose(disposableCheckAndRequestPermissions);
        ConcurrentUtil.tryToDispose(disposableSearchProgress);
    }

    private void accept(EventProgress event) {
        searchFilesProgressLiveData.setValue(event.percentage());
    }
}
