package com.itservices.gpxanalyzer.ui.components.chart.extras;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.itservices.gpxanalyzer.databinding.OverlayViewToReloadLayoutBinding;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.ui.components.mapview.DataMapView;

import org.osmdroid.views.MapView;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A custom {@link LinearLayout} designed to act as an overlay on top of other views
 * (like charts or maps) to indicate a loading, processing, or error state.
 * Its visibility is controlled based on the current application {@link RequestStatus}.
 * Uses View Binding (`OverlayViewToReloadLayoutBinding`) for its layout.
 */
public class OverlayViewToReloadLayoutView extends LinearLayout {
    private static final String TAG = OverlayViewToReloadLayoutView.class.getSimpleName();
    /** Global event bus (potentially used for observing status, though currently handled via direct method call). */
    @Inject
    GlobalEventWrapper mapChartGlobalEventWrapper;

    /** View binding instance for the layout (overlay_view_to_reload_layout.xml). */
    OverlayViewToReloadLayoutBinding binding;

    /**
     * Constructor used by Hilt for dependency injection.
     *
     * @param context The application context.
     */
    @Inject
    public OverlayViewToReloadLayoutView(@ApplicationContext Context context) {
        super(context);
        inflateView(context);
    }

    /**
     * Constructor called when inflating from XML.
     *
     * @param context The context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public OverlayViewToReloadLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView(context);
    }

    /**
     * Constructor called when inflating from XML with a default style attribute.
     *
     * @param context      The context the view is running in.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
     */
    public OverlayViewToReloadLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    /**
     * Constructor called when inflating from XML with default style attribute and resource.
     *
     * @param context      The context the view is running in.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
     * @param defStyleRes  A resource identifier of a style resource that supplies default values.
     */
    public OverlayViewToReloadLayoutView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflateView(context);
    }

    /**
     * Determines the appropriate visibility state ({@link View#VISIBLE} or {@link View#GONE})
     * for the overlay based on the given {@link RequestStatus}.
     * The overlay is generally visible during loading, processing, or error states, and hidden otherwise.
     *
     * @param requestStatus The current application request status.
     * @return {@link View#VISIBLE} or {@link View#GONE}.
     */
    public static Integer getOverlayViewToReloadIndicatorVisibility(RequestStatus requestStatus) {
        return switch (requireNonNull(requestStatus)) {
            case ERROR_DATA_SETS_NULL, ERROR_LINE_DATA_SET_NULL, ERROR_NEW_DATA_SET_NULL,
                 ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW, CHART_WEAK_REFERENCE_IS_NULL, CHART_IS_NULL,
                 ERROR,
                 LOADING, NEW_DATA_LOADING, DATA_LOADED, PROCESSING, PROCESSED, CHART_UPDATING,
                 CHART_UPDATED, SELECTED_FILE -> View.VISIBLE;
            case DEFAULT, CHART_INITIALIZED, DONE -> View.GONE;
        };
    }

    /**
     * Inflates the layout associated with this custom view using View Binding.
     *
     * @param context The application context.
     */
    private void inflateView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        binding = OverlayViewToReloadLayoutBinding.inflate(inflater, this, true);
    }

    /**
     * Updates the visibility of this overlay view based on the provided {@link RequestStatus}.
     * Uses {@link #getOverlayViewToReloadIndicatorVisibility(RequestStatus)} to determine the visibility.
     *
     * @param requestStatus The current application request status.
     */
    public void handleAction(RequestStatus requestStatus) {
        setVisibility(
                getOverlayViewToReloadIndicatorVisibility(requestStatus)
        );
        invalidate();
    }

    /**
     * Adds this overlay view as a child to a given {@link MapView}.
     * Configures layout parameters to ensure the overlay matches the map view's size.
     *
     * @param mapView The {@link MapView} to add this overlay to.
     */
    public void addInto(MapView mapView) {
        setDrawingCacheEnabled(true);
        try {
            // Make sure overlay view is added on top of other views
            MapView.LayoutParams layoutParams = new MapView.LayoutParams(
                    MapView.LayoutParams.MATCH_PARENT,
                    MapView.LayoutParams.MATCH_PARENT,
                    mapView.getMapCenter(),
                    MapView.LayoutParams.MATCH_PARENT,
                    0,
                    0
            );
            setLayoutParams(layoutParams);

            mapView.addView(this);
        } catch (Exception e) {
            Log.e(TAG, "Error adding overlay view", e);
        }
    }
}