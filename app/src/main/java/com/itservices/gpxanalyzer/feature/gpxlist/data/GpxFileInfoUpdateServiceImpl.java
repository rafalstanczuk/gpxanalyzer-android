package com.itservices.gpxanalyzer.feature.gpxlist.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.core.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.core.data.provider.geocoding.BaseGeocodingRepository;
import com.itservices.gpxanalyzer.core.data.provider.geocoding.android.GeocodingAndroidRepository;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.GpxFileInfoProvider;
import com.itservices.gpxanalyzer.core.data.provider.db.geocoding.GeocodingLocalRepository;
import com.itservices.gpxanalyzer.domain.service.GpxFileInfoUpdateService;

import com.itservices.gpxanalyzer.core.events.EventProgress;
import com.itservices.gpxanalyzer.core.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType;
import com.itservices.gpxanalyzer.core.ui.components.miniature.GpxFileInfoMiniatureProvider;
import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class GpxFileInfoUpdateServiceImpl implements GpxFileInfoUpdateService {
    private static final String TAG = GpxFileInfoUpdateServiceImpl.class.getSimpleName();

    private final GpxFileInfoProvider gpxFileInfoProvider;
    private final GpxFileInfoMiniatureProvider miniatureProvider;
    private final BaseGeocodingRepository geocodingRepository;
    private final GeocodingLocalRepository geocodingLocalRepository;
    private final GlobalEventWrapper globalEventWrapper;
    private MiniatureMapView miniatureRenderer;

    @Inject
    public GpxFileInfoUpdateServiceImpl(
            GpxFileInfoProvider gpxFileInfoProvider,
            GpxFileInfoMiniatureProvider miniatureProvider,
            GeocodingAndroidRepository geocodingRepository,
            GeocodingLocalRepository geocodingLocalRepository,
            GlobalEventWrapper globalEventWrapper) {
        this.gpxFileInfoProvider = gpxFileInfoProvider;
        this.miniatureProvider = miniatureProvider;
        this.geocodingRepository = geocodingRepository;
        this.geocodingLocalRepository = geocodingLocalRepository;
        this.globalEventWrapper = globalEventWrapper;
    }

    @Override
    public void setMiniatureRenderer(MiniatureMapView renderer) {
        this.miniatureRenderer = renderer;
    }

    @Override
    public Single<List<GpxFileInfo>> scanFiles(Context context) {
        return gpxFileInfoProvider.searchAndParseGpxFilesRecursively(context);
    }

    @Override
    public Completable generateMiniatures(List<GpxFileInfo> files) {
        if (files.isEmpty()) {
            return Completable.complete();
        }

        AtomicInteger processedCount = new AtomicInteger(0);
        return Observable.fromIterable(files)
                .concatMapCompletable(file -> {
                    //Log.d(TAG, "Generating miniature for: " + file.file().getName());
                    return miniatureProvider.requestForGenerateMiniature(miniatureRenderer, file)
                            .andThen(miniatureProvider.getGpxFileInfoWithMiniature()
                                    .filter(emittedItem -> emittedItem.equals(file))
                                    .firstOrError()
                                    .doOnSuccess(updatedItem -> {
                                        int current = processedCount.incrementAndGet();

                                        globalEventWrapper.onNext(EventProgress.create(
                                                PercentageUpdateEventSourceType.MINIATURE_GENERATION_PROGRESS,
                                                current, files.size()
                                        ));
                                    })
                            )
                            .ignoreElement();
                }, 1) // Process one file at a time
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable performGeocoding(List<GpxFileInfo> files) {
        return Observable.fromIterable(files)
                .filter(gpxFileInfo -> gpxFileInfo.firstPointLocation() != null)
                .flatMapMaybe(this::filterWithMissedGeocoding)
                .toList()
                .flatMapCompletable(this::requestForGeocodingLocations);
    }

    private Completable requestForGeocodingLocations(List<Location> locations) {
        if (locations.isEmpty()) {
            return Completable.complete();
        }

        return geocodingRepository.batchReverseGeocode(locations)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(throwable -> {
                    Log.e(TAG, "Error during batch reverse geocoding", throwable);
                })
                .flatMapCompletable(this::updateLocalGeocodingRepositoryWithResults);
    }

    private Completable updateLocalGeocodingRepositoryWithResults(ArrayList<GeocodingResult> geocodingResults) {
        if (geocodingResults.isEmpty()) {
            Log.w(TAG, "No geocoding results obtained");
            return Completable.complete();
        }

        return geocodingLocalRepository.insertAll(geocodingResults)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error during geocoding results insertion", throwable);
                    return Completable.complete(); // Continue even if insertion fails
                })
                .doOnComplete(() -> {
                    Log.i(TAG, "GeocodingLocalRepository insertAll completed successfully.");
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error during GeocodingLocalRepository insertAll", throwable);
                });
    }

    private Maybe<Location> filterWithMissedGeocoding(GpxFileInfo gpxFileInfo) {
        // Check if gpxFileInfo already has geocoded location
        if (gpxFileInfo.geoCodedLocation() == null || gpxFileInfo.geoCodedLocation().isEmpty()) {
            // Try to find existing geocoding in local repository
            Log.d(TAG, "performGeocoding() Try to find existing geocoding in local repository  gpxFileInfo.firstPointLocation() = [" + gpxFileInfo.firstPointLocation() + "]");

            return tryToUpdateWithLocalRepositoryOrReturnToUpdateLater(gpxFileInfo);
        }
        return Maybe.empty();
    }

    private Maybe<Location> tryToUpdateWithLocalRepositoryOrReturnToUpdateLater(GpxFileInfo gpxFileInfo) {
        return geocodingLocalRepository.findByLocationWithEpsilonGrade(gpxFileInfo.firstPointLocation(), 0.001)
                .firstElement()
                .flatMap(results -> {
                    if (!results.isEmpty()) {
                        // If found, update the gpxFileInfo with existing geocoding
                        Log.d(TAG, "performGeocoding() found, update the gpxFileInfo with existing geocoding = [" + results.get(0).getFormattedAddress() + "]");

                        gpxFileInfo.setGeoCodedLocation(results.get(0).getFormattedAddress());
                        return Maybe.empty(); // Don't need to geocode this location
                    }

                    Log.w(TAG, "performGeocoding() Need to geocode this location = [" + gpxFileInfo.firstPointLocation() + "]");
                    return Maybe.just(gpxFileInfo.firstPointLocation()); // Need to geocode this location
                })
                .onErrorResumeNext(throwable -> {
                    Log.e(TAG, "Error checking local geocoding for file: " + gpxFileInfo.file().getName(), throwable);
                    // If there's an error checking local geocoding, try to geocode anyway
                    return Maybe.just(gpxFileInfo.firstPointLocation());
                });
    }

    @Override
    public Completable updateWithGeocodedLocations(List<GpxFileInfo> files) {
        return Observable.fromIterable(files)
                .filter(fileInfo -> fileInfo.firstPointLocation() != null)
                .flatMapSingle(fileInfo ->
                    geocodingLocalRepository.findByLocationWithEpsilonGrade(fileInfo.firstPointLocation(), 0.001)
                        .firstOrError()
                        .onErrorResumeNext(throwable -> {
                            Log.e(TAG, "Error during geocoding for file: " + fileInfo.file().getName(), throwable);
                            return Single.just(new ArrayList<>());
                        })
                        .map(results -> {
                            //Log.d(TAG, "updateWithGeocodedLocations() called with: results = [" + results + "]");
                            if (!results.isEmpty()) {
                                fileInfo.setGeoCodedLocation(results.get(0).getFormattedAddress());
                            }
                            return fileInfo;
                        })
                )
                .ignoreElements()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable updateDatabase(List<GpxFileInfo> files) {
        //Log.d(TAG, "updateDatabase() called with: files = [" + files + "]");

        return gpxFileInfoProvider.replaceAll(files)
                .doOnSubscribe(disposable -> {
                    globalEventWrapper.onNext(
                            EventProgress.create(PercentageUpdateEventSourceType.UPDATING_RESOURCES_PROCESSING, 0,1)
                    );
                })
                .doOnComplete(() -> {
                    globalEventWrapper.onNext(
                            EventProgress.create(PercentageUpdateEventSourceType.UPDATING_RESOURCES_PROCESSING, 1,1)
                    );
                });
    }
}