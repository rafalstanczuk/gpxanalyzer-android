package com.itservices.gpxanalyzer.ui.components.miniature;

import static org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER;
import static org.osmdroid.views.drawing.MapSnapshot.Status.CANVAS_OK;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.util.Function;

import com.github.mikephil.charting.utils.Utils;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.drawing.MapSnapshot;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

public class MiniatureMapView extends MapView implements MapListener, MapView.OnFirstLayoutListener {
    private static final String TAG = MiniatureMapView.class.getSimpleName();
    private static final OnlineTileSourceBase MAPNIK_6 =
            new XYTileSource("Mapnik", 0, 19,
                    (int) Utils.convertDpToPixel(200), ".png",
                    new String[]{"https://tile.openstreetmap.org/"},
                    "© OpenStreetMap contributors",
                    new TileSourcePolicy(16,
                            TileSourcePolicy.FLAG_NO_BULK
                                    | TileSourcePolicy.FLAG_NO_PREVENTIVE
                                    | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                                    | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED));
    private final Handler handler = new Handler(Looper.getMainLooper());

    public MiniatureMapView(Context context) {
        super(context);
        init(context);
    }

    public MiniatureMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init() called with: context = [" + context + "]");

        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        setTileSource(MAPNIK_6);
        setMultiTouchControls(false);
        getZoomController().setVisibility(NEVER);
        setVisibility(View.INVISIBLE);

        addMapListener(this);
        setDrawingCacheEnabled(false);
        addOnFirstLayoutListener(this);
    }

    private void addCopyrightOverlay() {
        getOverlays().add(new CopyrightOverlay(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.addMapListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeMapListener(this);
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        //Log.d(TAG, "onZoom() called with: event = [" + event + "]");

        return false;
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom) {
        Log.d(TAG, "onFirstLayout() called with: v = [" + v + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");

    }

    public void generateBitmap(double latitude, double longitude, Function<Bitmap, Void> callback) {
        Log.d(TAG, "generateBitmap using MapSnapshot: lat=" + latitude + ", lon=" + longitude);

        handler.post(() -> {
            try {
                getOverlays().clear();
                //clearTileCache();

                GeoPoint startPoint = new GeoPoint(latitude, longitude);
                getController().setZoom(15.0);
                getController().setCenter(startPoint);

                Marker startMarker = new Marker(MiniatureMapView.this);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                getOverlays().add(startMarker);

                requestLayout();
                invalidate();

                handler.post(() -> takeSnapshotWithCallback(callback));

            } catch (Exception e) {
                Log.e(TAG, "Error setting up map for snapshot", e);
                if (callback != null) {
                    callback.apply(null);
                }
            }
        });
    }

    private void takeSnapshotWithCallback(Function<Bitmap, Void> callback) {
        if (callback == null) {
            Log.w(TAG, "Provider callback is null in takeSnapshotWithCallback");
            return;
        }

        MapSnapshot.MapSnapshotable snapshotListener = new MapSnapshot.MapSnapshotable() {
            @Override
            public void callback(MapSnapshot pMapSnapshot) {
                if (pMapSnapshot.getStatus() == CANVAS_OK) {
                    callback.apply(pMapSnapshot.getBitmap());
                } else {
                    callback.apply(null);
                }
            }
        };
        MapSnapshot mapSnapshot = new MapSnapshot(snapshotListener, MapSnapshot.INCLUDE_FLAG_UPTODATE, this);

        new Thread(mapSnapshot).start();
        Log.d(TAG, "MapSnapshot task started.");
    }

    public void clearTileCache() {
        MapTileProviderBase provider = getTileProvider();
        if (provider != null) {
            try {
                provider.clearTileCache();
                Log.i(TAG, "Cleared osmdroid tile cache via MapTileProviderBase.");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing tile cache via MapTileProviderBase", e);
            }

            if (provider.getTileWriter() instanceof SqlTileWriter sqlWriter) {
                Log.d(TAG, "TileWriter is SqlTileWriter, further specific clearing might be needed if MapTileProviderBase.clearTileCache() is insufficient.");
            }
        } else {
            Log.w(TAG, "Cannot clear tile cache: MapTileProviderBase is null.");
        }
    }
}