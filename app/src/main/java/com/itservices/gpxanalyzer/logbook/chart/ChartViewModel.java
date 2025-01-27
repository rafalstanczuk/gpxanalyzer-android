package com.itservices.gpxanalyzer.logbook.chart;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.EntryListCreator;
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.logbook.chart.settings.LineChartSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChartViewModel extends ViewModel {

    private static final List<String> TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_MEASUREMENT_BOUNDARIES = Arrays.asList(
            CurveMeasurementEntry.CURVE_MEASUREMENT);

    private final MutableLiveData<List<LineDataSet>> lineDataSetListToAddLive = new MutableLiveData<>();

    private final MutableLiveData<Integer> entryToHighlightTimeInt = new MutableLiveData<>();
    private final MutableLiveData<Entry> highlightedEntry = new MutableLiveData<>();
    private final MutableLiveData<Highlight> highlight = new MutableLiveData<>();

    @Inject
    public LineChartScaledEntries lineChartScaledEntries;

    @Inject
    public LineChartSettings lineChartSettings;

    @Inject
    PaletteColorDeterminer paletteColorDeterminer;

    @Inject
    ChartViewModel() {
    }

    public MutableLiveData<Integer> getEntryToHighlightTimeInt() {
        return entryToHighlightTimeInt;
    }

    public LiveData<Entry> getHighlightedEntry() {
        return highlightedEntry;
    }

    public LiveData<List<LineDataSet>> getLineDataSetListToAddLive() {
        return lineDataSetListToAddLive;
    }

    public void updateCurveMeasurementLineDataSetFrom(
            StatisticResults curveMeasurementStatisticResults
    ) {
        if (curveMeasurementStatisticResults == null) {
            return;
        }

        ArrayList<Entry> entries =
                EntryListCreator.createCurveMeasurementEntryList(curveMeasurementStatisticResults, paletteColorDeterminer);

        lineChartScaledEntries.setMeasurementCurveStatisticResults(curveMeasurementStatisticResults);

        if (!entries.isEmpty()) {
            LineDataSet measurementCurveLineDataSet = CurveMeasurementEntry.createCurveMeasurementLineDataSet(entries);

            addToLineDataSetListLive(measurementCurveLineDataSet);
        }
    }

    public void updateSingleMeasurementDataSetFrom(
            StatisticResults statisticResults
    ) {
        if (statisticResults == null) {
            return;
        }

        ArrayList<Entry> entries = EntryListCreator.createSingleMeasurementEntryList(
                statisticResults, paletteColorDeterminer
        );

        lineChartScaledEntries.setMeasurementSingleStatisticResults(statisticResults);

        LineDataSet measurementLineDataSet = SingleMeasurementEntry.createSingleMeasurementLineDataSet(entries);

        addToLineDataSetListLive(measurementLineDataSet);
    }

    public void tryToUpdateDataChart(
            MeasurementLineChart lineChart, List<LineDataSet> newDataSetList
    ) {

        if (!isEnoughDataToShow(newDataSetList)) {
            return;
        }

        LineData lineData = new LineData();

        for (LineDataSet lineDataSet : newDataSetList) {
            lineData.addDataSet(lineDataSet);
        }

        lineChart.clear();
        lineChartSettings.setChartSettingsFor(lineChart);

        lineChart.setData(lineData);
        lineChartScaledEntries.update(lineChart);

        lineChart.highlightValue(highlight.getValue(), true);
        lineChart.invalidate();
    }

    private boolean isEnoughDataToShow(final List<LineDataSet> newDataSetList) {
        return checkPrecondition(
                newDataSetList, TARGET_MATCHED_LINE_LABEL_DATA_TO_SHOW_WITH_MEASUREMENT_BOUNDARIES);
    }

    private boolean checkPrecondition(
            final List<LineDataSet> newDataSetList, final List<String> minimumMatchedDataSet
    ) {
        int currentMatchedDataSetCount = 0;
        int minimumMatchedDataSetCount = minimumMatchedDataSet.size();

        for (String currentLabel : minimumMatchedDataSet) {
            for (LineDataSet lineDataSet : newDataSetList) {
                if (lineDataSet.getLabel().contentEquals(currentLabel)) {
                    currentMatchedDataSetCount++;
                }
            }
        }

        return currentMatchedDataSetCount == minimumMatchedDataSetCount;
    }

    private void addToLineDataSetListLive(LineDataSet lineDataSet) {
        List<LineDataSet> lineDataSetList = lineDataSetListToAddLive.getValue();
        if (lineDataSetList == null) {
            lineDataSetList = new ArrayList<>();
        }

        lineDataSetList.add(lineDataSet);

        lineDataSetListToAddLive.setValue(lineDataSetList);
    }

    public void init(MeasurementLineChart lineChart) {
        lineChart.clear();
        lineChart.setData(new LineData());
        lineChart.invalidate();

        clearLineDataSetListToAddLive();
        lineChartSettings.setChartSettingsFor(lineChart);
    }

    private void clearLineDataSetListToAddLive() {
        List<LineDataSet> lineDataSetList = lineDataSetListToAddLive.getValue();
        if (lineDataSetList != null) {
            lineDataSetList.clear();
            lineDataSetListToAddLive.setValue(lineDataSetList);
        } else {
            lineDataSetListToAddLive.setValue(new ArrayList<>());
        }
    }

    public void setSelectionEntry(Entry entry) {
        highlightedEntry.setValue(entry);
    }

    public void setSelectionHighlight(Highlight h) {
        highlight.setValue(h);
    }

    public void selectEntryForTime(int entryTimeToSelect) {
        entryToHighlightTimeInt.setValue(entryTimeToSelect);
    }

    public void selectMarker(MeasurementLineChart lineChart, long selectedColumnTimeInt) {

        if (selectedColumnTimeInt < 0) {
            lineChart.highlightValue(null, false);
            lineChart.invalidate();
            return;
        }

        if (lineChart.getData() == null) {
            return;
        }

        for (int dataSetIndex = 0;
             dataSetIndex < lineChart.getData().getDataSets().size(); dataSetIndex++) {

            ILineDataSet iLineDataSet = lineChart.getData().getDataSets().get(dataSetIndex);

            LineDataSet lineDataSet = (LineDataSet) iLineDataSet;

            for (Entry entry : lineDataSet.getValues()) {

                if (!(entry instanceof BaseEntry)) {
                    break;
                }

                Location location = ((BaseEntry) entry).getLocation();
                long timeInt = location.getTime();

                if (timeInt == selectedColumnTimeInt) {
                    lineChart.highlightValue(entry.getX(), entry.getY(), dataSetIndex, true);

                    return;
                }
            }
        }
    }

    public void resetMarkerAndSaveSelection(MeasurementLineChart lineChart) {
        selectMarker(lineChart, -1);
    }

    public void resetMarkerAndClearSelection(MeasurementLineChart lineChart) {
        highlight.setValue(null);
        highlightedEntry.setValue(null);
        selectMarker(lineChart, -1);
    }
}