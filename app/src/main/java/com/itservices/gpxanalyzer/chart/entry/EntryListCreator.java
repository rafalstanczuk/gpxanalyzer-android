package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

import java.util.ArrayList;
import java.util.Vector;

public class EntryListCreator {
    public static ArrayList<Entry> createSingleDataEntityEntryList(
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
            double value = dataEntity.getValueList().get( statisticResults.getPrimaryDataIndex() );

            scaledEntries.add(
                    SingleDataEntityEntry.create(paletteColorDeterminer, statisticResults, i,
                            (float) value
                    ));
        }
        return scaledEntries;
    }

    public static ArrayList<Entry> createCurveDataEntityEntryList(
            StatisticResults statisticResults, PaletteColorDeterminer paletteColorDeterminer, EntryCacheMap entryCacheMap
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
            double value = dataEntity.getValueList().get( statisticResults.getPrimaryDataIndex() );

            CurveDataEntityEntry entry = CurveDataEntityEntry.create(paletteColorDeterminer, statisticResults, i, (float) value);
            entryCacheMap.add(dataEntity.getTimestampMillis(), entry);

            scaledEntries.add(entry);
        }

        return scaledEntries;
    }
}
