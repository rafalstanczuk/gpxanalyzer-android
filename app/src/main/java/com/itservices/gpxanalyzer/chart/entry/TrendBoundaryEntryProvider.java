package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

public class TrendBoundaryEntryProvider {

    @Inject
    public TrendBoundaryEntryProvider() {

    }

    public List<TrendBoundaryEntry> provide(
            DataEntityWrapper dataEntityWrapper, List<TrendBoundaryDataEntity> trendBoundaryList, PaletteColorDeterminer paletteColorDeterminer
    ) {
        List<TrendBoundaryEntry> trendBoundaryEntryList = new ArrayList<>();

        trendBoundaryList.forEach(trendBoundaryDataEntity -> {

            ArrayList<Entry> entryArrayList = new ArrayList<>();

            Vector<DataEntity> dataEntityVector = trendBoundaryDataEntity.dataEntityVector();

            dataEntityVector.forEach(dataEntity -> {
                CurveEntry entry = CurveEntry.create(dataEntity, trendBoundaryDataEntity, paletteColorDeterminer, dataEntityWrapper);

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
