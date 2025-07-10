package com.itservices.gpxanalyzer.ui.components.mapview;

import static org.osmdroid.util.BoundingBox.fromGeoPoints;

import android.graphics.Paint;
import android.util.Log;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.ui.components.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.events.EventVisibleChartEntriesTimestamp;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.data.cache.rawdata.GeoPointCache;
import com.itservices.gpxanalyzer.data.model.entity.GeoPointEntity;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Controller class responsible for managing the logic, interactions, and data display
 * for a {@link DataMapView} instance.
 *
 * It acts as a bridge between the MapView, data sources ({@link GeoPointCache}),
 * application-wide events (via {@link GlobalEventWrapper}), and UI actions.
 *
 * Key responsibilities:
 * - Binding to a {@link DataMapView} instance.
 * - Managing map readiness via {@link MapReadinessManager}.
 * - Handling map lifecycle events (scroll, zoom) via {@link MapListener}.
 * - Implementing map control operations (center, zoom, bounding box) via {@link MapOperations}.
 * - Managing map overlays (markers, polylines) via {@link MapOverlayOperations}.
 * - Observing application events (chart entry selection, visible range changes, request status)
 *   and updating the map accordingly (e.g., showing selected point, drawing polylines).
 * - Loading initial map data (full track polyline, bounding box).
 * - Handling RxJava subscriptions and resource disposal.
 */
public class MapViewController implements MapListener, MapReadinessManager.OnMapReadyCallback, MapOperations, MapOverlayOperations {
    private static final String TAG = MapViewController.class.getSimpleName();

    //private static IconOverlay MAP_ICON_OVERLAY;
    private final List<Marker> markerList = new ArrayList<>();
    private final List<Polyline> polylineList = new ArrayList<>();
    /** Subject to emit the GeoPointEntity when a point is selected on the map (potentially for future use). */
    private final PublishSubject<GeoPointEntity> selectedPointOnMap = PublishSubject.create();
    /** Manages RxJava subscriptions for this controller. */
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private WeakReference<DataMapView> mapView;
    private List<Overlay> mapOverlays;
    private IMapController mapController;
    /** Weak reference to the currently selected marker overlay on the map. */
    private WeakReference<Marker> currentSelectedMarker = new WeakReference<>(null);
    /** Reference to the polyline representing the currently visible segment based on chart interactions. */
    private Polyline boundaryPolyline = null;
    /** Reference to the polyline representing the full GPX track. */
    private Polyline fullPolyline = null;
    /** Weak reference to the GeoPointEntity corresponding to the currently selected point. */
    private WeakReference<GeoPointEntity> currentSelectedPoint = new WeakReference<>(null);
    /** Atomic reference holding the start/end timestamps of the currently visible segment from chart events. */
    private AtomicReference<Vector<Long>> currentVisible = new AtomicReference<>();
    /** Stores the last received RequestStatus to be processed once the map becomes ready. */
    private final AtomicReference<RequestStatus> requestStatusToHandleOnMapReadyAtomic = new AtomicReference<>();

    @Inject
    protected MapReadinessManager readinessManager;
    @Inject
    GeoPointCache geoPointCachedProvider;
    @Inject
    GlobalEventWrapper mapChartGlobalEventWrapper;

    @Inject
    public MapViewController() {
    }

    /**
     * Helper method to create a new BoundingBox expanded by a percentage.
     *
     * @param boundingBox    The original bounding box.
     * @param paddingPercent The padding percentage (e.g., 0.1 for 10%).
     * @return A new, padded BoundingBox.
     */
    private static BoundingBox createPaddedBoundingBox(BoundingBox boundingBox, double paddingPercent) {
        double latPadding = (boundingBox.getLatNorth() - boundingBox.getLatSouth()) * paddingPercent;
        double lonPadding = (boundingBox.getLonEast() - boundingBox.getLonWest()) * paddingPercent;

        return new BoundingBox(
                boundingBox.getLatNorth() + latPadding,
                boundingBox.getLonEast() + lonPadding,
                boundingBox.getLatSouth() - latPadding,
                boundingBox.getLonWest() - lonPadding
        );
    }

