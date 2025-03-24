package com.itservices.gpxanalyzer.chart;

import static com.itservices.gpxanalyzer.data.cache.ChartProcessedDataCachedProvider.EMPTY_CHART_PROCESSED_DATA;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.cache.ChartProcessedData;
import com.itservices.gpxanalyzer.data.extrema.ExtremaSegmentListProvider;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryCumulativeMapper;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.cache.ChartProcessedDataCachedProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ChartProcessedDataProvider {

    private final AtomicReference<ChartProcessedData> chartProcessedDataAtomicReference = new AtomicReference<>(EMPTY_CHART_PROCESSED_DATA);
    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;
    @Inject
    ChartProcessedDataCachedProvider chartProcessedDataCachedProvider;

    @Inject
    public ChartProcessedDataProvider() {
    }

    public ChartProcessedData provide() {
        if ( chartProcessedDataAtomicReference.get() != null ) {
            return chartProcessedDataAtomicReference.get();
        }

        return EMPTY_CHART_PROCESSED_DATA;
    }

    public Single<ChartProcessedData> provide(DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        if (dataEntityWrapper == null)
            return Single.just(chartProcessedDataAtomicReference.get())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());


        ChartProcessedData cachedChartProcessedData = chartProcessedDataCachedProvider.provide(dataEntityWrapper, settings);
        if (cachedChartProcessedData != null) {
            chartProcessedDataAtomicReference.set(cachedChartProcessedData);
            return Single.just( cachedChartProcessedData )
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }

        return provideInternal(dataEntityWrapper, settings, paletteColorDeterminer);
    }

    private Single<ChartProcessedData> provideInternal(DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        return ExtremaSegmentListProvider.provide(dataEntityWrapper)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .map(segmentList -> TrendBoundaryCumulativeMapper.mapFrom(dataEntityWrapper, segmentList))
                        .map(trendBoundaryDataEntityList -> trendBoundaryEntryProvider
                                .provide(dataEntityWrapper, trendBoundaryDataEntityList, paletteColorDeterminer)
                        )
                        .map(trendBoundaryEntryList -> createLineDataSetList(trendBoundaryEntryList, settings))
                        .map(ChartProcessedDataProvider::mapIntoProcessedData)
                        .map(chartProcessedData -> {
                            chartProcessedDataCachedProvider.add(settings, dataEntityWrapper, chartProcessedData);

                            chartProcessedDataAtomicReference.set(chartProcessedData);

                            return chartProcessedData;
                        });
    }

    private static ChartProcessedData mapIntoProcessedData(List<LineDataSet> lineDataSetList) {
        EntryCacheMap entryCacheMap = new EntryCacheMap();
        entryCacheMap.update(lineDataSetList);

        LineData lineData = LineDataSetMapper.mapIntoLineData(lineDataSetList);

        return new ChartProcessedData(
                new AtomicReference<>(entryCacheMap),
                new AtomicReference<>(lineData)
        );
    }

    private List<LineDataSet> createLineDataSetList(List<TrendBoundaryEntry> trendBoundaryEntryList, LineChartSettings settings) {
        return trendBoundaryEntryList.stream().map(boundaryEntry -> {
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
        }).collect(Collectors.toList());
    }
}
