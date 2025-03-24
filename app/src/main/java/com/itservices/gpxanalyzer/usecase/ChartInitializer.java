package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.cache.DataEntityWrapperCachedProvider;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;

import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Single;

public class ChartInitializer {

    @Inject
    public GpxViewModeMapper viewModeMapper;

    @Inject
    public DataEntityWrapperCachedProvider dataEntityWrapperCachedProvider;

    @Inject
    ChartInitializer() {
    }

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
                    DataEntityWrapper dataEntityWrapper = dataEntityWrapperCachedProvider.provide(data, (short) primaryKeyIndex);

                    if (chartAreaItem.getDataEntityWrapper() == null ||
                            chartAreaItem.getDataEntityWrapper().getPrimaryDataIndex() != primaryKeyIndex) {
                        chartAreaItem.setDataEntityWrapper(dataEntityWrapper);
                        Log.d("MultipleSyncedGpxChartUseCase", "Set new DataEntityWrapper with index: " + primaryKeyIndex);
                    }

                    return chartAreaItem;
                });
    }
}
