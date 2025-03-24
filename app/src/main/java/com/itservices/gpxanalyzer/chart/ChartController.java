package com.itservices.gpxanalyzer.chart;

import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener {

    private final PublishSubject<BaseEntry> baseEntrySelectionPublishSubject = PublishSubject.create();
    @Inject
    ChartProvider chartProvider;

    @Inject
    public ChartController() {
    }

    /**
     * Initialize the chart with no data (just styling).
     */
    @UiThread
    public void bindChart(@NonNull DataEntityLineChart chartBindings) {
        Log.d(ChartController.class.getSimpleName(), "bindChart() called with: chartBindings = [" + chartBindings + "]");

        chartBindings.setOnChartValueSelectedListener(this);
        chartBindings.setOnChartGestureListener(this);

        chartProvider.registerBinding(chartBindings);
    }

    public Single<RequestStatus> initChart() {
        return chartProvider.initChart();
    }

    @UiThread
    public boolean isDrawIconsEnabled() {

        if (chartProvider.getChart() != null) {
            LineData lineData = chartProvider.getChart().getData();
            if (lineData != null && !lineData.getDataSets().isEmpty()) {
                return lineData.getDataSets().get(0).isDrawIconsEnabled();
            }
        }
        return chartProvider.getSettings().isDrawIconsEnabled();
    }

    @UiThread
    public void setDrawIconsEnabled(boolean isChecked) {

        chartProvider.getSettings().setDrawIconsEnabled(isChecked);
        chartProvider.updateDataChart().subscribe();
    }

    public boolean isDrawAscDescSegEnabled() {
        return chartProvider.getSettings().isDrawAscDescSegEnabled();
    }

    @UiThread
    public void setDrawAscDescSegEnabled(boolean isChecked) {

        chartProvider.getSettings().setDrawAscDescSegEnabled(isChecked);
        chartProvider.updateDataChart().subscribe();
    }

    public void animateZoomToCenter(final float targetScaleX, final float targetScaleY, long duration) {
        Objects.requireNonNull(chartProvider.getChart()).animateZoomToCenter(targetScaleX, targetScaleY, duration);
    }

    public void animateFitScreen(long duration) {
        Objects.requireNonNull(chartProvider.getChart()).animateFitScreen(duration);
    }

    public void setDrawXLabels(boolean drawX) {
        chartProvider.getSettings().setDrawXLabels(drawX);
    }

    public Single<RequestStatus> updateChartData(DataEntityWrapper dataEntityWrapper) {
        return chartProvider.updateChartData(dataEntityWrapper);
    }

    public Observable<BaseEntry> getSelection() {
        return baseEntrySelectionPublishSubject;
    }

    public void select(long selectedTimeMillis) {
        manualSelectEntryOnSelectedTime(Objects.requireNonNull(chartProvider.getChart()), selectedTimeMillis, true, false);
    }

    private void manualSelectEntryOnSelectedTime(DataEntityLineChart chart, long selectedTimeMillis, boolean centerViewToSelection, boolean callListeners) {
        if (chart == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart is null during selection");
            return;
        }

        chart.getChartTouchListener().setLastGesture(ChartTouchListener.ChartGesture.NONE);

        if (selectedTimeMillis < 0) {
            chart.highlightValue(null, false);
            chart.invalidate();
            return;
        }
        if (chart.getData() == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart data is null during selection");
            return;
        }

        EntryCacheMap entryCacheMap = chartProvider.getEntryCacheMap();
        if (entryCacheMap == null) {
            Log.w(ChartController.class.getSimpleName(), "Entry cache map is null during selection");
            return;
        }

        BaseEntry entryFound = (BaseEntry) entryCacheMap.get(selectedTimeMillis);
        if (entryFound != null) {
            //Log.d(ChartController.class.getSimpleName(), "Found entry for timestamp: " + selectedTimeMillis);
            setSelectionEntry(entryFound, callListeners);
            chart.highlightValue(entryFound.getX(), entryFound.getY(), entryFound.getDataSetIndex(), callListeners);

            if (chart.getHighlighted() != null) {
                chartProvider.setSelectionHighlight(chart.getHighlighted()[0]);
            }

            if (centerViewToSelection) {
                chart.centerViewTo(entryFound.getX(), entryFound.getY(), YAxis.AxisDependency.LEFT);
            }
        } else {
            Log.w(ChartController.class.getSimpleName(), "No entry found for timestamp: " + selectedTimeMillis);
        }
    }

    private void setSelectionEntry(Entry entry, boolean publishSelection) {
        DataEntityLineChart chart = chartProvider.getChart();
        if (chart == null) {
            Log.w(ChartController.class.getSimpleName(), "Chart is null during selection entry setting");
            return;
        }

        chart.setHighlightedEntry(entry);

        if (publishSelection && (entry instanceof BaseEntry)) {
            //Log.d(ChartController.class.getSimpleName(), "Publishing selection for entry: " + entry);
            baseEntrySelectionPublishSubject.onNext((BaseEntry) entry);
        }
    }

    private void resetMarkerAndClearSelection(DataEntityLineChart chart) {
        chartProvider.setSelectionHighlight(null);
        chart.setHighlightedEntry(null);

        manualSelectEntryOnSelectedTime(chart, -1, false, true);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float speedX, float speedY) {
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Objects.requireNonNull(chartProvider.getChart()).highlightCenterValueInTranslation();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //Log.d(ChartController.class.getSimpleName(), "Value selected: " + e + ", highlight: " + h);
        setSelectionEntry(e, true);
        chartProvider.setSelectionHighlight(h);
    }

    @Override
    public void onNothingSelected() {
        Log.d(ChartController.class.getSimpleName(), "Nothing selected");
        resetMarkerAndClearSelection(Objects.requireNonNull(chartProvider.getChart()));
    }

    public String getChartAddress() {
        DataEntityLineChart chart = chartProvider.getChart();

        if (chart != null) {
            return Integer.toHexString( chart.hashCode() );
        }

        return null;
    }
}
