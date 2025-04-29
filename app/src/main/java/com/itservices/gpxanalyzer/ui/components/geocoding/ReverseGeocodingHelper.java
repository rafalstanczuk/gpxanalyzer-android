package com.itservices.gpxanalyzer.ui.components.geocoding;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingNetworkRepository;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Helper class for performing reverse geocoding operations.
 * Simplifies the process of converting coordinates to human-readable addresses.
 */
public class ReverseGeocodingHelper {
    private static final String TAG = "ReverseGeocodingHelper";

    private final GeocodingNetworkRepository geocodingRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    private OnReverseGeocodingListener listener;

    @Inject
    public ReverseGeocodingHelper(GeocodingNetworkRepository geocodingRepository) {
        this.geocodingRepository = geocodingRepository;
    }

    /**
     * Sets a listener to be notified of reverse geocoding results.
     */
    public void setListener(OnReverseGeocodingListener listener) {
        this.listener = listener;
    }

    /**
     * Releases resources when the helper is no longer needed.
     * Should be called in onDestroy() or similar lifecycle method.
     */
    public void dispose() {
        disposables.clear();
    }

    /**
     * Performs reverse geocoding for a GeoPoint.
     * 
     * @param point Location containing coordinates to reverse geocode
     */
    public void reverseGeocode(Location point) {
        if (point == null) {
            if (listener != null) {
                listener.onReverseGeocodingError("Invalid coordinates");
            }
            return;
        }
        
        if (listener != null) {
            listener.onReverseGeocodingStarted();
        }
        
        Disposable disposable = geocodingRepository.reverseGeocode(point)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (listener != null) {
                                listener.onReverseGeocodingSuccess(result);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Reverse geocoding error", error);
                            if (listener != null) {
                                listener.onReverseGeocodingError(error.getMessage());
                            }
                        }
                );
        
        disposables.add(disposable);
    }

    /**
     * Interface for notifying about reverse geocoding operations.
     */
    public interface OnReverseGeocodingListener {
        void onReverseGeocodingStarted();
        void onReverseGeocodingSuccess(GeocodingResult result);
        void onReverseGeocodingError(String errorMessage);
    }
} 