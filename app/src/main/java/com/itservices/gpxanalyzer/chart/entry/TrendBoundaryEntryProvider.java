package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;

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
                CurveEntry entry = CurveEntry.create(dataEntity, trendBoundaryDataEntity, paletteColorDeterminer, statisticResults);

                entryCacheMap.add(dataEntity.timestampMillis(), entry);

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
