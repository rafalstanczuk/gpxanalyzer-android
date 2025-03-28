package com.itservices.gpxanalyzer.chart;

import static com.itservices.gpxanalyzer.data.cache.processed.ChartProcessedDataCachedProvider.EMPTY_CHART_PROCESSED_DATA;

import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.cache.processed.ChartProcessedData;
import com.itservices.gpxanalyzer.data.extrema.ExtremaSegmentListProvider;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryCumulativeMapper;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.data.cache.processed.ChartProcessedDataCachedProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides processed chart data for visualization.
 * 
 * This class is responsible for transforming raw GPX data entities into chart-ready
 * processed data. It handles the creation of chart entries, line data sets, and
 * manages a caching system to improve performance for previously processed data.
 * 
 * The provider uses reactive programming (RxJava) to handle asynchronous data processing
 * and supports both retrieving cached data and generating new processed data when needed.
 */
public class ChartProcessedDataProvider {

    /** Atomic reference to the current processed chart data */
    private final AtomicReference<ChartProcessedData> chartProcessedDataAtomicReference = new AtomicReference<>(EMPTY_CHART_PROCESSED_DATA);
    
    /** Provides trend boundary entries for chart visualization */
    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;
    
    /** Caches processed chart data to improve performance */
    @Inject
    ChartProcessedDataCachedProvider chartProcessedDataCachedProvider;

    /**
     * Creates a new ChartProcessedDataProvider instance.
     * Uses Dagger for dependency injection.
     */
    @Inject
    public ChartProcessedDataProvider() {
    }

    /**
     * Provides the currently processed chart data.
     * Returns the current data if available, otherwise returns empty data.
     * 
     * @return The current ChartProcessedData
     */
    public ChartProcessedData provide() {
        if ( chartProcessedDataAtomicReference.get() != null ) {
            return chartProcessedDataAtomicReference.get();
        }

        return EMPTY_CHART_PROCESSED_DATA;
    }

    /**
     * Provides processed chart data for a specific data entity wrapper.
     * This method attempts to retrieve cached data first, and falls back to
     * generating new processed data if no cached data is available.
     * 
     * @param dataEntityWrapper The data wrapper containing GPX data to process
     * @param settings The chart settings to apply
     * @param paletteColorDeterminer The color determiner for chart elements
     * @return A Single that emits the processed chart data
     */
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

        return provideInternal(new EntryCacheMap(), dataEntityWrapper, settings, paletteColorDeterminer);
    }

    /**
     * Internal method to generate new processed chart data.
     * This method performs the complex transformation from raw data to chart-ready data,
     * including extrema detection, trend boundary mapping, and entry creation.
     * 
     * @param entryCacheMap The cache map to store entries for fast lookup
     * @param dataEntityWrapper The data wrapper containing GPX data to process
     * @param settings The chart settings to apply
     * @param paletteColorDeterminer The color determiner for chart elements
     * @return A Single that emits the processed chart data
     */
    private Single<ChartProcessedData> provideInternal(EntryCacheMap entryCacheMap, DataEntityWrapper dataEntityWrapper, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        return ExtremaSegmentListProvider.provide(dataEntityWrapper)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .map(segmentList -> TrendBoundaryCumulativeMapper.mapFrom(dataEntityWrapper, segmentList))
                        .map(trendBoundaryDataEntityList -> trendBoundaryEntryProvider
                                .provide(entryCacheMap, dataEntityWrapper, trendBoundaryDataEntityList, paletteColorDeterminer)
                        )
                        .map(trendBoundaryEntryList -> createLineDataSetList(trendBoundaryEntryList, settings))
                        .map(newLineDataSet -> mapIntoProcessedData(entryCacheMap, newLineDataSet))
                        .map(chartProcessedData -> {
                            chartProcessedDataCachedProvider.add(settings, dataEntityWrapper, chartProcessedData);

                            chartProcessedDataAtomicReference.set(chartProcessedData);

                            return chartProcessedData;
                        });
    }

    /**
     * Creates a LineData object from processed entry data.
     * This method wraps the processed data in the ChartProcessedData record
     * with atomic references for thread safety.
     * 
     * @param entryCacheMap The cache map containing entries for fast lookup
     * @param lineDataSetList The list of line data sets for the chart
     * @return A ChartProcessedData object containing the processed data
     */
    private static ChartProcessedData mapIntoProcessedData(EntryCacheMap entryCacheMap, List<LineDataSet> lineDataSetList) {
        LineData lineData = LineDataSetMapper.mapIntoLineData(lineDataSetList);

        return new ChartProcessedData(
                new AtomicReference<>(entryCacheMap),
                new AtomicReference<>(lineData)
        );
    }

    /**
     * Creates line data sets from trend boundary entries.
     * This method transforms the entry objects into chart-ready data sets
     * with appropriate visual styling based on settings.
     * 
     * @param trendBoundaryEntryList The list of trend boundary entries
     * @param settings The chart settings to apply to data sets
     * @return A list of LineDataSet objects ready for display
     */
    private static List<LineDataSet> createLineDataSetList(List<TrendBoundaryEntry> trendBoundaryEntryList, LineChartSettings settings) {
        return trendBoundaryEntryList.stream()
                .map( trendBoundaryEntry -> {
                    LineDataSet lineDataSet = new LineDataSet(trendBoundaryEntry.entries(), trendBoundaryEntry.getLabel());

                    TrendStatistics trendStatistics = trendBoundaryEntry.trendBoundaryDataEntity().trendStatistics();
                    lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

                    lineDataSet.setDrawCircles(false);
                    lineDataSet.setDrawCircleHole(false);

                    lineDataSet.setLineWidth(1.0f);
                    lineDataSet.setColor(Color.BLACK);

                    lineDataSet.setHighlightEnabled(true);
                    lineDataSet.setDrawVerticalHighlightIndicator(true);
                    lineDataSet.setDrawHorizontalHighlightIndicator(false);

                    lineDataSet.setDrawValues(false);

                    LineChartSettings.updateLineDataSetWithSettings(lineDataSet, settings);

                    lineDataSet.setFillColor(trendStatistics.trendType().getFillColor());
                    lineDataSet.setFillAlpha(trendStatistics.trendType().getFillAlpha());

                    return lineDataSet;
                }).collect(Collectors.toList());
    }
}
