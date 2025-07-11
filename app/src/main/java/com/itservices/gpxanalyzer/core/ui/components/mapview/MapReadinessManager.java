package com.itservices.gpxanalyzer.core.ui.components.mapview;

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

/**
 * Manages the readiness state of an osmdroid {@link MapView}.
 * The map is considered "ready" once the tile provider has completed its initial requests,
 * indicating that the base map tiles are likely loaded and displayed.
 * It provides both a callback interface ({@link OnMapReadyCallback}) and an RxJava {@link Observable}
 * to notify interested components when the map becomes ready.
 */
public class MapReadinessManager {
    private static final String TAG = MapReadinessManager.class.getSimpleName();

    /** Weak reference to the MapView being managed. */
    private WeakReference<MapView> mapView;
    /** Atomic flag indicating if the map has reached the ready state. */
    private final AtomicBoolean isMapReady = new AtomicBoolean(false);
    /** RxJava Subject that emits `true` when the map becomes ready. */
    private PublishSubject<Boolean> mapReadyPublishSubject = PublishSubject.create();
    /** Manages RxJava subscriptions. */
    private final CompositeDisposable disposables = new CompositeDisposable();
    /** Optional callback interface listener. */
    private OnMapReadyCallback mapReadyCallback;

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public MapReadinessManager() {}

    /**
     * Binds the manager to a specific {@link MapView} instance.
     * Sets up the listener to detect when map tiles are ready.
     *
     * @param mapView The {@link MapView} instance to monitor.
     */
    public void bind(MapView mapView) {
        this.mapView = new WeakReference<>(mapView);
        setupMapReadyListener();
    }

    /**
     * Sets up a handler to listen for tile request completion events from the MapView's tile provider.
     * When a completion message is received, it triggers {@link #setMapReady()}.
     */
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

    /**
     * Marks the map as ready if it wasn't already.
     * Atomically sets the {@link #isMapReady} flag to true.
     * If the state changes, it emits `true` to the {@link #mapReadyPublishSubject}
     * and invokes the {@link #mapReadyCallback} if it's set.
     */
    private void setMapReady() {
        if (isMapReady.compareAndSet(false, true)) {
            Log.d(TAG, "Map is ready");
            mapReadyPublishSubject.onNext(true);
            if (mapReadyCallback != null) {
                mapReadyCallback.onMapReady();
            }
        }
    }

    /**
     * Sets a callback to be invoked when the map becomes ready.
     * If the map is already ready when this method is called, the callback is invoked immediately.
     *
     * @param callback The {@link OnMapReadyCallback} listener.
     */
    public void setOnMapReadyCallback(OnMapReadyCallback callback) {
        this.mapReadyCallback = callback;
        if (isMapReady.get() && callback != null) {
            callback.onMapReady();
        }
    }

    /**
     * Returns an {@link Observable} that emits `true` exactly once when the map becomes ready.
     *
     * @return An Observable<Boolean> representing map readiness.
     */
    public Observable<Boolean> getMapReadyObservable() {
        return mapReadyPublishSubject;
    }

    /**
     * Checks if the map is currently considered ready.
     *
     * @return {@code true} if the map is ready, {@code false} otherwise.
     */
    public boolean isMapReady() {
        return isMapReady.get();
    }

    /**
     * Disposes of any RxJava resources and completes the {@link #mapReadyPublishSubject}.
     * Should be called when the associated MapView or component is destroyed.
     */
    public void dispose() {
        disposables.clear();
        mapReadyPublishSubject.onComplete();
    }

    /**
     * Callback interface for receiving map readiness notifications.
     */
    public interface OnMapReadyCallback {
        /**
         * Called when the map is considered ready for interaction (initial tiles loaded).
         */
        void onMapReady();
    }
} 