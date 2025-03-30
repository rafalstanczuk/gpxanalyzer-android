package com.itservices.gpxanalyzer.data.cache.processed.chart;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;
import com.itservices.gpxanalyzer.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.raw.DataEntity;
import com.itservices.gpxanalyzer.data.raw.DataEntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Provider for trend boundary entries used in chart visualization.
 * <p>
 * This class is responsible for creating {@link TrendBoundaryEntry} objects from
 * {@link TrendBoundaryDataEntity} instances. It handles the conversion of data entities
 * within trend boundaries into chart-ready entries, applying appropriate styling based
 * on the trend type and adding them to the entry cache for efficient retrieval.
 * <p>
 * The provider is typically used during chart data preparation to convert segments of
 * GPX data with similar characteristics (like uphill sections or speed zones) into
 * visually distinct chart elements.
 */
public class TrendBoundaryEntryProvider {

    /**
     * Creates a new TrendBoundaryEntryProvider instance.
     * <p>
     * This constructor is intended for use with Dagger dependency injection.
     */
    @Inject
    public TrendBoundaryEntryProvider() {
    }

    public Single<List<TrendBoundaryEntry>> provide(
            EntryCacheMap entryCacheMap,
            RawDataProcessed rawDataProcessed,
            PaletteColorDeterminer paletteColorDeterminer
    ) {
        return Single.fromCallable(() -> {
            List<TrendBoundaryEntry> trendBoundaryEntryList = new ArrayList<>();

            DataEntityWrapper dataEntityWrapper = rawDataProcessed.dataEntityWrapperAtomic().get();

            List<TrendBoundaryDataEntity> trendBoundaryList = rawDataProcessed.trendBoundaryDataEntityListAtomic().get();

            trendBoundaryList.forEach(trendBoundaryDataEntity -> {
                List<Entry> entries = new ArrayList<>();

                Vector<DataEntity> dataEntityVector = trendBoundaryDataEntity.dataEntityVector();

                // Create the label for this trend boundary
                String label = formatTrendBoundaryLabel(trendBoundaryDataEntity);

                dataEntityVector.forEach(dataEntity -> {
                    CurveEntry entry = CurveEntry.create(dataEntity, trendBoundaryDataEntity, paletteColorDeterminer, dataEntityWrapper);

                    entryCacheMap.add(entry.getDataEntity().timestampMillis(), entry);

                    entries.add(entry);
                });

                trendBoundaryEntryList.add(
                        new TrendBoundaryEntry(
                                trendBoundaryDataEntity,
                                label,
                                entries,
                                dataEntityWrapper)
                );
            });

            return trendBoundaryEntryList;
        });
    }

    /**
     * Formats a human-readable label for a trend boundary.
     * <p>
     * This method creates a display label based on the properties of the
     * trend boundary, such as type, range, or other identifying characteristics.
     *
     * @param trendBoundaryDataEntity The trend boundary to create a label for
     * @return A formatted string label for display in charts and legends
     */
    private String formatTrendBoundaryLabel(TrendBoundaryDataEntity trendBoundaryDataEntity) {
        // Create a descriptive label based on the trend boundary properties
        return String.valueOf(trendBoundaryDataEntity.id());
    }
}
