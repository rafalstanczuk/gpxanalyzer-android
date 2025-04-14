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
import com.itservices.gpxanalyzer.data.raw.GeoPointEntity;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
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

public class MapViewController implements MapListener, MapReadinessManager.OnMapReadyCallback, MapOperations, MapOverlayOperations {
    private static final String TAG = MapViewController.class.getSimpleName();
    private final List<Marker> markerList = new ArrayList<>();
    private final List<Polyline> polylineList = new ArrayList<>();
    private final PublishSubject<GeoPointEntity> selectedPointOnMap = PublishSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private WeakReference<DataMapView> mapView;
    private List<Overlay> mapOverlays;
    private IMapController mapController;
    private WeakReference<Marker> currentSelectedMarker = new WeakReference<>(null);
    private Polyline boundaryPolyline = null;
    private Polyline fullPolyline = null;
    private WeakReference<GeoPointEntity> currentSelectedPoint = new WeakReference<>(null);
    private AtomicReference<Vector<Long>> currentVisible = new AtomicReference<>();
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

    public void bind(DataMapView mapView) {
        Log.d(TAG, "bind() called with: mapView = [" + mapView + "]");

        this.mapView = new WeakReference<>(mapView);
        mapOverlays = mapView.getOverlays();
        mapController = mapView.getController();

        readinessManager.bind(mapView);
        readinessManager.setOnMapReadyCallback(this);
        mapView.addMapListener(this);

        observeEntrySelectionOnCharts();
        observeVisibleEntriesBoundaryOnCharts();
        observeRequestStatus();
    }

    public void loadInitialData() {
        Log.d(TAG, "loadInitialData() called");

        // GeoPoint center = geoPointCachedProvider.getGeoPointStatistics().getCenter();
        // setCenter(center);

        updateInitialBoundingBox();

        addOrUpdateFullPolyline();
        List<GeoPoint> points = geoPointCachedProvider.getGeoPointVector();
        addOrUpdateBoundaryPolyline(points);

        createOrUpdateSelectedMarker(currentSelectedPoint.get());

        invalidate();
    }

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

    private void handleAction(RequestStatus requestStatus) {
        Log.d(TAG, "handleAction() called with: requestStatus = [" + requestStatus + "]");

        if (requestStatus != null) {

            if (!readinessManager.isMapReady()) {
                requestStatusToHandleOnMapReadyAtomic.set(requestStatus);

                Log.d(TAG, "Map not ready, ignoring status: " + requestStatus);
                return;
            }

            requestStatusToHandleOnMapReadyAtomic.set(null);

            switch (requestStatus) {
                case NEW_DATA_LOADING -> clearOverlays();
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

    private void observeVisibleEntriesBoundaryOnCharts() {
        Log.d(TAG, "observeVisibleEntriesBoundaryOnCharts() called");

        compositeDisposable.add(mapChartGlobalEventWrapper
                .getEventVisibleChartEntriesTimestamp()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleEvent));
    }

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

    public void selectPoint(GeoPointEntity geoPointEntity) {
        //Log.d(TAG, "selectPoint() called with: geoPointEntity = [" + geoPointEntity + "]");
        currentSelectedPoint = new WeakReference<>(geoPointEntity);

        createOrUpdateSelectedMarker(currentSelectedPoint.get());
    }

    public Observable<Boolean> getMapReadyObservable() {
        return readinessManager.getMapReadyObservable();
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        //Log.d(TAG, "onScroll() called with: event = [" + event + "]");
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        //Log.d(TAG, "onZoom() called with: event = [" + event + "]");
        return false;
    }

    @Override
    public void onMapReady() {
        Log.d(TAG, "onMapReady() called");

        handleAction(requestStatusToHandleOnMapReadyAtomic.get());
    }

    public void dispose() {
        readinessManager.dispose();
        ConcurrentUtil.tryToDispose(compositeDisposable);
    }

    private void clearOverlays() {
        Log.d(TAG, "clearOverlays() called");

        mapOverlays.clear();
        polylineList.clear();
        markerList.clear();

        invalidate();
    }

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

    private void invalidate() {
        mapView.get().invalidate();
    }

    private boolean mapContains(Marker marker) {
        return mapOverlays.contains(marker);
    }

    private void updateMarker(GeoPointEntity geoPointEntity) {
        if (geoPointEntity != null && currentSelectedMarker != null) {
            Marker marker = currentSelectedMarker.get();
            marker.getPosition().setCoords(
                    geoPointEntity.getLatitude(),
                    geoPointEntity.getLongitude()
            );
        }
    }

    private void createMarkerWithPosition(GeoPointEntity geoPointEntity) {
        currentSelectedMarker
                = new WeakReference<>(addMarker(geoPointEntity, "SelectedOnChart :)")
        );
    }

    public Observable<GeoPointEntity> observeSelectPoint() {
        return selectedPointOnMap;
    }
}