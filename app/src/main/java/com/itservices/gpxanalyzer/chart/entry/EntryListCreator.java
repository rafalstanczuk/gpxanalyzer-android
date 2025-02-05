package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;

import java.util.ArrayList;
import java.util.Vector;

public class EntryListCreator {
    public static ArrayList<Entry> createSingleMeasurementEntryList(
            StatisticResults statisticResults, PaletteColorDeterminer paletteColorDeterminer
    ) {
        Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

        int startXIndex = 0;
        int endXIndex = dataEntityVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (dataEntityVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            DataEntity dataEntity = dataEntityVector.get(i);
            double value = dataEntity.getValueList().get( dataEntity.getPrimaryDataIndex() );

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
        Vector<DataEntity> dataEntityVector = statisticResults.getDataEntityVector();

        int startXIndex = 0;
        int endXIndex = dataEntityVector.size();

        ArrayList<Entry> scaledEntries = new ArrayList<>();
        if (dataEntityVector.isEmpty()) {
            return scaledEntries;
        }

        for (int i = startXIndex; i < endXIndex; i++) {
            DataEntity dataEntity = dataEntityVector.get(i);
            double value = dataEntity.getValueList().get( dataEntity.getPrimaryDataIndex() );

            scaledEntries.add(CurveMeasurementEntry.create(paletteColorDeterminer, statisticResults, i, (float) value));
        }

        return scaledEntries;
    }
}
