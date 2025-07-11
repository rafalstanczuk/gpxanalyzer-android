package com.itservices.gpxanalyzer.core.ui.components.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.core.events.RequestStatus;
import com.itservices.gpxanalyzer.core.ui.components.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.core.ui.components.chart.settings.background.GridBackgroundDrawer;
import com.itservices.gpxanalyzer.core.ui.components.chart.settings.highlight.StaticChartHighlighter;
import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartSlot;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Single;

/**
 * A specialized {@link LineChart} subclass tailored for displaying GPX track data.
 * Extends the base MPAndroidChart LineChart to integrate custom drawing, highlighting,
 * and interaction logic specific to the GPX Analyzer application.
 *
 * Key features and customizations:
 * - Integrates with {@link ChartComponents} for settings, scaling, palettes, etc.
 * - Uses {@link GridBackgroundDrawer} for a custom background.
 * - Employs {@link StaticChartHighlighter} for custom value highlighting.
 * - Provides methods for managing chart state related to GPX data (e.g., visible boundaries).
 * - Includes specialized touch handling and animation methods.
 */
@AndroidEntryPoint
public class DataEntityLineChart extends LineChart {

/*	@Inject
	DataEntityInfoLayoutView dataEntityInfoLayoutView;*/

    /** Component responsible for drawing the custom grid background. Injected by Hilt. */
    @Inject
    GridBackgroundDrawer gridBackgroundDrawer;

    /** Identifier for the slot this chart occupies in a multi-chart layout. */
    private ChartSlot chartSlot = null;
    /** Weak reference to the container holding shared chart components (settings, scaler, etc.). */
    private WeakReference<ChartComponents> chartComponentsWeakReference;

    /**
     * Constructor.
     *
     * @param context The context the view is running in.
     */
    public DataEntityLineChart(Context context) {
        super(context);
        initDataEntityInfoLayoutView();
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context The context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public DataEntityLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        initDataEntityInfoLayoutView();
    }

