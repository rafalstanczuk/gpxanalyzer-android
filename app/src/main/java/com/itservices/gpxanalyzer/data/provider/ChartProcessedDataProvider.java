package com.itservices.gpxanalyzer.data.provider;

import static com.itservices.gpxanalyzer.data.cache.processed.chart.ChartProcessedDataCachedProvider.EMPTY_CHART_PROCESSED_DATA;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.data.cache.processed.chart.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.processed.chart.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.data.cache.processed.chart.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartProcessedData;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartProcessedDataCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ChartProcessedDataProvider {

    private static final String TAG = ChartProcessedDataProvider.class.getSimpleName();
    private final AtomicReference<ChartProcessedData> chartProcessedDataAtomicReference = new AtomicReference<>(EMPTY_CHART_PROCESSED_DATA);

    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;
    
    @Inject
    ChartProcessedDataCachedProvider chartProcessedDataCachedProvider;

    @Inject
    RawDataProcessedProvider rawDataProcessedProvider;

    @Inject
    public ChartProcessedDataProvider() {
    }

    public ChartProcessedData provide() {
        if ( chartProcessedDataAtomicReference.get() != null ) {
            return chartProcessedDataAtomicReference.get();
        }

        return EMPTY_CHART_PROCESSED_DATA;
    }

    public Single<ChartProcessedData> provide(RawDataProcessed rawDataProcessed, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        if (rawDataProcessed == null)
            return Single.just(chartProcessedDataAtomicReference.get())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());


        ChartProcessedData cachedChartProcessedData = chartProcessedDataCachedProvider.provide(rawDataProcessed, settings);
        if (cachedChartProcessedData != null) {
            chartProcessedDataAtomicReference.set(cachedChartProcessedData);
            return Single.just( cachedChartProcessedData )
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }

        return provideInternal(new EntryCacheMap(), rawDataProcessed, settings, paletteColorDeterminer);
    }

    private Single<ChartProcessedData> provideInternal(EntryCacheMap entryCacheMap, RawDataProcessed rawDataProcessed, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        return trendBoundaryEntryProvider
                .provide(entryCacheMap, rawDataProcessed, paletteColorDeterminer)
                        .map(trendBoundaryEntryList -> createLineDataSetList(trendBoundaryEntryList, settings))
                        .map(newLineDataSet -> mapIntoProcessedData(rawDataProcessed, entryCacheMap, newLineDataSet))
                        .map(chartProcessedData -> {

                            ChartSlot chartSlot = settings.getChartSlot();
                            GpxViewMode gpxViewMode = GpxViewMode.from(rawDataProcessed.dataEntityWrapperAtomic().get().getPrimaryDataIndex());
                            Log.i(TAG, "provideInternal: PROCESSED ChartProcessedData for ChartSlot: " + chartSlot + ", and GpxViewMode: " + gpxViewMode);

                            chartProcessedDataCachedProvider.add(settings, rawDataProcessed, chartProcessedData);

                            chartProcessedDataAtomicReference.set(chartProcessedData);

                            return chartProcessedData;
                        });
    }

    private static ChartProcessedData mapIntoProcessedData(RawDataProcessed rawDataProcessed, EntryCacheMap entryCacheMap, List<LineDataSet> lineDataSetList) {
        LineData lineData = LineDataSetMapper.mapIntoLineData(lineDataSetList);

        return new ChartProcessedData(
                new AtomicReference<>(rawDataProcessed.dataEntityWrapperAtomic().get().getDataHash()),
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
