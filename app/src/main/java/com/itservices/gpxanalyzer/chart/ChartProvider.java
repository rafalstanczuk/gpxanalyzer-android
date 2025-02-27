package com.itservices.gpxanalyzer.chart;

import androidx.annotation.UiThread;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.RequestStatus;
import com.itservices.gpxanalyzer.data.StatisticResults;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class ChartProvider {

    @Inject
    LineChartSettings settings;

    @Inject
    ColorFilledLineDataSetListProvider lineDataSetListProvider;

    private DataEntityLineChart chart;

    private Highlight currentHighlight;

    @Inject
    public ChartProvider() {
    }

    /**
     * Initialize the chart with empty data + styling.
     */
    public void initChart(DataEntityLineChart chart) {
        this.chart = chart;
        chart.initChart(settings);

        clearLineDataSets();
    }

    public void setSelectionHighlight(Highlight h) {
        currentHighlight = h;
    }

    @UiThread
    public Single<RequestStatus> updateChartData(StatisticResults statisticResults) {

        chart.setStatisticResults(statisticResults);

        return lineDataSetListProvider
                .provide(statisticResults, settings, chart.getPaletteColorDeterminer())
                .observeOn(AndroidSchedulers.mainThread())
                .map(newLineDataSetList -> {

                    if (newLineDataSetList != null) {
                        return updateChart(lineDataSetListProvider.provide(), currentHighlight);
                    }
                    return RequestStatus.ERROR_LINE_DATA_SET_NULL;
                })
                .observeOn(Schedulers.io());
    }

    @UiThread
    public void tryToUpdateDataChart() {
        updateChart(lineDataSetListProvider.provide(), currentHighlight);
    }

    public DataEntityLineChart getChart() {
        return chart;
    }

    public LineChartSettings getSettings() {
        return settings;
    }

    public EntryCacheMap getEntryCacheMap() {
        return lineDataSetListProvider.getTrendBoundaryEntryProvider().getEntryCacheMap();
    }

    private void clearLineDataSets() {
        List<LineDataSet> sets = lineDataSetListProvider.provide();
        if (sets != null) {
            sets.clear();
            // update the chart
            tryToUpdateDataChart();
        }
    }


    /**
     * Combine the given datasets, apply styling and scaling, highlight if needed.
     */
    private RequestStatus updateChart(List<LineDataSet> dataSets,
                                      Highlight highlight) {
        if (chart == null)
            return RequestStatus.ERROR;

        if (dataSets == null) {
            return RequestStatus.ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW;
        }

        LineData lineData = new LineData();
        for (LineDataSet ds : dataSets) {
            lineData.addDataSet(ds);
        }

        chart.clear();
        chart.setData(lineData);
        chart.loadChartSettings(settings);
        chart.highlightValue(highlight, true);
        chart.invalidate();

        return RequestStatus.DONE;
    }
}
