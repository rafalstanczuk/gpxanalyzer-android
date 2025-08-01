package com.itservices.gpxanalyzer.domain.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaOAuthManager;
import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Service interface for handling GPX file updates, including scanning, miniature generation,
 * and geocoding operations.
 */
public interface GpxFileInfoUpdateService {
    /**
     * Checks if the necessary storage permissions are granted.
     * 
     * @param activity Current activity context
     * @return true if permissions are granted, false otherwise
     */
    boolean hasStoragePermissions(Activity activity);
    
    /**
     * Checks if the user is authenticated with Strava.
     * 
     * @return Single emitting true if authenticated, false otherwise
     */
    Single<Boolean> isStravaAuthenticated();
    
    /**
     * Initiates the Strava OAuth authentication flow.
     * 
     * @param activity Activity to start the OAuth flow from
     * @param requestCode Request code for activity result
     */
    void startStravaAuthentication(Activity activity, int requestCode);
    
    /**
     * Handles the result of the Strava OAuth flow.
     * 
     * @param requestCode Request code from startActivityForResult
     * @param resultCode Result code from the OAuth activity
     * @param data Intent data from the OAuth activity
     * @return Result of the OAuth flow
     */
    StravaOAuthManager.OAuthResult handleStravaAuthResult(int requestCode, int resultCode, Intent data);
    
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