package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.cache.type.DataEntityWrapperCachedProvider;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;

import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Responsible for initializing chart items with appropriate data.
 * This class handles connecting chart objects with their corresponding data sources,
 * mapping view modes to data indices, and preparing charts for rendering.
 * It serves as a bridge between the visual chart components and their underlying data.
 */
public class ChartInitializer {

    @Inject
    public GpxViewModeMapper viewModeMapper;

    @Inject
    public DataEntityWrapperCachedProvider dataEntityWrapperCachedProvider;

    /**
     * Creates a new ChartInitializer instance.
     * Uses Dagger for dependency injection.
     */
    @Inject
    ChartInitializer() {
    }

    /**
     * Initializes a chart item with the appropriate data wrapper based on its view mode.
     * This method:
     * 1. Initializes the chart controller
     * 2. Maps the chart's view mode to a primary key index
     * 3. Obtains the DataEntityWrapper for that index
     * 4. Associates the data wrapper with the chart item
     *
     * @param chartAreaItem The chart item to initialize
     * @param data The raw data entities available for charting
     * @return A Single that emits the initialized chart item
     */
    @NonNull
    Single<ChartAreaItem> initChartItemWithDataWrapper(ChartAreaItem chartAreaItem, Vector<DataEntity> data) {
        return chartAreaItem.getChartController()
                .initChart()
                .map(status -> {
                    GpxViewMode iChartViewMode = chartAreaItem.getViewMode().getValue();

                    if (iChartViewMode == null) {
                        Log.w("MultipleSyncedGpxChartUseCase", "ViewMode is null for chart item");
                        return chartAreaItem;
                    }

                    int primaryKeyIndex = viewModeMapper.mapToPrimaryKeyIndexList(iChartViewMode);
                    DataEntityWrapper dataEntityWrapper = dataEntityWrapperCachedProvider.provide( (short) primaryKeyIndex);

                    if (chartAreaItem.getDataEntityWrapper() == null ||
                            chartAreaItem.getDataEntityWrapper().getPrimaryDataIndex() != primaryKeyIndex) {
                        chartAreaItem.setDataEntityWrapper(dataEntityWrapper);
                        Log.d("MultipleSyncedGpxChartUseCase", "Set new DataEntityWrapper with index: " + primaryKeyIndex);
                    }

                    return chartAreaItem;
                });
    }
}
