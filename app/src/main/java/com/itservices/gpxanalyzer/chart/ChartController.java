package com.itservices.gpxanalyzer.chart;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itservices.gpxanalyzer.chart.entry.BaseDataEntityEntry;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener {

    private List<LineDataSet> currentLineDataSetList = new ArrayList<>();
    private Highlight currentHighlight;


    private final ChartProvider chartProvider;
    private final PublishSubject<BaseDataEntityEntry> baseEntrySelectionPublishSubject = PublishSubject.create();

    @Inject
    public ChartController(ChartProvider chartProvider) {
        this.chartProvider = chartProvider;
    }

    /**
     * Initialize the chart with no data (just styling).
     */
    @UiThread
    public void bindChart(@NonNull DataEntityLineChart chartBindings) {
        chartProvider.initChart(chartBindings);
        // clear any existing LiveData sets
        clearLineDataSets();

        chartBindings.setOnChartValueSelectedListener(this);
        chartBindings.setOnChartGestureListener(this);
    }

    @UiThread
    public RequestStatus tryToUpdateDataChart() {
        List<LineDataSet> dataSets = currentLineDataSetList;
        if (dataSets == null) return RequestStatus.ERROR_DATA_SETS_NULL;

        return chartProvider.updateChart(dataSets, currentHighlight);
    }

    @UiThread
    public RequestStatus refreshStatisticResults(StatisticResults statisticResults) {

        List<LineDataSet> newLineDataSetList = chartProvider.createCurveDataEntityDataSet(statisticResults);
        if (newLineDataSetList != null) {
            return addOrUpdateDataSetList(newLineDataSetList);
        }
        return RequestStatus.ERROR_LINE_DATA_SET_NULL;
    }

    @UiThread
    private RequestStatus addOrUpdateDataSetList(List<LineDataSet> newLineDataSetList) {
        if (newLineDataSetList == null)
            return RequestStatus.ERROR_NEW_DATA_SET_NULL;

        currentLineDataSetList = newLineDataSetList;

        // update the chart
        return tryToUpdateDataChart();
    }

    private void clearLineDataSets() {
        List<LineDataSet> sets = currentLineDataSetList;
        if (sets != null) {
            sets.clear();
            currentLineDataSetList = sets;

            // update the chart
            tryToUpdateDataChart();
        }
    }

    public Observable<BaseDataEntityEntry> getSelection() {
        return baseEntrySelectionPublishSubject;
    }

    private void setSelectionEntry(Entry entry, boolean publishSelection) {
        chartProvider.getChart().setHighlightedEntry(entry);

        if (publishSelection && (entry instanceof BaseDataEntityEntry)) {
            baseEntrySelectionPublishSubject.onNext((BaseDataEntityEntry) entry);
        }
    }

    private void setSelectionHighlight(Highlight h) {
        currentHighlight = h;
    }

    public void manualSelectEntry(long selectedTimeMillis) {
        ////Log.d(ChartController.class.getSimpleName(), "manualSelectEntry() called with: selectedTimeMillis = [" + selectedTimeMillis + "]");

        manualSelectEntryOnSelectedTime(chartProvider.getChart(), selectedTimeMillis, true, false);
    }

    private void manualSelectEntryOnSelectedTime(DataEntityLineChart lineChart, long selectedTimeMillis, boolean centerViewToSelection, boolean callListeners) {

        lineChart.getChartTouchListener()
                .setLastGesture(ChartTouchListener.ChartGesture.NONE);

        if (selectedTimeMillis < 0) {
            lineChart.highlightValue(null, false);
            lineChart.invalidate();
            return;
        }
        if (lineChart.getData() == null) {
            return;
        }

        BaseDataEntityEntry entryFound = (BaseDataEntityEntry)chartProvider.getEntryCacheMap().get(selectedTimeMillis);
        if (entryFound != null) {
            setSelectionEntry(entryFound, false);
            lineChart.highlightValue(entryFound.getX(), entryFound.getY(), entryFound.getDataSetIndex(), callListeners);

            setSelectionHighlight(lineChart.getHighlighted()[0]);

            if (centerViewToSelection) {
                lineChart.centerViewTo(entryFound.getX(), entryFound.getY(), YAxis.AxisDependency.LEFT);
            }
        }
    }

    private void resetMarkerAndClearSelection(DataEntityLineChart lineChart) {
        currentHighlight = null;
        chartProvider.getChart().setHighlightedEntry(null);

        manualSelectEntryOnSelectedTime(lineChart, -1, false, true);
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
        setSelectionHighlight(h);
    }

    @Override
    public void onNothingSelected() {
        resetMarkerAndClearSelection(chartProvider.getChart());
    }

    @UiThread
    public void setDrawIconsEnabled(boolean isChecked) {

        chartProvider.getSettings().setDrawIconsEnabled(isChecked);
        tryToUpdateDataChart();
    }

    @UiThread
    public boolean isDrawIconsEnabled() {

        if (chartProvider.getChart() != null) {
            LineData lineData = chartProvider.getChart().getData();
            if ( lineData != null && !lineData.getDataSets().isEmpty() ) {
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
}
