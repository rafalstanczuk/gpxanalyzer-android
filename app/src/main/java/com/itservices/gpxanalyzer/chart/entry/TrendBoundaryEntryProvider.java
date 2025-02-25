package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TrendBoundaryEntryProvider {

    public static List<TrendBoundaryEntry> provide(
            StatisticResults statisticResults, List<TrendBoundaryDataEntity> trendBoundaryList, PaletteColorDeterminer paletteColorDeterminer, EntryCacheMap entryCacheMap
    ) {

        List<TrendBoundaryEntry> trendBoundaryEntryList = new ArrayList<>();

        trendBoundaryList.forEach(trendBoundaryDataEntity -> {

            ArrayList<Entry> entryArrayList = new ArrayList<>();

            Vector<DataEntity> dataEntityVector = trendBoundaryDataEntity.dataEntityVector();

            dataEntityVector.forEach(dataEntity -> {
                CurveDataEntityEntry entry = CurveDataEntityEntry.create(dataEntity, trendBoundaryDataEntity, paletteColorDeterminer, statisticResults);

                entryCacheMap.add(dataEntity.getTimestampMillis(), entry);

                entryArrayList.add(entry);
            });

            trendBoundaryEntryList.add(
                    new TrendBoundaryEntry(
                            trendBoundaryDataEntity,
                            entryArrayList)
            );

        });

        return trendBoundaryEntryList;
    }
}
