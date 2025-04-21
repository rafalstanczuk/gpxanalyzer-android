package com.itservices.gpxanalyzer.usecase;

import static com.itservices.gpxanalyzer.events.RequestStatus.CHART_UPDATING;
import static com.itservices.gpxanalyzer.events.RequestStatus.DATA_LOADED;
import static com.itservices.gpxanalyzer.events.RequestStatus.LOADING;
import static com.itservices.gpxanalyzer.events.RequestStatus.PROCESSED;
import static com.itservices.gpxanalyzer.events.RequestStatus.PROCESSING;

import android.util.Log;

import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.data.provider.GpxDataEntityCachedProvider;
import com.itservices.gpxanalyzer.data.provider.RawDataProcessedProvider;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Responsible for loading GPX data and initializing chart components.
 * This class manages the data loading process for all chart elements in the application,
 * handling the full data loading pipeline from data retrieval to chart initialization and updating.
 * It publishes status updates during the loading process.
 */
public class LoadChartDataUseCase {
    private static final String TAG = LoadChartDataUseCase.class.getSimpleName();

    /**
     * Mapper to convert {@link GpxViewMode} to data indices.
     */
    @Inject
    public GpxViewModeMapper viewModeMapper;

    /**
     * Provider for accessing cached GPX data entities.
     */
    @Inject
    public GpxDataEntityCachedProvider dataEntityCachedProvider;

    /**
     * Cache holding raw GPX data entities.
     */
    @Inject
    DataEntityCache dataEntityCache;

    /**
     * Provider for accessing processed raw data suitable for charts.
     */
    @Inject
    RawDataProcessedProvider rawDataProcessedProvider;

    /**
     * Global event bus for publishing status updates (e.g., loading progress, errors).
     */
    @Inject
    GlobalEventWrapper eventWrapper;

    /**
     * Creates a new {@code LoadChartDataUseCase} instance.
     * Constructor is used by Dagger for dependency injection.
     */
    @Inject
    LoadChartDataUseCase() {
    }

    /**
     * Loads data for a list of {@link ChartAreaItem} instances and updates their corresponding charts.
     * This method orchestrates the entire data loading pipeline using RxJava:
     * <ol>
     *     <li>Fetches raw GPX data entities using {@link #dataEntityCachedProvider}.</li>
     *     <li>Publishes {@link RequestStatus#LOADING} and {@link RequestStatus#DATA_LOADED} events.</li>
     *     <li>Initializes each chart using the provided {@link ChartInitializerUseCase}.</li>
     *     <li>Publishes {@link RequestStatus#PROCESSING} event during initialization.</li>
     *     <li>Fetches processed data specific to each chart's view mode using {@link #provideDataFor(ChartAreaItem)}.</li>
     *     <li>Publishes {@link RequestStatus#PROCESSED} and {@link RequestStatus#CHART_UPDATING} events.</li>
     *     <li>Updates each chart with the processed data via {@link ChartAreaItem#updateChart(RawDataProcessed)}.</li>
     *     <li>Collects the final status of each chart update.</li>
     *     <li>Publishes the overall final status ({@link RequestStatus#DONE} or an error status) via the {@link #eventWrapper}.</li>
     * </ol>
     * All operations are performed on appropriate Schedulers (IO for data fetching, Computation for processing, MainThread for final status emission).
     *
     * @param chartAreaItemList The list of {@link ChartAreaItem} objects representing the charts to load data into.
     * @param chartInitializer  The use case responsible for the initial setup of each chart before data is loaded.
     * @return An {@link Observable} that emits the final overall {@link RequestStatus} of the data loading process (e.g., {@code DONE}, {@code ERROR}).
     *         Returns {@code Observable.just(RequestStatus.ERROR)} immediately if the input list is null or empty.
     */
    public Observable<RequestStatus> loadData(List<ChartAreaItem> chartAreaItemList, ChartInitializerUseCase chartInitializer) {
        if (chartAreaItemList == null || chartAreaItemList.isEmpty()) {
            Log.w(TAG, "Cannot load data - chart list is null or empty");
            return Observable.just(RequestStatus.ERROR);
        }

        Log.d(TAG, "Starting data loading for " + chartAreaItemList.size() + " charts");
        eventWrapper.onNext(LOADING);

        return dataEntityCachedProvider.provide()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnSuccess(data -> {
                    Log.d(TAG, "Data loaded successfully");
                    eventWrapper.onNext(DATA_LOADED);
                })
                .doOnError(throwable -> {
                    Log.e(TAG, "Error loading data", throwable);
                    eventWrapper.onNext(RequestStatus.ERROR);
                })
                .flatMapObservable(data -> initWithData(chartAreaItemList, chartInitializer))
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(requestStatusList -> {
                    RequestStatus finalStatus = requestStatusList.stream()
                            .min(Comparator.comparingInt(Enum::ordinal))
                            .orElse(RequestStatus.ERROR);

                    Log.d(TAG, "Data loading completed with status: " + finalStatus);
                    eventWrapper.onNext(finalStatus == RequestStatus.CHART_UPDATED ?
                            RequestStatus.DONE : finalStatus);
                    return Observable.just(finalStatus == RequestStatus.CHART_UPDATED ?
                            RequestStatus.DONE : finalStatus);
                })
                .doOnError(error -> {
                    Log.e(TAG, "Fatal error in data loading chain", error);
                    eventWrapper.onNext(RequestStatus.ERROR);
                })
                .doOnComplete(() -> {
                    Log.d(TAG, "Data loading chain completed");
                    eventWrapper.onNext(RequestStatus.DONE);
                });
    }

