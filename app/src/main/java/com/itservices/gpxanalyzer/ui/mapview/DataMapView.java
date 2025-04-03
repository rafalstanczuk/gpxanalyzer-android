package com.itservices.gpxanalyzer.ui.mapview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;


@AndroidEntryPoint
public class DataMapView extends MapView {
    private static final String TAG = DataMapView.class.getSimpleName();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    protected MapViewController mapViewController;

    public DataMapView(Context context) {
        super(context);

        init(context);
    }

    public DataMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init() called with: context = [" + context + "]");

        mapViewController.bind(this);

        Configuration.getInstance().setUserAgentValue(context.getPackageName());


        setTileSource(TileSourceFactory.MAPNIK);

        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        setMultiTouchControls(true);
        getController().setZoom(MapConfig.DEFAULT_ZOOM_LEVEL);
    }

    public MapViewController getMapViewController() {
        return mapViewController;
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
        mapViewController.dispose();
        super.onDetachedFromWindow();
    }
}