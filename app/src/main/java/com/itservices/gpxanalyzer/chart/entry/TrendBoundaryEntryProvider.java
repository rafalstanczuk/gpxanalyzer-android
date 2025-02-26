package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

public class TrendBoundaryEntryProvider {


    @Inject
    EntryCacheMap entryCacheMap;


    @Inject
    public TrendBoundaryEntryProvider() {

    }

    public final EntryCacheMap getEntryCacheMap() {
        return entryCacheMap;
    }

    public List<TrendBoundaryEntry> provide(
            StatisticResults statisticResults, List<TrendBoundaryDataEntity> trendBoundaryList, PaletteColorDeterminer paletteColorDeterminer
    ) {
        entryCacheMap.init(statisticResults.getDataEntityVector().size());

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
