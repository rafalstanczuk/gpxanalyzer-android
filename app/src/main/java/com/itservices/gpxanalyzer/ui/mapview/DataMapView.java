package com.itservices.gpxanalyzer.ui.mapview;

import static org.osmdroid.util.BoundingBox.fromGeoPoints;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.cache.rawdata.GeoPointCache;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;


@AndroidEntryPoint
public class DataMapView extends MapView implements MapListener, MapOperations, MapOverlayOperations, MapReadinessManager.OnMapReadyCallback {
    private static final String TAG = "MapViewWrapper";
    private final List<Marker> markers = new ArrayList<>();
    private final List<Polyline> polylines = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MapReadinessManager readinessManager;

    @Inject
    GeoPointCache geoPointCachedProvider;

    public DataMapView(Context context) {
        super(context);
        readinessManager = new MapReadinessManager(this);
        init(context);
    }

    public DataMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readinessManager = new MapReadinessManager(this);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init() called with: context = [" + context + "]");

        Configuration.getInstance().setUserAgentValue(context.getPackageName());
        setTileSource(TileSourceFactory.MAPNIK);

        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        setMultiTouchControls(true);
        getController().setZoom(MapConfig.DEFAULT_ZOOM_LEVEL);

        setOnMapReadyCallback(this);
        addMapListener(this);
    }


    private void loadInitialData() {

        List<GeoPoint> points = geoPointCachedProvider.getPointVector();
        GeoPoint center = geoPointCachedProvider.getGeoPointStatistics().getCenter();

        if (center!=null) {

            addPolyline(points, getContext().getColor(R.color.lightGreen128), MapConfig.DEFAULT_POLYLINE_WIDTH * 4);
            setCenter(center);

            addMarker(center, "Center");

            updateInitialBoundingBox();
        }
    }

    private void updateInitialBoundingBox() {
        try {
            List<GeoPoint> bounds = Arrays.asList(
                    geoPointCachedProvider.getGeoPointStatistics().getNortheastCorner(),
                    geoPointCachedProvider.getGeoPointStatistics().getSouthwestCorner());

            BoundingBox boundingBox = fromGeoPoints(bounds);

            setBoundingBoxWithPadding(boundingBox, 0.1,
                    MapConfig.DEFAULT_PADDING_PX, MapConfig.ANIMATION_DURATION_MS);
        } catch (Exception e) {
            Log.e(TAG, "Error setting bounding box", e);
        }
    }

    @Override
    public void addMarker(GeoPoint position, String title) {
        Marker marker = new Marker(this);
        marker.setPosition(position);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        getOverlays().add(marker);
        markers.add(marker);
    }

    @Override
    public void addPolyline(List<GeoPoint> points, int color, float width) {
        Log.d(TAG, "addPolyline() called with points size: " + (points != null ? points.size() : 0));
        if (points == null || points.isEmpty()) {
            Log.w(TAG, "Cannot add empty or null polyline");
            return;
        }


/*        disposables.add(
                Observable.timer(5, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                __ -> {
                                    polyline.getOutlinePaint().setColor(Color.RED);
                                    polyline.getOutlinePaint().setStrokeWidth(width*2);
                                    invalidate();

                                },
                                throwable -> Log.e(TAG, "Error in delayed bounding box update", throwable)
                        )
        );*/


        Polyline polyline = new Polyline();

        polyline.getOutlinePaint().setColor(color);
        polyline.getOutlinePaint().setStrokeWidth(width);
        //polyline.setGeodesic(true);

        polyline.setPoints(points);

        getOverlays().add(polyline);
        polylines.add(polyline);
    }

    @Override
    public void clearMarkers() {
        for (Marker marker : markers) {
            getOverlays().remove(marker);
        }
        markers.clear();
    }

    @Override
    public void clearPolylines() {
        for (Polyline polyline : polylines) {
            getOverlays().remove(polyline);
        }
        polylines.clear();
    }

    @Override
    public void clearAll() {
        clearMarkers();
        clearPolylines();
    }

    @Override
    public void setCenter(GeoPoint center) {
        if (center != null) {
            getController().setCenter(center);
        }
    }

    @Override
    public void setZoom(double zoomLevel) {
        getController().setZoom(zoomLevel);
    }

    @Override
    public void setCenterAndZoom(GeoPoint center, double zoomLevel) {
        if (center != null) {
            getController().setCenter(center);
            getController().setZoom(zoomLevel);
        }
    }

    @Override
    public void setBoundingBox(BoundingBox boundingBox) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }
        zoomToBoundingBox(boundingBox, true, MapConfig.DEFAULT_PADDING_PX,
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

        zoomToBoundingBox(boundingBox, true, paddingPx, animationDurationMs, null);
    }

    @Override
    public void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }

        paddingPercent = Math.min(1, Math.max(0, paddingPercent));
        BoundingBox paddedBox = createPaddedBoundingBox(boundingBox, paddingPercent);
        zoomToBoundingBox(paddedBox, true, MapConfig.DEFAULT_PADDING_PX,
                MapConfig.ANIMATION_DURATION_MS, null);
    }

    @Override
    public void setBoundingBoxWithPadding(BoundingBox boundingBox, double paddingPercent,
                                          int paddingPx, long animationDurationMs) {
        if (boundingBox == null) {
            Log.w(TAG, "Cannot set null bounding box");
            return;
        }

        paddingPercent = Math.min(1, Math.max(0, paddingPercent));
        paddingPx = Math.max(0, paddingPx);
        animationDurationMs = Math.max(0, animationDurationMs);

        BoundingBox paddedBox = createPaddedBoundingBox(boundingBox, paddingPercent);
        zoomToBoundingBox(paddedBox, true, paddingPx, animationDurationMs, null);
    }

    private BoundingBox createPaddedBoundingBox(BoundingBox boundingBox, double paddingPercent) {
        double latPadding = (boundingBox.getLatNorth() - boundingBox.getLatSouth()) * paddingPercent;
        double lonPadding = (boundingBox.getLonEast() - boundingBox.getLonWest()) * paddingPercent;

        return new BoundingBox(
                boundingBox.getLatNorth() + latPadding,
                boundingBox.getLonEast() + lonPadding,
                boundingBox.getLatSouth() - latPadding,
                boundingBox.getLonWest() - lonPadding
        );
    }

    public void setOnMapReadyCallback(MapReadinessManager.OnMapReadyCallback callback) {
        readinessManager.setOnMapReadyCallback(callback);
    }

    public Observable<Boolean> getMapReadyObservable() {
        return readinessManager.getMapReadyObservable();
    }

    public boolean isMapReady() {
        return readinessManager.isMapReady();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        disposables.clear();
        readinessManager.dispose();
        super.onDetachedFromWindow();
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

        loadInitialData();
    }
}