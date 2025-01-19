package com.itservices.gpxanalyzer.logbook.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.Measurement;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;

import java.util.ArrayList;
import java.util.Vector;

public class EntryListCreator {
    public static ArrayList<Entry> createSingleMeasurementEntryList(
            StatisticResults statisticResults, PaletteColorDeterminer paletteColorDeterminer
    ) {
        Vector<Measurement> measurementVector = statisticResults.getMeasurements();

        int startXIndex = 0;
        int endXIndex = measurementVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (measurementVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            double value = measurementVector.get(i).measurement;

            scaledEntries.add(
                    SingleMeasurementEntry.create(paletteColorDeterminer, statisticResults, i,
                            (float) value
                    ));
        }
        return scaledEntries;
    }

    public static ArrayList<Entry> createCurveMeasurementEntryList(
            StatisticResults statisticResults, PaletteColorDeterminer paletteColorDeterminer
    ) {
        Vector<Measurement> measurementVector = statisticResults.getMeasurements();

        int startXIndex = 0;
        int endXIndex = measurementVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (measurementVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            double value = measurementVector.get(i).measurement;

            scaledEntries.add(CurveMeasurementEntry.create(paletteColorDeterminer, statisticResults, i, (float) value));
        }

        return scaledEntries;
    }
}
