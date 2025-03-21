package com.itservices.gpxanalyzer.chart;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.extrema.ExtremaSegmentListProvider;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryCumulativeMapper;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.provider.LineDataSetListCachedProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class LineDataSetListProvider {

    private final AtomicReference<List<LineDataSet>> listLineDataSetAtomicReference = new AtomicReference<>(new ArrayList<>());
    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;
    @Inject
    LineDataSetListCachedProvider lineDataSetListCachedProvider;

    @Inject
    public LineDataSetListProvider() {
    }

    public synchronized final TrendBoundaryEntryProvider getTrendBoundaryEntryProvider() {
        return trendBoundaryEntryProvider;
    }

    public synchronized List<LineDataSet> provide() {
        return listLineDataSetAtomicReference.get();
    }

    public Observable<List<LineDataSet>> provide(DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        if (dataEntityWrapper == null)
            return Observable.just(listLineDataSetAtomicReference.get())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());


        List<LineDataSet> cachedList = lineDataSetListCachedProvider.provide(dataEntityWrapper, settings);
        if (cachedList != null) {
            return Observable.just( cachedList )
                    .doOnNext(listLineDataSetAtomicReference::set)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }

        return provideInternal(dataEntityWrapper, settings, paletteColorDeterminer);
    }

    private Observable<List<LineDataSet>> provideInternal(DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        return ExtremaSegmentListProvider.provide(dataEntityWrapper)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .map(segmentList -> TrendBoundaryCumulativeMapper.mapFrom(dataEntityWrapper, segmentList))
                        .map(trendBoundaryDataEntityList -> trendBoundaryEntryProvider
                                .provide(dataEntityWrapper, trendBoundaryDataEntityList, paletteColorDeterminer)
                        )
                        .map(trendBoundaryEntryList -> createAndProvide(trendBoundaryEntryList, settings))
                        .map(newLineDataSet -> {
                            lineDataSetListCachedProvider.add(dataEntityWrapper, newLineDataSet);

                            listLineDataSetAtomicReference.set(newLineDataSet);
                            return newLineDataSet;
                        });
    }

    private List<LineDataSet> createAndProvide(List<TrendBoundaryEntry> trendBoundaryEntryList, LineChartSettings settings) {
        listLineDataSetAtomicReference.set(trendBoundaryEntryList.stream().map(boundaryEntry -> {
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

            TrendStatistics trendStatistics = boundaryEntry.trendBoundaryDataEntity().trendStatistics();
            lineDataSet.setDrawFilled(settings.isDrawAscDescSegEnabled());
            lineDataSet.setFillColor(trendStatistics.trendType().getFillColor());
            lineDataSet.setFillAlpha(trendStatistics.trendType().getFillAlpha());

            return lineDataSet;
        }).collect(Collectors.toList()));

        return listLineDataSetAtomicReference.get();
    }
}
