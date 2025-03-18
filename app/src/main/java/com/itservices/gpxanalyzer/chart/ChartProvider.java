package com.itservices.gpxanalyzer.chart;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.RequestStatus;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class ChartProvider {

    private final AtomicReference<Highlight> currentHighlightRef = new AtomicReference<>();
    @Inject
    LineChartSettings settings;
    @Inject
    LineDataSetListProvider lineDataSetListProvider;
    private WeakReference<DataEntityLineChart> chartWeakReference;

    @Inject
    public ChartProvider() {
    }

    @NonNull
    private static LineData mapIntoLineData(List<LineDataSet> lineDataSetList) {
        LineData lineData = new LineData();
        lineDataSetList.forEach(lineData::addDataSet);
        return lineData;
    }

    public void registerBinding(DataEntityLineChart chart) {
        if (chart != null) {
            if (chartWeakReference != null)
                chartWeakReference.clear();

            chartWeakReference = new WeakReference<>(chart);

            initChart().subscribe();
            Log.d(ChartProvider.class.getSimpleName(), "chartWeakReference = [" + chartWeakReference + "]");
        }
    }

    /**
     * Initialize the chart with empty data + styling.
     *
     * @return
     */
    public Observable<RequestStatus> initChart() {
        Log.d(ChartProvider.class.getSimpleName(), "initChart chartWeakReference = [" + chartWeakReference + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Observable.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return chartWeakReference.get().initChart(settings)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(req -> {
                    List<LineDataSet> sets = lineDataSetListProvider.provide();
                    if (sets != null) {
                        sets.clear();
                        return tryToUpdateDataChart();
                    }
                    return Observable.just(RequestStatus.CHART_INITIALIZED);
                });
    }

    public void setSelectionHighlight(Highlight h) {
        currentHighlightRef.set(h);
    }

    @UiThread
    public Observable<RequestStatus> updateChartData(DataEntityWrapper dataEntityWrapper) {
        Log.d(ChartProvider.class.getSimpleName(), "updateChartData() called with: dataEntityWrapper = [" + dataEntityWrapper + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Observable.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return
                Observable.just(chartWeakReference.get())
                        .map(chart -> {
                            chart.setDataEntityWrapper(dataEntityWrapper);
                            return chart.getPaletteColorDeterminer();
                        })
                        .flatMap(palette -> lineDataSetListProvider
                                .provide(dataEntityWrapper, settings, palette))
                        .map(ChartProvider::mapIntoLineData)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(lineData -> updateChart(lineData, currentHighlightRef.get()))
                        .observeOn(Schedulers.io());
    }

    public Observable<RequestStatus> tryToUpdateDataChart() {
        return Observable.just(lineDataSetListProvider.provide())
                .subscribeOn(Schedulers.io())
                .map(settings::updateSettingsFor)
                .observeOn(Schedulers.computation())
                .map(ChartProvider::mapIntoLineData)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(lineData -> updateChart(lineData, currentHighlightRef.get()));

    }

    @Nullable
    public DataEntityLineChart getChart() {
        if (chartWeakReference != null)
            return chartWeakReference.get();
        else
            return null;
    }

    public synchronized LineChartSettings getSettings() {
        return settings;
    }

    public EntryCacheMap getEntryCacheMap() {
        return lineDataSetListProvider.getTrendBoundaryEntryProvider().getEntryCacheMap();
    }

    /**
     * Combine the given datasets, apply styling and scaling, highlight if needed.
     */
    private Observable<RequestStatus> updateChart(LineData lineData,
                                                  Highlight highlight) {
        return Observable.fromCallable(() -> {

            if (chartWeakReference == null)
                return RequestStatus.CHART_WEAK_REFERENCE_IS_NULL;

            DataEntityLineChart chart = chartWeakReference.get();

            if (chart == null)
                return RequestStatus.CHART_IS_NULL;

            synchronized (chart) {
                chart.clear();
                chart.setData(lineData);
                chart.loadChartSettings(settings);
                chart.highlightValue(highlight, true);
                chart.invalidate();
            }
            return RequestStatus.CHART_UPDATED;
        });
    }

}
