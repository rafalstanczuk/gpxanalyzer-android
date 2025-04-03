package com.itservices.gpxanalyzer.chart;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.Animation;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.settings.background.GridBackgroundDrawer;
import com.itservices.gpxanalyzer.chart.settings.highlight.StaticChartHighlighter;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Single;

/**
 * Specialized LineChart for displaying GPX data.
 * <p>
 * This class extends MPAndroidChart's LineChart to provide custom functionality
 * for displaying and interacting with GPX data. It integrates with the application's
 * chart styling, data handling, and interaction components.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Custom background grid drawing with GPX-specific styling</li>
 *   <li>Enhanced highlighting of data points with selectable behaviors</li>
 *   <li>Integration with application-specific data structures like {@link DataEntityWrapper}</li>
 *   <li>Specialized touch handling for GPX data exploration</li>
 *   <li>Support for limit lines showing important thresholds or boundaries</li>
 *   <li>Customized scaling and zooming behavior for GPX tracks</li>
 * </ul>
 * <p>
 * This chart component is designed to work with the broader GPX Analyzer application
 * architecture, including dependency injection through Dagger Hilt and reactive
 * programming patterns with RxJava.
 */
@AndroidEntryPoint
public class DataEntityLineChart extends LineChart {

/*	@Inject
	DataEntityInfoLayoutView dataEntityInfoLayoutView;*/

    /**
     * Handles drawing of the chart's background grid.
     * <p>
     * This component is responsible for rendering the grid background with
     * custom styling appropriate for GPX data visualization.
     */
    @Inject
    GridBackgroundDrawer gridBackgroundDrawer;

    private ChartSlot chartSlot = null;
    private WeakReference<ChartComponents> chartComponentsWeakReference;

    /**
     * Creates a new DataEntityLineChart with the specified context.
     * <p>
     * This constructor is typically used when creating the chart programmatically.
     *
     * @param context The context in which the chart is running
     */
    public DataEntityLineChart(Context context) {
        super(context);
        initDataEntityInfoLayoutView();
    }

    /**
     * Creates a new DataEntityLineChart with the specified context and attributes.
     * <p>
     * This constructor is typically used when the chart is inflated from XML.
     *
     * @param context The context in which the chart is running
     * @param attrs   The attribute set defining XML attributes for the chart
     */
    public DataEntityLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        initDataEntityInfoLayoutView();
    }

    /**
     * Creates a new DataEntityLineChart with the specified context, attributes, and style.
     * <p>
     * This constructor is typically used when the chart is inflated from XML with a specific style.
     *
     * @param context  The context in which the chart is running
     * @param attrs    The attribute set defining XML attributes for the chart
     * @param defStyle The default style resource ID
     */
    public DataEntityLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initDataEntityInfoLayoutView();
    }

    /**
     * Initializes the data entity info layout view.
     * <p>
     * This method is currently commented out in the implementation, but would
     * normally set up an overlay view to display information about selected data points.
     */
    private void initDataEntityInfoLayoutView() {
        //dataEntityInfoLayoutView.setDrawingCacheEnabled(true);

/*		try {
			this.addView(dataEntityInfoLayoutView);
		} catch (Exception ignored) {
		}*/
    }

    /**
     * Draws the chart on the canvas.
     * <p>
     * This method overrides the parent implementation to handle exceptions gracefully.
     * Any exceptions during drawing are caught and ignored to prevent application crashes.
     *
     * @param canvas The canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception ignore) {
        }
    }

    /**
     * Draws the grid background.
     * <p>
     * This method overrides the parent implementation to use the custom grid background drawer.
     * The custom drawer adds GPX-specific styling and visual elements to the grid background.
     *
     * @param canvas The canvas to draw on
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
     * Called when the layout changes.
     * <p>
     * This method overrides the parent implementation to handle layout of child views.
     * Currently, the data entity info layout view positioning is commented out.
     *
     * @param changed True if the layout has changed
     * @param left    Left position of this view
     * @param top     Top position of this view
     * @param right   Right position of this view
     * @param bottom  Bottom position of this view
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
     * Called to measure the view and its content.
     * <p>
     * This method overrides the parent implementation to handle measurement of child views.
     * Currently, the data entity info layout view measurement is commented out.
     *
     * @param widthMeasureSpec  Horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent
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
     * Initializes the chart with the specified settings.
     * <p>
     * This method sets up the highlighter, initializes empty data, and applies settings.
     * It returns a Single that emits the status of the initialization operation, making
     * it compatible with reactive programming patterns.
     *
     * @return A Single that emits the status of the initialization operation
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
     * Gets the chart's touch listener.
     * <p>
     * This method provides access to the touch listener that handles user interactions
     * with the chart, such as dragging, zooming, and tapping.
     *
     * @return The BarLineChartTouchListener for this chart
     */
    public BarLineChartTouchListener getChartTouchListener() {
        return (BarLineChartTouchListener) mChartTouchListener;
    }

    /**
     * Highlights the value at the center of the current view.
     * <p>
     * This method is called during chart translation to maintain highlight on the center value.
     * It determines which data point is at the center of the visible area and highlights it.
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
     * Sets the currently highlighted entry.
     * <p>
     * This method is called when a value is selected on the chart. It updates
     * highlight indicators based on the type of gesture used for selection.
     *
     * @param selectedEntry The entry to highlight, or null to clear highlight
     */
    public void setHighlightedEntry(Entry selectedEntry) {
        if (!(selectedEntry instanceof BaseEntry)) {
            return;
        }

        ChartTouchListener.ChartGesture chartGesture = getChartTouchListener().getLastGesture();

        determineSettingsDataEntityCurveLineHighlightIndicator(chartGesture);
    }

    /**
     * Determines whether to draw highlight indicators based on the chart gesture.
     * <p>
     * This method controls the appearance of highlight indicators based on the user's interaction.
     * Different gestures (tap, drag, zoom) may result in different highlight behaviors.
     *
     * @param chartGesture The last gesture performed on the chart
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
     * Animates the chart zoom to center with the specified scale.
     * <p>
     * This method smoothly zooms the chart to a specified scale level, centered
     * on the current center point of the visible area.
     *
     * @param targetScaleX The target X-axis scale
     * @param targetScaleY The target Y-axis scale
     * @param duration     The animation duration in milliseconds
     */
    public void animateZoomToCenter(final float targetScaleX, final float targetScaleY, long duration, Animator.AnimatorListener listener) {
        super.animateZoomToCenter(targetScaleX, targetScaleY, duration, listener);
    }

    /**
     * Called when the view is detached from its window.
     * <p>
     * This method ensures proper cleanup of resources, particularly the chart renderer's bitmap.
     * Releasing the bitmap prevents memory leaks when the chart is no longer visible.
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    public ChartSlot getChartSlot() {
        return chartSlot;
    }

    public void setChartSlot(ChartSlot chartSlot) {
        this.chartSlot = chartSlot;
    }
}
