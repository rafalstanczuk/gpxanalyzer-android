package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class FileSelectorViewModel extends ViewModel {

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;
    private final MutableLiveData<List<File>> filesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFileFoundLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> requestPermissionsLiveData = new MutableLiveData<>();

    private Disposable disposableLoadLocal;
    private Disposable disposableIsFileFound;

    private Disposable disposableFileListFound;
    private Disposable disposableRequestPermissions;

    private Disposable disposableCheckAndRequestPermissions;

    public LiveData<List<File>> getFoundFileList() {
        return filesLiveData;
    }

    @Inject
    public FileSelectorViewModel() {
    }


    public void loadLocalFiles(Context context) {
        ConcurrentUtil.tryToDispose(disposableLoadLocal);

        disposableLoadLocal = selectGpxFileUseCase.loadLocalGpxFiles(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filesLiveData::setValue);
    }

    @NonNull
    public LiveData<Boolean> getPermissionsGrantedLiveData() {
        return requestPermissionsLiveData;
    }

    public Boolean getPermissionsGranted() {
        return requestPermissionsLiveData.getValue() != null ? requestPermissionsLiveData.getValue() : false;
    }

    public LiveData<Boolean> getFileFound() {
        return isFileFoundLiveData;
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
        ConcurrentUtil.tryToDispose(disposableIsFileFound);
        disposableIsFileFound = selectGpxFileUseCase.getIsGpxFileFound()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFileFoundLiveData::setValue);

        ConcurrentUtil.tryToDispose(disposableRequestPermissions);
        disposableRequestPermissions = selectGpxFileUseCase.getPermissionsGranted()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestPermissionsLiveData::setValue);

        ConcurrentUtil.tryToDispose(disposableFileListFound);
        disposableFileListFound = selectGpxFileUseCase.getGpxFileFoundList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(filesLiveData::setValue);
    }
}
