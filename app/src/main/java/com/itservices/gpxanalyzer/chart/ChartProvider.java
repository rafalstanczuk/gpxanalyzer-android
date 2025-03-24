package com.itservices.gpxanalyzer.chart;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.github.mikephil.charting.highlight.Highlight;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.cache.ChartProcessedData;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class ChartProvider {

    private final AtomicReference<Highlight> currentHighlightRef = new AtomicReference<>();
    @Inject
    LineChartSettings settings;
    @Inject
    ChartProcessedDataProvider chartProcessedDataProvider;
    private WeakReference<DataEntityLineChart> chartWeakReference;

    @Inject
    public ChartProvider() {
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
    public Single<RequestStatus> initChart() {
        Log.d(ChartProvider.class.getSimpleName(), "initChart chartWeakReference = [" + chartWeakReference + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return chartWeakReference.get().initChart(settings)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(req -> {
                    ChartProcessedData chartProcessedData = chartProcessedDataProvider.provide();
                    if (chartProcessedData != null) {
                        //sets.clear();
                        return updateDataChart();

                    }
                    return Single.just(RequestStatus.CHART_INITIALIZED);
                });
    }

    public void setSelectionHighlight(Highlight h) {
        currentHighlightRef.set(h);
    }

    @UiThread
    public Single<RequestStatus> updateChartData(DataEntityWrapper dataEntityWrapper) {
        Log.d(ChartProvider.class.getSimpleName(), "updateChartData() called with: dataEntityWrapper = [" + dataEntityWrapper + "]");

        if (chartWeakReference == null || chartWeakReference.get() == null) {
            return Single.just(RequestStatus.CHART_WEAK_REFERENCE_IS_NULL);
        }

        return
                Single.just(chartWeakReference.get())
                        .map(chart -> {
                            chart.setDataEntityWrapper(dataEntityWrapper);
                            return chart.getPaletteColorDeterminer();
                        })
                        .flatMap(palette -> chartProcessedDataProvider
                                .provide(dataEntityWrapper, settings, palette))
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(lineData -> updateChart(lineData, currentHighlightRef.get()))
                        .observeOn(Schedulers.io());
    }

    public Single<RequestStatus> updateDataChart() {
        return Single.just(chartProcessedDataProvider.provide())
                .subscribeOn(Schedulers.io())
                .doOnEvent( (chartProcessedData, throwable) -> settings.updateSettingsFor(chartProcessedData.lineData().get()))
                .observeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(chartProcessedData -> updateChart(chartProcessedData, currentHighlightRef.get()));

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

    @Nullable
    public EntryCacheMap getEntryCacheMap() {
        if (chartProcessedDataProvider.provide() != null) {
            return chartProcessedDataProvider.provide().entryCacheMapAtomic().get();
        }

        return null;
    }

    /**
     * Combine the given datasets, apply styling and scaling, highlight if needed.
     */
    private Single<RequestStatus> updateChart(ChartProcessedData chartProcessedData,
                                                  Highlight highlight) {
        return Single.fromCallable(() -> {

            if (chartWeakReference == null)
                return RequestStatus.CHART_WEAK_REFERENCE_IS_NULL;

            DataEntityLineChart chart = chartWeakReference.get();

            if (chart == null)
                return RequestStatus.CHART_IS_NULL;

            synchronized (chart) {
                chart.clear();
                chart.setData(chartProcessedData.lineData().get());
                chart.loadChartSettings(settings);
                chart.highlightValue(highlight, true);
                chart.invalidate();
            }
            return RequestStatus.CHART_UPDATED;
        });
    }

}