    /**
     * Initializes a list of charts sequentially and then updates them with data.
     * Helper method used within the {@link #loadData(List, ChartInitializerUseCase)} chain.
     *
     * @param chartAreaItemList List of chart items to initialize and update.
     * @param chartInitializer  The use case for initializing charts.
     * @return An Observable emitting the {@link RequestStatus} after each chart update.
     */
    private Observable<RequestStatus> initWithData(List<ChartAreaItem> chartAreaItemList, ChartInitializerUseCase chartInitializer) {
        return Observable.fromIterable(chartAreaItemList)
                .flatMapSingle(chartAreaItem -> chartInitializer.initChart(chartAreaItem)
                        .subscribeOn(Schedulers.computation())
                        .doOnError(e -> Log.e(TAG, "Error initializing chart item", e))
                        .doOnSuccess(item -> {
                            Log.d(TAG, "Chart item initialized successfully");
                            eventWrapper.onNext(PROCESSING);
                        }))
                .flatMapSingle(this::updateWithData)
                .flatMapSingle(requestStatusSingle -> requestStatusSingle)
                .flatMapSingle(requestStatus -> {
                    Log.d(TAG, "Updating chart with rawDataProcessed requestStatus: " + requestStatus);
                    return Single.just(requestStatus);
                });
    }

    /**
     * Fetches processed data for a specific {@link ChartAreaItem} and triggers its chart update.
     * Helper method used within the {@link #initWithData(List, ChartInitializerUseCase)} chain.
     *
     * @param chartAreaItem The chart item to update.
     * @return A {@link Single} emitting another {@link Single} which, upon subscription, updates the chart
     *         and emits the resulting {@link RequestStatus}.
     */
    private Single<Single<RequestStatus>> updateWithData(ChartAreaItem chartAreaItem) {
        return provideDataFor(chartAreaItem)
                .map(chartAreaItem::updateChart);
    }

    /**
     * Provides the processed data required for a specific chart based on its view mode.
     * Uses the {@link RawDataProcessedProvider} and creates a {@link DataEntityWrapper} specific
     * to the chart's required data columns.
     *
     * @param chartAreaItem The chart item for which to provide data.
     * @return A {@link Single} emitting the {@link RawDataProcessed} data for the chart.
     */
    private Single<RawDataProcessed> provideDataFor(ChartAreaItem chartAreaItem) {
        return rawDataProcessedProvider.provide(
                        createWrapperFor(chartAreaItem.getViewMode().getValue())
                )
                .observeOn(Schedulers.computation())
                .doOnError(e -> Log.e(TAG, "Error rawDataProcessedProvider", e))
                .doOnSuccess(rawDataProcessed -> {
                    Log.d(TAG, "rawDataProcessed successfully");
                    eventWrapper.onNext(PROCESSED);
                    eventWrapper.onNext(CHART_UPDATING);
                });
    }

    /**
     * Creates a {@link DataEntityWrapper} for a given {@link GpxViewMode}.
     * This wrapper tells the data provider which specific columns (primary key index)
     * are needed from the raw data cache for the given view mode.
     *
     * @param viewMode The {@link GpxViewMode} determining the required data.
     * @return A {@link DataEntityWrapper} configured for the view mode.
     */
    private DataEntityWrapper createWrapperFor(GpxViewMode viewMode) {
        int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(viewMode);

        return new DataEntityWrapper(primaryKeyIndex, dataEntityCache);
    }
}
