package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class EntryListCreator {

    public static List<TrendBoundaryEntry> createTrendBoundaryEntryList(
            StatisticResults statisticResults, List<TrendBoundaryDataEntity> trendBoundaryList, PaletteColorDeterminer paletteColorDeterminer, EntryCacheMap entryCacheMap
    ) {

        List<TrendBoundaryEntry> trendBoundaryEntryList = new ArrayList<>();

        trendBoundaryList.forEach(trendBoundaryDataEntity -> {

            ArrayList<Entry> entryArrayList = new ArrayList<>();

            Vector<DataEntity> dataEntityVector = trendBoundaryDataEntity.dataEntityVector();

            dataEntityVector.forEach(dataEntity -> {
                CurveDataEntityEntry entry = CurveDataEntityEntry.create(dataEntity, trendBoundaryDataEntity.id(), paletteColorDeterminer, statisticResults);

                entryCacheMap.add(dataEntity.getTimestampMillis(), entry);

                entryArrayList.add(entry);
            });

            trendBoundaryEntryList.add(
                    new TrendBoundaryEntry(
                            trendBoundaryDataEntity.id(),
                            trendBoundaryDataEntity.trendType(),
                            trendBoundaryDataEntity.beginTimestamp(),
                            trendBoundaryDataEntity.endTimestamp(),
                            entryArrayList)
            );

        });

        return trendBoundaryEntryList;
    }
}
