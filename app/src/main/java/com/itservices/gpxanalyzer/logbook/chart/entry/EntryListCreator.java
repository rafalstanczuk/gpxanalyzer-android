package com.itservices.gpxanalyzer.logbook.chart.entry;

import android.location.Location;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;

import java.util.ArrayList;
import java.util.Vector;

public class EntryListCreator {
    public static ArrayList<Entry> createSingleMeasurementEntryList(
            StatisticResults statisticResults, PaletteColorDeterminer paletteColorDeterminer
    ) {
        Vector<Location> GPXPointVector = statisticResults.getMeasurements();

        int startXIndex = 0;
        int endXIndex = GPXPointVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (GPXPointVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            double value = GPXPointVector.get(i).getAltitude();

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
        Vector<Location> locationVector = statisticResults.getMeasurements();

        int startXIndex = 0;
        int endXIndex = locationVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (locationVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            double value = locationVector.get(i).getAltitude();

            scaledEntries.add(CurveMeasurementEntry.create(paletteColorDeterminer, statisticResults, i, (float) value));
        }

        return scaledEntries;
    }
}
