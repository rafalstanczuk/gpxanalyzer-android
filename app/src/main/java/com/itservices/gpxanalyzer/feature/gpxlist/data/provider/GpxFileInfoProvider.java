package com.itservices.gpxanalyzer.feature.gpxlist.data.provider;

import android.content.Context;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.parser.GpxFileInfoParser;
import com.itservices.gpxanalyzer.core.data.provider.db.gpxfileinfo.GpxFileInfoRepository;
import com.itservices.gpxanalyzer.core.data.provider.file.GpxFileValidator;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.file.DeviceStorageFileProvider;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaApiFileProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class GpxFileInfoProvider {
    public static final String GPX_FILE_EXTENSION = ".gpx";
    public static final ProviderType DEFAULT_PROVIDER = ProviderType.LOCAL;
    private static final String[] MEDIA_STORE_SELECTION_ARGS = new String[]{"application/gpx+xml", "text/xml", "%.gpx"};

    @Inject
    GpxFileInfoParser parser;

    @Inject
    DeviceStorageFileProvider deviceStorage;

    @Inject
    StravaApiFileProvider stravaApi;

    private final GpxFileInfoRepository repository;

    @Inject
    public GpxFileInfoProvider(GpxFileInfoRepository repository) {
        this.repository = repository;
    }

    public Single<List<GpxFileInfo>> getAndUpdate(Context context, ProviderType providerType) {
        return switch (providerType) {
            case LOCAL -> getAndUpdateFromLocalProvider(context);
            case ONLINE -> getAndUpdateFromOnlineProvider(context);
        };
    }

    private Single<List<GpxFileInfo>> getAndUpdateFromOnlineProvider(Context context) {
        return stravaApi.getFiles(context, file -> parser.parse(file) )
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    private Single<List<GpxFileInfo>> getAndUpdateFromLocalProvider(Context context) {
        return deviceStorage.getFiles(
                        context, file -> parser.parse(file),
                        GPX_FILE_EXTENSION, MEDIA_STORE_SELECTION_ARGS
                )
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<List<GpxFileInfo>> getCached() {
        return repository.getAll()
                .map(GpxFileValidator::validateAndFilterFiles);
    }

    public Completable setCached(List<GpxFileInfo> gpxFileInfoList) {
        return repository.deleteAll()
                .andThen(insertIntoDb(gpxFileInfoList));
    }

    private Completable insertIntoDb(List<GpxFileInfo> gpxFileInfoList) {
        //Log.d(GpxFileInfoProvider.class.getSimpleName(), "insertIntoDb() called with: gpxFileInfoList = [" + gpxFileInfoList + "]");
        return repository.insertAll(gpxFileInfoList);
    }
}
