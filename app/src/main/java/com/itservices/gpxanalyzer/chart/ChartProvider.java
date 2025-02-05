package com.itservices.gpxanalyzer.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.chart.entry.EntryListCreator;
import com.itservices.gpxanalyzer.chart.entry.SingleMeasurementEntry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Responsible for:
 *  - Creating LineDataSets from StatisticResults
 *  - Applying chart styling (delegates to LineChartSettings)
 *  - Updating chart scale (delegates to LineChartScaler)
 *  - Checking preconditions, setting data, re-applying highlights
 */
public class ChartProvider {

    private static final List<String> DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_MEASUREMENT_BOUNDARIES =
            Arrays.asList(CurveMeasurementEntry.CURVE_MEASUREMENT);

    private MeasurementLineChart measurementLineChart;

    @Inject
    public ChartProvider() {
    }

    public LineDataSet createCurveMeasurementDataSet(StatisticResults curveResults) {
        if (curveResults == null) return null;

        PaletteColorDeterminer paletteColorDeterminer = measurementLineChart.getPaletteColorDeterminer();
        paletteColorDeterminer.initPalette(curveResults);

        ArrayList<Entry> entries =
                EntryListCreator.createCurveMeasurementEntryList(curveResults, paletteColorDeterminer);

        // needed for scaling
        measurementLineChart.getLineChartScaler().setMeasurementCurveStatisticResults(curveResults);

        if (!entries.isEmpty()) {
            return CurveMeasurementEntry.createCurveMeasurementLineDataSet(entries);
        }
        return null;
    }

    public LineDataSet createSingleMeasurementDataSet(StatisticResults singleResults) {
        if (singleResults == null) return null;

        PaletteColorDeterminer paletteColorDeterminer = measurementLineChart.getPaletteColorDeterminer();

        ArrayList<Entry> entries =
                EntryListCreator.createSingleMeasurementEntryList(singleResults, paletteColorDeterminer);

        // needed for scaling
        measurementLineChart.getLineChartScaler().setMeasurementSingleStatisticResults(singleResults);

        return SingleMeasurementEntry.createSingleMeasurementLineDataSet(entries);
    }

    /**
     * Initialize the chart with empty data + styling.
     */
    public void initChart(MeasurementLineChart lineChart, LineChartSettings lineChartSettings) {
        this.measurementLineChart = lineChart;
        lineChart.initChart(lineChartSettings);
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

        measurementLineChart.clear();
        measurementLineChart.loadChartSettings();
        measurementLineChart.setData(lineData);
        measurementLineChart.scale();
        measurementLineChart.highlightValue(highlight, true);
        measurementLineChart.invalidate();

        return RequestStatus.DONE;
    }

    private boolean isDataSetAmountValidToShow(final List<LineDataSet> dataSets) {
        int currentMatchedCount = 0;
        int requiredCount = DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_MEASUREMENT_BOUNDARIES.size();

        for (String requiredLabel : DEFAULT_TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_MEASUREMENT_BOUNDARIES) {
            for (LineDataSet ds : dataSets) {
                if (ds.getLabel().contentEquals(requiredLabel)) {
                    currentMatchedCount++;
                }
            }
        }
        return (currentMatchedCount == requiredCount);
    }

    public MeasurementLineChart getMeasurementLineChart() {
        return measurementLineChart;
    }
}