    /**
     * Binds this controller to a specific {@link DataMapView} instance.
     * Initializes references to the map, its overlays, and its controller.
     * Sets up the {@link MapReadinessManager}, map listeners, and observers for application events.
     *
     * @param mapView The {@link DataMapView} to control.
     */
    public void bind(DataMapView mapView) {
        Log.d(TAG, "bind() called with: mapView = [" + mapView + "]");

        this.mapView = new WeakReference<>(mapView);
        mapOverlays = mapView.getOverlays();
        addCopyrightOverlay(mapView);
        mapController = mapView.getController();

        readinessManager.bind(mapView);
        readinessManager.setOnMapReadyCallback(this);
        mapView.addMapListener(this);

        observeEntrySelectionOnCharts();
        observeVisibleEntriesBoundaryOnCharts();
        observeRequestStatus();
    }

    /**
     * Adds the copyright overlay to the map.
     *
     * @param mapView The map view instance.
     */
    private void addCopyrightOverlay(DataMapView mapView) {
        mapOverlays.add(new CopyrightOverlay(mapView.getContext()));
    }

    /**
     * Loads the initial map state, including setting the bounding box to fit the track,
     * drawing the full track polyline, and potentially the initial visible boundary polyline
     * and selected marker.
     */
    public void loadInitialData() {
        Log.d(TAG, "loadInitialData() called");

        // GeoPoint center = geoPointCachedProvider.getGeoPointStatistics().getCenter();
        // setCenter(center);

        updateInitialBoundingBox();

        addOrUpdateFullPolyline();
        List<GeoPoint> points = geoPointCachedProvider.getGeoPointVector();
        addOrUpdateBoundaryPolyline(points);

        createOrUpdateSelectedMarker(currentSelectedPoint.get());

        //mapOverlays.remove(MAP_ICON_OVERLAY);

        invalidate();
    }

    /**
     * Subscribes to {@link RequestStatus} updates from the {@link GlobalEventWrapper}
     * to handle application state changes (e.g., loading, data ready).
     */
    private void observeRequestStatus() {
        Log.d(TAG, "observeRequestStatus() called");

        compositeDisposable.add(mapChartGlobalEventWrapper
                .getRequestStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(status -> Log.d(TAG, "Received status: " + status))
                .doOnError(error -> Log.e(TAG, "Error in status subscription", error))
                .subscribe(this::handleAction));
    }

    /**
     * Handles incoming {@link RequestStatus} events.
     * If the map is not ready, the status is stored and handled later in {@link #onMapReady()}.
     * Otherwise, updates the loading overlay visibility and triggers actions like clearing overlays
     * or loading initial data based on the status.
     *
     * @param requestStatus The received status event.
     */
    private void handleAction(RequestStatus requestStatus) {
        Log.d(TAG, "handleAction() called with: requestStatus = [" + requestStatus + "]");

        if (requestStatus != null) {

            if (!readinessManager.isMapReady()) {
                requestStatusToHandleOnMapReadyAtomic.set(requestStatus);

                Log.d(TAG, "Map not ready, ignoring status: " + requestStatus);
                return;
            }

            requestStatusToHandleOnMapReadyAtomic.set(null);

            mapView.get().overlayViewToReloadLayoutView.handleAction(requestStatus);

            switch (requestStatus) {
                case NEW_DATA_LOADING, SELECTED_FILE -> clearOverlays();
                case DATA_LOADED, PROCESSING, PROCESSED, CHART_UPDATING,  DONE -> {
                    if (polylineList.isEmpty()) {
                        loadInitialData();
                    } else {
                        Log.d(TAG, "Map has data already, ignoring status: " + requestStatus);
                    }
                }
                default -> {
                }
            }
        }
    }

