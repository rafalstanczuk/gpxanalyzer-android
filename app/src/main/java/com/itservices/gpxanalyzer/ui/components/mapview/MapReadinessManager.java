package com.itservices.gpxanalyzer.ui.components.mapview;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.osmdroid.views.MapView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class MapReadinessManager {
    private static final String TAG = MapReadinessManager.class.getSimpleName();

    private WeakReference<MapView> mapView;
    private final AtomicBoolean isMapReady = new AtomicBoolean(false);
    private PublishSubject<Boolean> mapReadyPublishSubject = PublishSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private OnMapReadyCallback mapReadyCallback;

    @Inject
    public MapReadinessManager() {}

    public void bind(MapView mapView) {
        this.mapView = new WeakReference<>(mapView);
        setupMapReadyListener();
    }

    private void setupMapReadyListener() {
        mapView.get().getTileProvider().getTileRequestCompleteHandlers().add(
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
            mapReadyPublishSubject.onNext(true);
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
        return mapReadyPublishSubject;
    }

    public boolean isMapReady() {
        return isMapReady.get();
    }

    public void dispose() {
        disposables.clear();
        mapReadyPublishSubject.onComplete();
    }

    public interface OnMapReadyCallback {
        void onMapReady();
    }
} 