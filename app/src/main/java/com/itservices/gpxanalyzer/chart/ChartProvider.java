package com.itservices.gpxanalyzer.chart;

import androidx.annotation.UiThread;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.chart.linedataset.ColorFilledLineDataSetListProvider;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.List;

import javax.inject.Inject;

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
    public RequestStatus updateChartData(StatisticResults statisticResults) {

        chart.setStatisticResults(statisticResults);

        List<LineDataSet> newLineDataSetList = lineDataSetListProvider.provide(statisticResults, settings, chart.getPaletteColorDeterminer());
        if (newLineDataSetList != null) {
            return updateChart(lineDataSetListProvider.provide(), currentHighlight);
        }
        return RequestStatus.ERROR_LINE_DATA_SET_NULL;
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
