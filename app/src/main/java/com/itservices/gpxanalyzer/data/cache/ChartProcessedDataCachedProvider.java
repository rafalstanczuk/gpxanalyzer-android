package com.itservices.gpxanalyzer.data.cache;

import android.util.Log;

import com.github.mikephil.charting.data.LineData;
import com.itservices.gpxanalyzer.chart.LineChartSettings;
import com.itservices.gpxanalyzer.chart.entry.EntryCacheMap;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChartProcessedDataCachedProvider {

    public static ChartProcessedData EMPTY_CHART_PROCESSED_DATA =
            new ChartProcessedData(
                    new AtomicReference<>(new EntryCacheMap()),
                    new AtomicReference<>(new LineData(new ArrayList<>()))
            );
    private static final String TAG = ChartProcessedDataCachedProvider.class.getSimpleName();
    private static final int MAX_CHARTS_TO_CACHE = 10;
    private static final int MAX_DATASETS_PER_CHART = 20;

    private final ConcurrentMap<String, ConcurrentMap<DataEntityWrapper, AtomicReference<ChartProcessedData> >> chartDataMap = new ConcurrentHashMap<>(MAX_CHARTS_TO_CACHE);

    @Inject
    public ChartProcessedDataCachedProvider() {
    }

    private static boolean isNotEqualByHash(DataEntityWrapper dataEntityWrapper, DataEntityWrapper dataEntityWrapperSecond) {
        Vector<DataEntity> data = dataEntityWrapper.getData();
        Vector<DataEntity> dataSecond = dataEntityWrapperSecond.getData();

        if (data == dataSecond) return false;
        if (data == null || dataSecond == null) return true;
        if (data.size() != dataSecond.size()) return true;
        
        // More reliable hash comparison that only compares essential data
        long hash1 = dataEntityWrapper.getDataHash();
        long hash2 = dataEntityWrapperSecond.getDataHash();
        return hash1 != hash2;
    }

    public ChartProcessedData provide(DataEntityWrapper currentDataEntityWrapper, LineChartSettings settings) {
        if (currentDataEntityWrapper == null || settings == null) {
            return null;
        }

        String chartAddress = settings.getChartAddress();
        if (chartAddress == null || chartAddress.isEmpty()) {
            return null;
        }

        clearOldCachedData(currentDataEntityWrapper);

        ConcurrentMap<DataEntityWrapper, AtomicReference<ChartProcessedData>> map =
                chartDataMap.get(chartAddress);

        if (map == null) {
            return null;
        }

        AtomicReference<ChartProcessedData> chartProcessedDataAtomicReference = map.get(currentDataEntityWrapper);

        if (chartProcessedDataAtomicReference == null) {
            return null;
        }

        ChartProcessedData result = chartProcessedDataAtomicReference.get();
        if (result == null) {
            return null;
        }

        settings.updateSettingsFor(result.lineData().get());

        // Return a defensive copy to prevent external modifications
        return new ChartProcessedData(
                new AtomicReference<>( result.entryCacheMapAtomic().get() ),
                new AtomicReference<>( result.lineData().get() )
        );
    }

    private void clearOldCachedData(DataEntityWrapper currentDataEntityWrapper) {
        if (currentDataEntityWrapper == null || currentDataEntityWrapper.getData() == null) {
            return;
        }

        chartDataMap.forEach((chartAddress, dataMap) -> {
            if (dataMap != null) {
                dataMap.keySet().removeIf(wrapper -> {
                    if (wrapper == null || isNotEqualByHash(wrapper, currentDataEntityWrapper)) {
                        Log.d(TAG, "clearOldCachedData() remove: key keyDataEntityWrapperMap = [" + wrapper + "]");
                        return true;
                    }
                    return false;
                });
            }
        });
    }

    public void init(List<String> newChartsAddressList) {
        if (newChartsAddressList == null || newChartsAddressList.isEmpty()) {
            chartDataMap.clear();
            return;
        }
        
        clearOldChartsCachedData(newChartsAddressList);
    }

    /**
     * Clear cache data for charts that are not in the provided list while preserving data
     * for charts that are still present. This optimizes performance when switching chart modes.
     */
    private void clearOldChartsCachedData(List<String> newDataEntityLineChartAddressList) {
        if (newDataEntityLineChartAddressList == null || newDataEntityLineChartAddressList.isEmpty()) {
            // Don't clear everything if no addresses are provided - could be temporary state
            Log.w(TAG, "clearOldChartsCachedData called with empty list - ignoring");
            return;
        }
        
        Log.d(TAG, "clearOldChartsCachedData called with: newDataEntityLineChartAddressList = [" + newDataEntityLineChartAddressList + "]");

        // Find keys to remove (using removeIf for atomic operations)
        chartDataMap.keySet().removeIf(oldSavedChartAddress -> {
            boolean shouldRemove = newDataEntityLineChartAddressList.stream()
                    .noneMatch(chartAddress -> chartAddress.contentEquals(oldSavedChartAddress));
            
            if (shouldRemove) {
                Log.d(TAG, "clearOldChartsCachedData() remove: key oldSavedChartAddress= [" + oldSavedChartAddress + "]");
            } else {
                Log.d(TAG, "clearOldChartsCachedData() preserving: key oldSavedChartAddress= [" + oldSavedChartAddress + "]");
            }
            
            return shouldRemove;
        });
        
        // Check if we need to limit the cache size, but only if there are charts to remove
        if (chartDataMap.size() > MAX_CHARTS_TO_CACHE) {
            int toRemove = chartDataMap.size() - MAX_CHARTS_TO_CACHE;
            Log.d(TAG, "Chart cache over capacity, removing " + toRemove + " oldest entries");

            // Remove oldest entries that are not in the new list
            List<String> potentialKeysToRemove = new ArrayList<>();
            chartDataMap.keySet().stream()
                .filter(key -> !newDataEntityLineChartAddressList.contains(key))
                .forEach(potentialKeysToRemove::add);

            // If we found enough non-current charts to remove, use those
            if (potentialKeysToRemove.size() >= toRemove) {
                potentialKeysToRemove.stream().limit(toRemove).forEach(chartDataMap::remove);
            } else {
                // Otherwise just take the first few entries (least recently added)
                List<String> keysToRemove = new ArrayList<>(chartDataMap.keySet())
                    .subList(0, toRemove);
                keysToRemove.forEach(chartDataMap::remove);
            }
        }
    }

    public void add(LineChartSettings settings, DataEntityWrapper dataEntityWrapper, ChartProcessedData chartProcessedData) {
        if (settings == null || dataEntityWrapper == null || chartProcessedData == null) {
            return;
        }
        
        String chartAddress = settings.getChartAddress();
        if (chartAddress == null || chartAddress.isEmpty()) {
            return;
        }

        ChartProcessedData newDefensiveCopy = new ChartProcessedData(
                new AtomicReference<>( chartProcessedData.entryCacheMapAtomic().get() ),
                new AtomicReference<>( chartProcessedData.lineData().get() )
        );

        ConcurrentMap<DataEntityWrapper, AtomicReference<ChartProcessedData>> map;
        
        if (chartDataMap.containsKey(chartAddress)) {
            map = chartDataMap.get(chartAddress);

            if (map == null) {
                map = new ConcurrentHashMap<>(MAX_DATASETS_PER_CHART);
                chartDataMap.put(chartAddress, map);
            }
            
            // Check if we need to limit the per-chart cache size
            if (map.size() >= MAX_DATASETS_PER_CHART) {
                // Simple approach: clear half the cache when it gets full
                int toRemove = map.size() / 2;
                List<DataEntityWrapper> keysToRemove = new ArrayList<>(map.keySet()).subList(0, toRemove);
                keysToRemove.forEach(map::remove);
            }


            if (map.containsKey(dataEntityWrapper)) {
                AtomicReference<ChartProcessedData> ref = map.get(dataEntityWrapper);
                if (ref != null) {
                    ref.set(newDefensiveCopy); // Set defensive copy
                } else {
                    map.put(dataEntityWrapper, new AtomicReference<>(newDefensiveCopy));
                }
            } else {
                map.put(dataEntityWrapper, new AtomicReference<>(newDefensiveCopy));
            }
        } else {
            // Create new map if it doesn't exist
            map = new ConcurrentHashMap<>(MAX_DATASETS_PER_CHART);
            map.put(dataEntityWrapper, new AtomicReference<>(newDefensiveCopy));
            
            // Check if we need to limit the total charts cached
            if (chartDataMap.size() >= MAX_CHARTS_TO_CACHE) {
                // Simple approach: remove one random entry to make room
                if (!chartDataMap.isEmpty()) {
                    String keyToRemove = chartDataMap.keySet().iterator().next();
                    chartDataMap.remove(keyToRemove);
                    Log.d(TAG, "Cache limit reached. Removing chart: " + keyToRemove);
                }
            }
            
            chartDataMap.put(chartAddress, map);
        }
    }
    
    public void clear() {
        chartDataMap.clear();
    }
}
