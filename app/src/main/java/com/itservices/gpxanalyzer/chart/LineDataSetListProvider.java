package com.itservices.gpxanalyzer.chart;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.extrema.ExtremaSegmentListProvider;
import com.itservices.gpxanalyzer.data.extrema.TrendBoundaryMapper;
import com.itservices.gpxanalyzer.data.TrendStatistics;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class LineDataSetListProvider {

    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;

    private List<LineDataSet> dataSetList = new ArrayList<>();


    @Inject
    public LineDataSetListProvider() {
    }

    public final TrendBoundaryEntryProvider getTrendBoundaryEntryProvider() {
        return trendBoundaryEntryProvider;
    }

    public List<LineDataSet> provide() {
        return dataSetList;
    }

    public Single<List<LineDataSet>> provide(DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        if (dataEntityWrapper == null)
            return Single.just(dataSetList);

        return  !dataSetList.isEmpty() ?
                Single.just(dataSetList)
                    :
                ExtremaSegmentListProvider
                        .provide(dataEntityWrapper)
                .map(segmentList -> TrendBoundaryMapper.mapFrom(dataEntityWrapper, segmentList))
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .map(trendBoundaryDataEntityList -> trendBoundaryEntryProvider.provide(dataEntityWrapper, trendBoundaryDataEntityList, paletteColorDeterminer))
                .map(trendBoundaryEntryList -> createAndProvide(trendBoundaryEntryList, settings))
                .map(data -> {
                    dataSetList = data;
                    return data;
                });
    }

    private List<LineDataSet> createAndProvide(List<TrendBoundaryEntry> trendBoundaryEntryList, LineChartSettings settings) {
        dataSetList = trendBoundaryEntryList.stream()
                .map(boundaryEntry -> {
                    LineDataSet lineDataSet = new LineDataSet(boundaryEntry.entryList(), boundaryEntry.getLabel());

                    lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                    lineDataSet.setHighlightEnabled(true);
                    lineDataSet.setDrawCircles(false);

                    lineDataSet.setLineWidth(1.0f);
                    lineDataSet.setColor(Color.BLACK);

                    lineDataSet.setDrawHorizontalHighlightIndicator(false);

                    lineDataSet.setHighLightColor(Color.BLACK);
                    lineDataSet.setDrawIcons(settings.isDrawIconsEnabled());
                    lineDataSet.setDrawValues(false);

                    lineDataSet.setDrawFilled(true);

                    TrendStatistics trendStatistics = boundaryEntry.trendBoundaryDataEntity().trendStatistics();
                    lineDataSet.setFillColor(trendStatistics.trendType().getFillColor());
                    lineDataSet.setFillAlpha(trendStatistics.trendType().getFillAlpha());

                    return lineDataSet;
                })
                .collect(Collectors.toList());


        return dataSetList;
    }


}
