package com.itservices.gpxanalyzer.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.CurveDataEntityEntry;
import com.itservices.gpxanalyzer.chart.entry.EntryListCreator;
import com.itservices.gpxanalyzer.chart.entry.SingleDataEntityEntry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class ChartProvider {

    private static final List<String> DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_DATA_ENTITY_BOUNDARIES =
            Arrays.asList(CurveDataEntityEntry.CURVE_DATA_ENTITY);

    @Inject
    LineChartSettings settings;

    private DataEntityLineChart chart;

    @Inject
    public ChartProvider() {
    }

    public LineDataSet createCurveDataEntityDataSet(StatisticResults curveResults) {
        if (curveResults == null) return null;

        PaletteColorDeterminer paletteColorDeterminer = chart.getPaletteColorDeterminer();
        paletteColorDeterminer.initPalette(curveResults);

        ArrayList<Entry> entries =
                EntryListCreator.createCurveDataEntityEntryList(curveResults, paletteColorDeterminer);

        // needed for scaling
        chart.getScaler().setDataEntityCurveStatisticResults(curveResults);

        if (!entries.isEmpty()) {
            return CurveDataEntityEntry.createCurveDataEntityLineDataSet(entries, settings);
        }
        return null;
    }

    public LineDataSet createSingleDataEntityDataSet(StatisticResults statisticResults) {
        if (statisticResults == null) return null;

        PaletteColorDeterminer paletteColorDeterminer = chart.getPaletteColorDeterminer();

        ArrayList<Entry> entries =
                EntryListCreator.createSingleDataEntityEntryList(statisticResults, paletteColorDeterminer);

        // needed for scaling
        chart.getScaler().setDataEntitySingleStatisticResults(statisticResults);

        return SingleDataEntityEntry.createSingleDataEntityLineDataSet(entries);
    }

    /**
     * Initialize the chart with empty data + styling.
     */
    public void initChart(DataEntityLineChart chart) {
        this.chart = chart;
        chart.initChart(settings);
    }

    /**
     * Combine the given datasets, apply styling and scaling, highlight if needed.
     */
    public RequestStatus updateChart(List<LineDataSet> dataSets,
                                     Highlight highlight) {

        if (!isDataSetAmountValidToShow(dataSets)) {
            return RequestStatus.ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW;
        }

        // combine all data sets into one lineData
        LineData lineData = new LineData();
        for (LineDataSet ds : dataSets) {
            lineData.addDataSet(ds);
        }

        chart.clear();
        chart.setData(lineData);
        chart.loadChartSettings(settings);
        chart.scale();
        chart.highlightValue(highlight, true);
        chart.invalidate();

        return RequestStatus.DONE;
    }

    private boolean isDataSetAmountValidToShow(final List<LineDataSet> dataSets) {
        int currentMatchedCount = 0;
        int requiredCount = DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_DATA_ENTITY_BOUNDARIES.size();

        for (String requiredLabel : DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_DATA_ENTITY_BOUNDARIES) {
            for (LineDataSet ds : dataSets) {
                if (ds.getLabel().contentEquals(requiredLabel)) {
                    currentMatchedCount++;
                }
            }
        }
        return (currentMatchedCount == requiredCount);
    }

    public DataEntityLineChart getChart() {
        return chart;
    }

    public LineChartSettings getSettings() {
        return settings;
    }
}