    /**
     * Subscribes to {@link EventVisibleChartEntriesTimestamp} events from the {@link GlobalEventWrapper}
     * to update the highlighted polyline segment on the map based on chart visibility changes.
     */
    private void observeVisibleEntriesBoundaryOnCharts() {
        Log.d(TAG, "observeVisibleEntriesBoundaryOnCharts() called");

        compositeDisposable.add(mapChartGlobalEventWrapper
                .getEventVisibleChartEntriesTimestamp()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleEvent));
    }

    /**
     * Subscribes to {@link com.itservices.gpxanalyzer.events.EventEntrySelection} events from the {@link GlobalEventWrapper}
     * to update the selected marker on the map when a point is selected on a chart.
     */
    private void observeEntrySelectionOnCharts() {
        Log.d(TAG, "observeEntrySelectionOnCharts() called");

        compositeDisposable.add(mapChartGlobalEventWrapper
                .getEventEntrySelection()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventEntrySelection -> {
                            CurveEntry curveEntry = eventEntrySelection.curveEntry();

                            Object extraData = curveEntry.getDataEntity().getExtraData();

                            if (extraData instanceof GeoPointEntity) {
                                selectPoint((GeoPointEntity) extraData);
                                invalidate();
                            }
                        }
                ));
    }

    /**
     * Selects a specific point on the map, updating the internal state and the selected marker overlay.
     *
     * @param geoPointEntity The {@link GeoPointEntity} representing the point to select.
     */
    public void selectPoint(GeoPointEntity geoPointEntity) {
        //Log.d(TAG, "selectPoint() called with: geoPointEntity = [" + geoPointEntity + "]");
        currentSelectedPoint = new WeakReference<>(geoPointEntity);

        createOrUpdateSelectedMarker(currentSelectedPoint.get());
    }

    /**
     * Returns an Observable that emits true when the map is ready.
     *
     * @return Observable<Boolean>
     */
    public Observable<Boolean> getMapReadyObservable() {
        return readinessManager.getMapReadyObservable();
    }

    /**
     * {@link MapListener} callback for scroll events. Currently unused.
     */
    @Override
    public boolean onScroll(ScrollEvent event) {
        //Log.d(TAG, "onScroll() called with: event = [" + event + "]");
        return false;
    }

    /**
     * {@link MapListener} callback for zoom events. Currently unused.
     */
    @Override
    public boolean onZoom(ZoomEvent event) {
        //Log.d(TAG, "onZoom() called with: event = [" + event + "]");
        return false;
    }

    /**
     * {@link MapReadinessManager.OnMapReadyCallback} implementation.
     * Called when the map tiles are considered ready.
     * Handles any pending {@link RequestStatus} that was received before the map was ready.
     */
    @Override
    public void onMapReady() {
        Log.d(TAG, "onMapReady() called");

        //mapOverlayMode();

        handleAction(requestStatusToHandleOnMapReadyAtomic.get());
    }