    /**
     * Constructor that is called when inflating a view from XML with a specific style.
     *
     * @param context  The context the view is running in.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle The default style resource ID.
     */
    public DataEntityLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDataEntityInfoLayoutView();
    }

    /**
     * Initializes the (currently commented out) overlay view for displaying data point info.
     */
    private void initDataEntityInfoLayoutView() {
        //dataEntityInfoLayoutView.setDrawingCacheEnabled(true);

/*		try {
			this.addView(dataEntityInfoLayoutView);
		} catch (Exception ignored) {
		}*/
    }

    /**
     * Overrides the default drawing behavior to catch and ignore potential exceptions during drawing,
     * preventing crashes.
     *
     * @param canvas The canvas to draw on.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);

        } catch (Exception ignore) {
        }
    }

    /**
     * Overrides the default grid background drawing to use the custom {@link GridBackgroundDrawer}.
     *
     * @param canvas The canvas to draw on.
     */
    @Override
    protected void drawGridBackground(Canvas canvas) {
        super.drawGridBackground(canvas);

        if (chartComponentsWeakReference != null && chartComponentsWeakReference.get() != null) {
            gridBackgroundDrawer.drawGridBackground(
                    this, chartComponentsWeakReference.get().getPaletteColorDeterminer(), canvas
            );
        }
    }

    /**
     * Overrides the default layout behavior. (Currently, custom layout logic is commented out).
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

/*		dataEntityInfoLayoutView.layout(
				90 ,getHeight() / 20,
				getWidth(),	getHeight()
		);*/
    }

    /**
     * Overrides the default measurement behavior. (Currently, custom measurement logic is commented out).
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
/*
		dataEntityInfoLayoutView.measure(dataEntityInfoLayoutView.getMeasuredWidth(),
			dataEntityInfoLayoutView.getMeasuredHeight()
		);*/
    }

    /**
     * Initializes the chart structure and appearance using the provided {@link ChartComponents}.
     * Sets up the custom highlighter, clears existing data, applies settings, and invalidates the view.
     *
     * @param chartComponents The container holding chart settings, scaler, palette, etc.
     * @return A {@link Single} that emits {@link RequestStatus#DONE} upon successful initialization.
     */
    public Single<RequestStatus> initChart(ChartComponents chartComponents) {
        return Single.fromCallable(() -> {
            chartComponentsWeakReference = new WeakReference<>(chartComponents);

            StaticChartHighlighter<DataEntityLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
                    this, (BarLineChartTouchListener) mChartTouchListener);
            setHighlighter(staticChartHighlighter);

            setData(new LineData());

            chartComponents.loadChartSettings(this);

            invalidate();
            return RequestStatus.DONE;
        });
    }

    /**
     * Gets the chart's primary touch listener, responsible for handling gestures like drag, scale, and tap.
     *
     * @return The {@link BarLineChartTouchListener} for this chart.
     */
    public BarLineChartTouchListener getChartTouchListener() {
        return (BarLineChartTouchListener) mChartTouchListener;
    }

    /**
     * Highlights the data entry closest to the vertical center of the current chart viewport.
     * This is often used during scrolling/translation to provide context.
     */
    public void highlightCenterValueInTranslation() {
        MPPointF pointFCenter = mViewPortHandler.getContentCenter();

        Entry entry = getEntryByTouchPoint(
                pointFCenter.getX(),
                pointFCenter.getY()
        );

        if (entry instanceof BaseEntry baseDataEntityEntry) {

            highlightValue(baseDataEntityEntry.getX(), baseDataEntityEntry.getY(), baseDataEntityEntry.getDataSetIndex(), true);
        }
    }

    /**
     * Determines the timestamp range of the data entries currently visible within the chart's viewport.
     *
     * @return A {@link Vector} containing two Long values: the minimum and maximum timestamps (in milliseconds)
     *         of the visible entries. Returns null if the chart data is not available.
     */
    public Vector<Long> getVisibleEntriesBoundaryTimestamps() {

        MPPointF pointFCenter = mViewPortHandler.getContentCenter();
        RectF contentRect = mViewPortHandler.getContentRect();

        float leftX = contentRect.left;
        float rightX = contentRect.right;

        Entry entryStart = getEntryByTouchPoint(
                leftX,
                pointFCenter.getY()
        );

        Entry entryEnd = getEntryByTouchPoint(
                rightX,
                pointFCenter.getY()
        );

        if ((entryStart instanceof BaseEntry) && (entryEnd instanceof BaseEntry)) {

            return new Vector<>(Arrays.asList(
                    ((BaseEntry) entryStart).getDataEntity().timestampMillis(),
                    ((BaseEntry) entryEnd).getDataEntity().timestampMillis()
            ));
        }

        return new Vector<>();
    }

    /**
     * Sets the currently highlighted entry on the chart.
     * Updates the chart's internal highlighted values and redraws the highlight indicator.
     *
     * @param selectedEntry The {@link Entry} to highlight.
     */
    public void setHighlightedEntry(Entry selectedEntry) {
        if (!(selectedEntry instanceof BaseEntry)) {
            return;
        }

        ChartTouchListener.ChartGesture chartGesture = getChartTouchListener().getLastGesture();

        determineSettingsDataEntityCurveLineHighlightIndicator(chartGesture);
    }

    /**
     * Determines the appearance of the highlight indicator based on the last performed gesture.
     * For example, it might show a specific indicator during drag/translation.
     *
     * @param chartGesture The last gesture performed on the chart.
     */
    private void determineSettingsDataEntityCurveLineHighlightIndicator(
            ChartTouchListener.ChartGesture chartGesture
    ) {
        if (getLineData() == null) {
            return;
        }

        AtomicBoolean shouldDraw = new AtomicBoolean(false);
        List<ILineDataSet> dataEntityCurveLineDataSet = getLineData().getDataSets();

        if (dataEntityCurveLineDataSet != null) {

            if (isFullyZoomedOut()) {
                shouldDraw.set(false);
            } else {
                switch (chartGesture) {
                    case X_ZOOM:
                    case Y_ZOOM:
                    case PINCH_ZOOM:
                    case ROTATE:
                    case DOUBLE_TAP:
                    case LONG_PRESS:
                    case SINGLE_TAP:
                        shouldDraw.set(false);

                        break;

                    case NONE:
                    case FLING:
                    case DRAG:
                        shouldDraw.set(false);

                        break;
                }
            }

            dataEntityCurveLineDataSet.forEach(iLineDataSet -> {
                ((LineDataSet) iLineDataSet).setDrawHorizontalHighlightIndicator(shouldDraw.get());
            });
        }
    }

    /**
     * Cleans up resources when the view is detached from the window.
     * Calls the superclass implementation.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    /**
     * Gets the {@link ChartSlot} identifier associated with this chart instance.
     *
     * @return The {@link ChartSlot}.
     */
    public ChartSlot getChartSlot() {
        return chartSlot;
    }

    /**
     * Sets the {@link ChartSlot} identifier for this chart instance.
     *
     * @param chartSlot The {@link ChartSlot} to associate with this chart.
     */
    public void setChartSlot(ChartSlot chartSlot) {
        this.chartSlot = chartSlot;
    }
}
