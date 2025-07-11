package com.itservices.gpxanalyzer.core.ui.components.mapview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.itservices.gpxanalyzer.core.ui.components.chart.extras.OverlayViewToReloadLayoutView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;


/**
 * A custom {@link MapView} subclass designed for displaying GPX track data.
 * It integrates with a {@link MapViewController} for managing map interactions, overlays, and data display.
 * Sets default configurations suitable for the application (e.g., MAPNIK tile source, disabled built-in zoom controls).
 * Includes an {@link OverlayViewToReloadLayoutView} to indicate loading or pending states.
 */
@AndroidEntryPoint
public class DataMapView extends MapView {
    private static final String TAG = DataMapView.class.getSimpleName();

    /** Manages RxJava subscriptions within this view. */
    private final CompositeDisposable disposables = new CompositeDisposable();

    /** Controller handling map logic, overlays, and interactions. Injected by Hilt. */
    @Inject
    protected MapViewController mapViewController;

    /** View displayed as an overlay to indicate loading state. Injected by Hilt. */
    @Inject
    OverlayViewToReloadLayoutView overlayViewToReloadLayoutView;

    /**
     * Constructor.
     *
     * @param context The context the view is running in.
     */
    public DataMapView(Context context) {
        super(context);

        init(context);
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context The context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public DataMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    /**
     * Initializes the map view.
     * Binds the {@link MapViewController}, sets the user agent, tile source (MAPNIK),
     * disables default zoom buttons, enables multi-touch controls, sets default zoom,
     * and adds the loading overlay view.
     *
     * @param context The application context.
     */
    private void init(Context context) {
        Log.d(TAG, "init() called with: context = [" + context + "]");

        mapViewController.bind(this);
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
        setTileSource(TileSourceFactory.MAPNIK);
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        setMultiTouchControls(true);
        getController().setZoom(MapConfig.DEFAULT_ZOOM_LEVEL);

        overlayViewToReloadLayoutView.addInto(this);
    }

    /**
     * Lays out the MapView and its children, including the loading overlay.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        overlayViewToReloadLayoutView.layout(0, 0, right - left, bottom - top);
    }

    /**
     * Measures the MapView and the loading overlay to ensure the overlay matches the MapView size.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        overlayViewToReloadLayoutView.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
        );
    }

    /**
     * Calls the superclass implementation for onResume.
     * (OsmDroid MapView handles its own resume logic).
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Calls the superclass implementation for onPause.
     * (OsmDroid MapView handles its own pause logic).
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Cleans up resources when the view is detached from the window.
     * Clears RxJava disposables, disposes the {@link MapViewController}, and calls the superclass method.
     */
    @Override
    public void onDetachedFromWindow() {
        disposables.clear();
        mapViewController.dispose();
        super.onDetachedFromWindow();
    }
}