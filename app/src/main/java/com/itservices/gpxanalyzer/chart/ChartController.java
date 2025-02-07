package com.itservices.gpxanalyzer.chart;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ChartController implements OnChartValueSelectedListener, OnChartGestureListener {

    private List<LineDataSet> currentLineDataSetList = new ArrayList<>();
    private Highlight currentHighlight;
    private final ChartProvider chartProvider;
    private final PublishSubject<BaseEntry> baseEntrySelectionPublishSubject = PublishSubject.create();

    @Inject
    public ChartController(ChartProvider chartProvider) {
        this.chartProvider = chartProvider;
    }

    /**
     * Initialize the chart with no data (just styling).
     */
    @UiThread
    public void bindChart(@NonNull DataEntitiesLineChart lineChartBindings, @NonNull LineChartSettings lineChartSettings, @NonNull MainActivity mainActivity) {
        lineChartBindings.bindActivity(mainActivity);
        chartProvider.initChart(lineChartBindings, lineChartSettings);
        // clear any existing LiveData sets
        clearLineDataSets();

        lineChartBindings.setOnChartValueSelectedListener(this);
        lineChartBindings.setOnChartGestureListener(this);
    }

    @UiThread
    public RequestStatus tryToUpdateDataChart() {
        List<LineDataSet> dataSets = currentLineDataSetList;
        if (dataSets == null) return RequestStatus.ERROR_DATA_SETS_NULL;

        return chartProvider.updateChart(dataSets, currentHighlight);
    }

    @UiThread
    public RequestStatus refreshStatisticResults(StatisticResults statisticResults) {
        // treat everything as "curve" results
        return updateCurveDataEntityLineDataSetFrom(statisticResults);
    }

    @UiThread
    private RequestStatus updateCurveDataEntityLineDataSetFrom(StatisticResults stats) {
        LineDataSet ds = chartProvider.createCurveDataEntityDataSet(stats);
        if (ds != null) {
            return addOrUpdateDataSet(ds);
        }
        return RequestStatus.ERROR_LINE_DATA_SET_NULL;
    }

    private RequestStatus updateSingleDataEntityDataSetFrom(StatisticResults stats) {
        LineDataSet ds = chartProvider.createSingleDataEntityDataSet(stats);
        if (ds != null) {
            return addOrUpdateDataSet(ds);
        }
        return RequestStatus.ERROR_LINE_DATA_SET_NULL;
    }

    @UiThread
    private RequestStatus addOrUpdateDataSet(LineDataSet newDataSet) {
        if (newDataSet == null)
            return RequestStatus.ERROR_NEW_DATA_SET_NULL;

        if (currentLineDataSetList == null) {
            currentLineDataSetList = new ArrayList<>();
        }

        List<LineDataSet> current = currentLineDataSetList;

        if (current
                .stream()
                .noneMatch(a -> newDataSet.getLabel().contentEquals(a.getLabel())) ) {
            current.add(newDataSet);
        }



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

    public Observable<BaseEntry> getSelection() {
        return baseEntrySelectionPublishSubject;
    }

    private void setSelectionEntry(Entry entry, boolean publishSelection) {
        chartProvider.getMeasurementLineChart().setHighlightedEntry(entry);

        if (publishSelection && (entry instanceof BaseEntry) ) {
            baseEntrySelectionPublishSubject.onNext((BaseEntry) entry);
        }
    }

    private void setSelectionHighlight(Highlight h) {
        currentHighlight = h;
    }


    public void manualSelectEntry(long selectedTimeMillis) {
        manualSelectEntryOnSelectedTime(chartProvider.getMeasurementLineChart(), selectedTimeMillis, true,false);
    }

    private void manualSelectEntryOnSelectedTime(DataEntitiesLineChart lineChart, long selectedTimeMillis, boolean centerViewToSelection, boolean callListeners) {

        lineChart.getChartTouchListener()
                .setLastGesture( ChartTouchListener.ChartGesture.NONE );

        if (selectedTimeMillis < 0) {
            lineChart.highlightValue(null, false);
            lineChart.invalidate();
            return;
        }
        if (lineChart.getData() == null) {
            return;
        }

        for (int dataSetIndex = 0; dataSetIndex < lineChart.getData().getDataSets().size(); dataSetIndex++) {
            ILineDataSet iLineDataSet = lineChart.getData().getDataSets().get(dataSetIndex);

            if (!(iLineDataSet instanceof LineDataSet)) continue;
            LineDataSet lineDataSet = (LineDataSet) iLineDataSet;

            for (Entry entry : lineDataSet.getEntries()) {
                if (!(entry instanceof BaseEntry)) break;
                DataEntity dataEntity = ((BaseEntry) entry).getDataEntity();
                long timeInt = dataEntity.getTimestampMillis();
                if (timeInt == selectedTimeMillis) {
                    setSelectionEntry(entry, false);
                    lineChart.highlightValue(entry.getX(), entry.getY(), dataSetIndex, callListeners);

                    if(centerViewToSelection) {
                        lineChart.centerViewTo(entry.getX(), entry.getY(), YAxis.AxisDependency.LEFT);
                    }
                    return;
                }
            }
        }
    }

    private void resetMarkerAndClearSelection(DataEntitiesLineChart lineChart) {
        currentHighlight = null;
        chartProvider.getMeasurementLineChart().setHighlightedEntry(null);

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
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        chartProvider.getMeasurementLineChart().highlightCenterValueInTranslation();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        setSelectionEntry(e, true);
        setSelectionHighlight(h);
    }

    @Override
    public void onNothingSelected() {
        resetMarkerAndClearSelection(chartProvider.getMeasurementLineChart());
    }
}
