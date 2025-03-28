package com.itservices.gpxanalyzer.ui.mapview;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.osmdroid.views.MapView;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public class MapReadinessManager {
    private static final String TAG = "MapReadinessManager";

    private final MapView mapView;
    private final AtomicBoolean isMapReady = new AtomicBoolean(false);
    private final BehaviorSubject<Boolean> mapReadySubject = BehaviorSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private OnMapReadyCallback mapReadyCallback;

    public MapReadinessManager(MapView mapView) {
        this.mapView = mapView;
        setupMapReadyListener();
    }

    private void setupMapReadyListener() {
        mapView.getTileProvider().getTileRequestCompleteHandlers().add(
                new Handler(
                        Looper.getMainLooper(),
                        msg -> {
                            setMapReady();
                            return false;
                        })
        );
    }

    private void setMapReady() {
        if (isMapReady.compareAndSet(false, true)) {
            Log.d(TAG, "Map is ready");
            mapReadySubject.onNext(true);
            if (mapReadyCallback != null) {
                mapReadyCallback.onMapReady();
            }
        }
    }

    public void setOnMapReadyCallback(OnMapReadyCallback callback) {
        this.mapReadyCallback = callback;
        if (isMapReady.get() && callback != null) {
            callback.onMapReady();
        }
    }

    public Observable<Boolean> getMapReadyObservable() {
        return mapReadySubject;
    }

    public boolean isMapReady() {
        return isMapReady.get();
    }

    public void dispose() {
        disposables.clear();
        mapReadySubject.onComplete();
    }

    public interface OnMapReadyCallback {
        void onMapReady();
    }
} 