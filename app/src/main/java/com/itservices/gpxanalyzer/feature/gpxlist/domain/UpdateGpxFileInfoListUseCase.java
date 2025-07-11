package com.itservices.gpxanalyzer.feature.gpxlist.domain;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.domain.service.GpxFileInfoUpdateService;
import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Use case responsible for updating the list of available GPX files and generating their corresponding map miniatures.
 * It orchestrates the process of:
 * <ol>
 *     <li>Searching the device storage for GPX files</li>
 *     <li>Generating miniatures for each file</li>
 *     <li>Performing geocoding for file locations</li>
 *     <li>Updating the database with the processed files</li>
 * </ol>
 * The process runs primarily on background threads (IO, Computation) and observes final results/errors on the main thread.
 */
@Singleton
public class UpdateGpxFileInfoListUseCase {
    private static final String TAG = UpdateGpxFileInfoListUseCase.class.getSimpleName();
    
    private final GpxFileInfoUpdateService gpxFileUpdateService;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public UpdateGpxFileInfoListUseCase(
            GpxFileInfoUpdateService gpxFileUpdateService) {
        this.gpxFileUpdateService = gpxFileUpdateService;
    }

    /**
     * Initiates the process of searching for GPX files, generating miniatures, performing geocoding,
     * and updating the database.
     *
     * @param context           The application context, needed for file searching
     * @param miniatureRenderer The {@link MiniatureMapView} instance used to render the miniatures
     * @return A {@link Completable} that completes when the entire process is finished
     */
    public Completable updateAndGenerateMiniatures(Context context, MiniatureMapView miniatureRenderer) {
        gpxFileUpdateService.setMiniatureRenderer(miniatureRenderer);

        return gpxFileUpdateService.scanFiles(context)
                .flatMapCompletable(files -> {
                    if (files.isEmpty()) {
                        Log.d(TAG, "No GPX files found to process.");
                        return Completable.complete();
                    }

                    Log.d(TAG, "Starting processing for " + files.size() + " items.");
                    
                    return Completable.concatArray(
                            gpxFileUpdateService.generateMiniatures(files),
                            gpxFileUpdateService.performGeocoding(files),
                            gpxFileUpdateService.updateWithGeocodedLocations(files),
                            gpxFileUpdateService.updateDatabase(files)
                    );
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    Log.i(TAG, "Processing completed successfully.");
                    disposables.clear();
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error during processing", throwable);
                    disposables.clear();
                });
    }
}

