package com.itservices.gpxanalyzer.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.CurveDataEntityEntry;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.chart.entry.EntryListCreator;
import com.itservices.gpxanalyzer.chart.entry.SingleDataEntityEntry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ChartProvider {

    @Inject
    LineChartSettings settings;

    @Inject
    EntryCacheMap entryCacheMap;

    private DataEntityLineChart chart;

    @Inject
    public ChartProvider() {
    }

    public LineDataSet createCurveDataEntityDataSet(StatisticResults statisticResults) {
        if (statisticResults == null) return null;

        PaletteColorDeterminer paletteColorDeterminer = chart.getPaletteColorDeterminer();
        paletteColorDeterminer.initPalette(statisticResults);

        entryCacheMap.init(statisticResults.getDataEntityVector().size());

        ArrayList<Entry> entries =
                EntryListCreator.createCurveDataEntityEntryList(statisticResults, paletteColorDeterminer, entryCacheMap);

        // needed for scaling
        chart.getScaler().setDataEntityCurveStatisticResults(statisticResults);

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
        if (chart == null)
            return RequestStatus.ERROR;

        if (dataSets.isEmpty()) {
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

    public DataEntityLineChart getChart() {
        return chart;
    }

    public LineChartSettings getSettings() {
        return settings;
    }

    public EntryCacheMap getEntryCacheMap() {
        return entryCacheMap;
    }
}