/*    private void mapOverlayMode() {
        MAP_ICON_OVERLAY = new IconOverlay(
                mapView.get().getMapCenter(),
                DrawableUtil.createScaledDrawableFitWith(mapView.get(), R.drawable.ic_menu_mapmode)
        );
        mapOverlays.add(MAP_ICON_OVERLAY);
    }*/

    /**
     * Disposes of the {@link MapReadinessManager} and clears all RxJava subscriptions.
     * Should be called when the controller is no longer needed (e.g., when the MapView is detached).
     */
    public void dispose() {
        readinessManager.dispose();
        ConcurrentUtil.tryToDispose(compositeDisposable);
    }

    /**
     * Clears all markers and polylines currently displayed on the map, except for the copyright overlay.
     */
    private void clearOverlays() {
        Log.d(TAG, "clearOverlays() called");

        mapOverlays.clear();
        polylineList.clear();
        markerList.clear();
        addCopyrightOverlay(mapView.get());

        //mapOverlayMode();

        invalidate();
    }

    /**
     * Updates the map view to fit the bounding box of the entire track data from the cache.
     */
    private void updateInitialBoundingBox() {
        try {
            if (geoPointCachedProvider.getGeoPointStatistics().getNortheastCorner() != null) {
                List<GeoPoint> bounds = Arrays.asList(
                        geoPointCachedProvider.getGeoPointStatistics().getNortheastCorner(),
                        geoPointCachedProvider.getGeoPointStatistics().getSouthwestCorner());

                BoundingBox boundingBox = fromGeoPoints(bounds);

                setBoundingBoxWithPadding(boundingBox, 0.1,
                        MapConfig.DEFAULT_PADDING_PX, true, MapConfig.ANIMATION_DURATION_MS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting bounding box", e);
        }
    }

    /**
     * Handles {@link EventVisibleChartEntriesTimestamp} events by updating the highlighted
     * polyline segment on the map ({@link #boundaryPolyline}) to match the visible time range from the chart.
     *
     * @param event The event containing the start and end timestamps of the visible range.
     */
    private void handleEvent(EventVisibleChartEntriesTimestamp event) {
        //Log.d(TAG, "handleEvent() called with: event = [" + event + "]");

        Vector<Long> visibleEntriesBoundary = event.timestampBoundary();

        if (visibleEntriesBoundary.isEmpty()) {
            return;
        }

        currentVisible = new AtomicReference<>(visibleEntriesBoundary);

        long startTime = System.currentTimeMillis();
        List<GeoPoint> bounds = geoPointCachedProvider.get(
                visibleEntriesBoundary.firstElement(),
                visibleEntriesBoundary.lastElement()
        );

        addOrUpdateFullPolyline();
        addOrUpdateBoundaryPolyline(bounds);

        long endTime = System.currentTimeMillis();

        double timeSec = (endTime - startTime) / 1000.0;

        //Log.d(TAG, "updateZoomFrom() time to get visibleEntriesBoundary from cache  = " + timeSec + "[s]");

        startTime = System.currentTimeMillis();
        BoundingBox boundingBox = fromGeoPoints(bounds);
        endTime = System.currentTimeMillis();
        timeSec = (endTime - startTime) / 1000.0;

        //Log.d(TAG, "updateZoomFrom() time to get BoundingBox from fromGeoPoints  = " + timeSec + "[s]");

        setBoundingBoxWithPadding(boundingBox, 0.1,
                MapConfig.DEFAULT_PADDING_PX, true, MapConfig.ANIMATION_DURATION_MS / 2);

        invalidate();
    }

    /**
     * Adds or updates the polyline representing the full GPX track.
     * Retrieves points from {@link GeoPointCache} and creates/updates the {@link #fullPolyline}.
     */
    private void addOrUpdateFullPolyline() {
        List<GeoPoint> points = geoPointCachedProvider.getGeoPointVector();

        if (points == null || points.isEmpty()) {
            Log.w(TAG, "Cannot add empty or null polyline");
            return;
        }

        if (fullPolyline == null) {
            fullPolyline = new Polyline();
            Paint paint = fullPolyline.getOutlinePaint();
            paint.setColor(mapView.get().getContext().getColor(R.color.darkBlue128));
            paint.setStrokeWidth(MapConfig.DEFAULT_POLYLINE_WIDTH);

            addOverlayAndSortWithPriority(fullPolyline);
            polylineList.add(fullPolyline);
        }
        fullPolyline.setPoints(points);

        if (!mapOverlays.contains(fullPolyline)) {
            addOverlayAndSortWithPriority(fullPolyline);
        }
    }

    /**
     * Adds or updates the polyline representing the currently visible segment based on chart interactions.
     * Creates/updates the {@link #boundaryPolyline}.
     *
     * @param points The list of {@link GeoPoint}s for the visible segment.
     */
    private void addOrUpdateBoundaryPolyline(List<GeoPoint> points) {
        if (boundaryPolyline == null) {
            boundaryPolyline = new Polyline();

            Paint paint = boundaryPolyline.getOutlinePaint();
            paint.setColor(mapView.get().getContext().getColor(R.color.darkOrange));
            paint.setStrokeWidth(MapConfig.DEFAULT_POLYLINE_WIDTH * 2);

            addOverlayAndSortWithPriority(boundaryPolyline);
            polylineList.add(boundaryPolyline);
        }
        boundaryPolyline.setPoints(points);

        if (!mapOverlays.contains(boundaryPolyline)) {
            addOverlayAndSortWithPriority(boundaryPolyline);
        }
    }

    /**
     * Adds an overlay to the map's overlay list, attempting to maintain a specific drawing order
     * based on {@link OverlayPriority}.
     *
     * @param overlay The {@link Overlay} to add.
     */
    public void addOverlayAndSortWithPriority(Overlay overlay) {
        mapOverlays.add(overlay);

        OverlayPriority.sort(mapOverlays);
    }

    @Override
    public Marker addMarker(GeoPoint position, String title) {
        Marker marker = null;
        if (mapView.get() != null) {
            marker = new Marker(mapView.get());
            marker.setPosition(position);
            marker.setTitle(title);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            addOverlayAndSortWithPriority(marker);
            markerList.add(marker);
        }

        return marker;
    }

    @Override
    public void addPolyline(List<GeoPoint> points, int color, float width) {
        Log.d(TAG, "addPolyline() called with points size: " + (points != null ? points.size() : 0));
        if (points == null || points.isEmpty()) {
            Log.w(TAG, "Cannot add empty or null polyline");
            return;
        }

        Polyline polyline = new Polyline();

        polyline.getOutlinePaint().setColor(color);
        polyline.getOutlinePaint().setStrokeWidth(width);
        //polyline.setGeodesic(true);

        polyline.setPoints(points);

        addOverlayAndSortWithPriority(polyline);
    }

    @Override
    public void clearMarkers() {
        for (Marker marker : markerList) {
            mapOverlays.remove(marker);
        }
        markerList.clear();
    }

    @Override
    public void clearPolylines() {
        for (Polyline polyline : polylineList) {
            mapOverlays.remove(polyline);
        }
        polylineList.clear();
    }

    @Override
    public void clearAll() {
        clearMarkers();
        clearPolylines();
    }

    @Override
    public void setCenter(GeoPoint center) {
        if (center != null) {
            mapController.setCenter(center);
        }
    }

    @Override
    public void setZoom(double zoomLevel) {
        mapController.setZoom(zoomLevel);
    }

    @Override
    public void setCenterAndZoom(GeoPoint center, double zoomLevel) {
        if (center != null) {
            mapController.setCenter(center);
            mapController.setZoom(zoomLevel);
        }
    }

    @Override
    public void setBoundingBox(BoundingBox boundingBox) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }
        mapView.get().zoomToBoundingBox(boundingBox, true, MapConfig.DEFAULT_PADDING_PX,
                MapConfig.ANIMATION_DURATION_MS, null);
    }

    @Override
    public void setBoundingBox(BoundingBox boundingBox, int paddingPx, long animationDurationMs) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }

        paddingPx = Math.max(0, paddingPx);
        animationDurationMs = Math.max(0, animationDurationMs);

        mapView.get().zoomToBoundingBox(boundingBox, true, paddingPx, animationDurationMs, null);
    }

    @Override
    public void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }

        paddingPercent = Math.min(1, Math.max(0, paddingPercent));
        BoundingBox paddedBox = createPaddedBoundingBox(boundingBox, paddingPercent);
        mapView.get().zoomToBoundingBox(paddedBox, true, MapConfig.DEFAULT_PADDING_PX,
                MapConfig.ANIMATION_DURATION_MS, null);
    }

    @Override
    public void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent,
                                          int paddingPx, boolean animated, long animationDurationMs) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }

        paddingPercent = Math.min(1, Math.max(0, paddingPercent));
        paddingPx = Math.max(0, paddingPx);
        animationDurationMs = Math.max(0, animationDurationMs);

        BoundingBox paddedBox = createPaddedBoundingBox(boundingBox, paddingPercent);
        mapView.get().zoomToBoundingBox(paddedBox, animated, paddingPx, 20, animationDurationMs);
    }

    /**
     * Creates a new marker for the selected point or updates the position of the existing selected marker.
     * Uses {@link CustomSelectedMarkerMap} for the marker type.
     *
     * @param geoPointEntity The {@link GeoPointEntity} representing the point to mark as selected.
     */
    private void createOrUpdateSelectedMarker(GeoPointEntity geoPointEntity) {
        if (geoPointEntity == null) {
            return;
        }

        if (currentSelectedMarker != null && currentSelectedMarker.get() != null && mapContains(currentSelectedMarker.get())) {
            updateMarker(geoPointEntity);
        } else {
            createMarkerWithPosition(geoPointEntity);
        }
    }

    /**
     * Forces the MapView to redraw itself and its overlays.
     */
    private void invalidate() {
        mapView.get().invalidate();
    }

    /**
     * Checks if a given marker is currently present in the map's overlay list.
     *
     * @param marker The {@link Marker} to check.
     * @return True if the marker is on the map, false otherwise.
     */
    private boolean mapContains(Marker marker) {
        return mapOverlays.contains(marker);
    }

    /**
     * Updates the position and potentially other properties of the currently selected marker.
     *
     * @param geoPointEntity The new {@link GeoPointEntity} data for the selected marker.
     */
    private void updateMarker(GeoPointEntity geoPointEntity) {
        if (geoPointEntity != null && currentSelectedMarker != null) {
            Marker marker = currentSelectedMarker.get();
            marker.getPosition().setCoords(
                    geoPointEntity.getLatitude(),
                    geoPointEntity.getLongitude()
            );
        }
    }

    /**
     * Creates a new {@link CustomSelectedMarkerMap}, sets its position and icon, adds it to the map overlays,
     * and updates the {@link #currentSelectedMarker} reference.
     *
     * @param geoPointEntity The {@link GeoPointEntity} to create the marker for.
     */
    private void createMarkerWithPosition(GeoPointEntity geoPointEntity) {
        currentSelectedMarker
                = new WeakReference<>(addMarker(geoPointEntity, "SelectedOnChart :)")
        );
    }

    /**
     * Returns an Observable that emits the {@link GeoPointEntity} whenever a point is selected on the map.
     * (Currently seems unused based on code, but provides the capability).
     *
     * @return Observable<GeoPointEntity>
     */
    public Observable<GeoPointEntity> observeSelectPoint() {
        return selectedPointOnMap;
    }
}