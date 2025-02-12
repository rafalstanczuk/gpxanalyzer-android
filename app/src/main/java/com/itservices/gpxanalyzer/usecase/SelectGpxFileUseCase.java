package com.itservices.gpxanalyzer.usecase;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class SelectGpxFileUseCase {

    private final PublishSubject<File> selectedFile = PublishSubject.create();


    @Inject
    public SelectGpxFileUseCase() {}

    public Observable<File> getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File file) {
        selectedFile.onNext(file);
    }

    public void findFileFromStorage() {

    }

    public void selectFileFromFoundList() {

    }

}
