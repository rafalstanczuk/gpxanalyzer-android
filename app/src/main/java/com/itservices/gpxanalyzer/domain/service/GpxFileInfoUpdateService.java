package com.itservices.gpxanalyzer.domain.service;

import android.content.Context;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Service interface for handling GPX file updates, including scanning, miniature generation,
 * and geocoding operations.
 */
public interface GpxFileInfoUpdateService {
    /**
     * Scans for GPX files in the device storage.
     *
     * @param context The application context
     * @return Single emitting the list of found GPX files
     */
    Single<List<GpxFileInfo>> scanFiles(Context context);

    /**
     * Generates miniatures for a list of GPX files.
     *
     * @param files The list of GPX files to process
     * @return Completable that completes when all miniatures are generated
     */
    Completable generateMiniatures(List<GpxFileInfo> files);

    /**
     * Performs geocoding for a list of GPX files.
     *
     * @param files The list of GPX files to process
     * @return Completable that completes when all geocoding is done
     */
    Completable performGeocoding(List<GpxFileInfo> files);

    /**
     * Updates the database with the processed GPX files.
     *
     * @param files The list of processed GPX files
     * @return Completable that completes when the database is updated
     */
    Completable updateDatabase(List<GpxFileInfo> files);


    void setMiniatureRenderer(MiniatureMapView miniatureRenderer);

    Completable updateWithGeocodedLocations(List<GpxFileInfo> files);
}