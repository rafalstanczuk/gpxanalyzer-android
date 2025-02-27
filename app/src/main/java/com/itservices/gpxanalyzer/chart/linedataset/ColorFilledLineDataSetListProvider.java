package com.itservices.gpxanalyzer.chart.linedataset;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntryProvider;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.provider.TrendBoundaryDataEntityProvider;
import com.itservices.gpxanalyzer.data.provider.TrendStatistics;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.data.statistics.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class ColorFilledLineDataSetListProvider {


    @Inject
    TrendBoundaryEntryProvider trendBoundaryEntryProvider;

    private List<LineDataSet> dataSetList = new ArrayList<>();

    private Disposable providerDisposable;

    @Inject
    public ColorFilledLineDataSetListProvider() {
    }

    public final TrendBoundaryEntryProvider getTrendBoundaryEntryProvider() {
        return trendBoundaryEntryProvider;
    }

    public List<LineDataSet> provide() {
        return dataSetList;
    }

    public Single<List<LineDataSet>> provide(StatisticResults statisticResults, LineChartSettings settings, PaletteColorDeterminer paletteColorDeterminer) {
        if (statisticResults == null)
            return Single.just(dataSetList);

        return  !dataSetList.isEmpty() ?
                Single.just(dataSetList)
                    :
                TrendBoundaryDataEntityProvider
                .provide(statisticResults)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .map(trendBoundaryDataEntityList -> trendBoundaryEntryProvider.provide(statisticResults, trendBoundaryDataEntityList, paletteColorDeterminer))
                .map(createTrendBoundaryEntryList -> createAndProvide(createTrendBoundaryEntryList, settings))
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
