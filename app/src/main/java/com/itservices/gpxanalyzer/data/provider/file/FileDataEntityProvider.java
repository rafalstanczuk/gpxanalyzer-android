package com.itservices.gpxanalyzer.data.provider.file;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.raw.DataEntity;

import java.io.InputStream;
import java.util.Vector;

import io.reactivex.Single;

abstract class FileDataEntityProvider {

    public Single<Vector<DataEntity>> provideDefault() {
        return Single.just(new Vector<>());
    }

    public abstract Single<Vector<DataEntity>> provide(@NonNull InputStream inputStream);
}
