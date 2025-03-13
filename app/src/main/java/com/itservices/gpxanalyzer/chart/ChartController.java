package com.itservices.gpxanalyzer.chart;

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
import com.itservices.gpxanalyzer.data.RequestStatus;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener {

    @Inject
    ChartProvider chartProvider;

    private final PublishSubject<BaseEntry> baseEntrySelectionPublishSubject = PublishSubject.create();

    @Inject
    public ChartController() {
    }

    /**
     * Initialize the chart with no data (just styling).
     */
    @UiThread
    public void bindChart(@NonNull DataEntityLineChart chartBindings) {
        chartProvider.initChart(chartBindings);

        chartBindings.setOnChartValueSelectedListener(this);
        chartBindings.setOnChartGestureListener(this);
    }

    @UiThread
    public void setDrawIconsEnabled(boolean isChecked) {

        chartProvider.getSettings().setDrawIconsEnabled(isChecked);
        chartProvider.tryToUpdateDataChart();
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

    public void animateZoomToCenter(final float targetScaleX, final float targetScaleY, long duration) {
        chartProvider.getChart().animateZoomToCenter(targetScaleX, targetScaleY, duration);
    }

    public void animateFitScreen(long duration) {
        chartProvider.getChart().animateFitScreen(duration);
    }

    public void setDrawXLabels(boolean drawX) {
        chartProvider.getSettings().setDrawXLabels(drawX);
    }

    public Observable<RequestStatus> updateChartData(DataEntityWrapper dataEntityWrapper) {
        return chartProvider.updateChartData(dataEntityWrapper);
    }

    public Observable<BaseEntry> getSelection() {
        return baseEntrySelectionPublishSubject;
    }

    public void select(long selectedTimeMillis) {
        manualSelectEntryOnSelectedTime(chartProvider.getChart(), selectedTimeMillis, true, false);
    }

    private void manualSelectEntryOnSelectedTime(DataEntityLineChart chart, long selectedTimeMillis, boolean centerViewToSelection, boolean callListeners) {

        chart.getChartTouchListener()
                .setLastGesture(ChartTouchListener.ChartGesture.NONE);

        if (selectedTimeMillis < 0) {
            chart.highlightValue(null, false);
            chart.invalidate();
            return;
        }
        if (chart.getData() == null) {
            return;
        }

        BaseEntry entryFound = (BaseEntry) chartProvider.getEntryCacheMap().get(selectedTimeMillis);
        if (entryFound != null) {
            setSelectionEntry(entryFound, callListeners);
            chart.highlightValue(entryFound.getX(), entryFound.getY(), entryFound.getDataSetIndex(), callListeners);

            if (chart.getHighlighted() != null) {
                chartProvider.setSelectionHighlight(chart.getHighlighted()[0]);
            }

            if (centerViewToSelection) {
                chart.centerViewTo(entryFound.getX(), entryFound.getY(), YAxis.AxisDependency.LEFT);
            }
        }
    }

    private void setSelectionEntry(Entry entry, boolean publishSelection) {
        chartProvider.getChart().setHighlightedEntry(entry);

        if (publishSelection && (entry instanceof BaseEntry)) {
            baseEntrySelectionPublishSubject.onNext((BaseEntry) entry);
        }
    }

    private void resetMarkerAndClearSelection(DataEntityLineChart chart) {
        chartProvider.setSelectionHighlight(null);
        chartProvider.getChart().setHighlightedEntry(null);

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
        chartProvider.getChart().highlightCenterValueInTranslation();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        setSelectionEntry(e, true);
        chartProvider.setSelectionHighlight(h);
    }

    @Override
    public void onNothingSelected() {
        resetMarkerAndClearSelection(chartProvider.getChart());
    }

}
