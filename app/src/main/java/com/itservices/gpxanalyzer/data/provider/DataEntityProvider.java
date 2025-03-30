package com.itservices.gpxanalyzer.data.provider;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.raw.DataEntity;

import java.io.InputStream;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

abstract class DataEntityProvider {
    protected final PublishSubject<Integer> percentageProgressSubject = PublishSubject.create();

    public Single<Vector<DataEntity>> provideDefault() {
        return Single.just(new Vector<>());
    }

    public Observable<Integer> getPercentageProgress() {
        return percentageProgressSubject;
    }

    public abstract Single<Vector<DataEntity>> provide(@NonNull InputStream inputStream);
}
