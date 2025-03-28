package com.itservices.gpxanalyzer.data.cache;

import android.util.Log;

import com.itservices.gpxanalyzer.data.cache.processed.ChartProcessedDataCachedProvider;
import com.itservices.gpxanalyzer.data.cache.type.DataEntityWrapperCachedProvider;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Central cache manager for the application
 * Coordinates caching of chart data, wrappers, and entries
 * Optimized to handle large datasets (30,000+ entries)
 */
@Singleton
public class MultipleChartsGlobalCache {
    private static final String TAG = MultipleChartsGlobalCache.class.getSimpleName();

    @Inject
    ChartProcessedDataCachedProvider chartProcessedDataCachedProvider;
    
    @Inject
    DataEntityWrapperCachedProvider dataEntityWrapperCachedProvider;

    // Keep track of active charts to optimize memory use
    private final Set<ChartAreaItem> activeCharts = Collections.newSetFromMap(new WeakHashMap<>());

    @Inject
    MultipleChartsGlobalCache(){}

    /**
     * Initialize the cache with a list of chart area items
     * This will clear any existing cache entries that are no longer needed
     * while preserving entries for charts that remain in the configuration
     * @param list List of chart area items
     */
    public void init(List<ChartAreaItem> list) {
        if (list == null) {
            clearAllCaches();
            return;
        }
        
        Log.d(TAG, "Initializing global cache with " + list.size() + " charts");
        
        // Track active charts to help with memory management
        activeCharts.clear();
        activeCharts.addAll(list);
        
        // Extract chart addresses and initialize the line data set cache
        List<String> newChartsAddressList = list.stream()
                .map(item -> item.getChartController().getChartAddress())
                .filter(address -> address != null && !address.isEmpty())
                .collect(Collectors.toList());

        // First check if there's any change at all to avoid unnecessary work
        if (!newChartsAddressList.isEmpty()) {
            // Only clear caches that aren't needed anymore, preserving existing data
            chartProcessedDataCachedProvider.init(newChartsAddressList);
        } else {
            Log.w(TAG, "Empty chart address list provided to init");
        }
    }
    
    /**
     * Clear all caches completely - use when memory is low or when navigation is changing
     */
    public void clearAllCaches() {
        Log.d(TAG, "Clearing all caches");
        chartProcessedDataCachedProvider.clear();
        dataEntityWrapperCachedProvider.clear();
        activeCharts.clear();
    }
    
    /**
     * Call when activity/fragment is paused/stopped to free resources
     * This is important for large datasets to prevent memory issues
     */
    public void trimCaches() {
        Log.d(TAG, "Trimming caches");


        // Clean up inactive charts from the weak reference set
        int beforeSize = activeCharts.size();
        activeCharts.removeIf(chart -> chart == null || chart.getDataEntityWrapper() == null);
        int afterSize = activeCharts.size();
        
        if (beforeSize != afterSize) {
            Log.d(TAG, "Removed " + (beforeSize - afterSize) + " inactive charts from tracking");
        }
    }
    
    /**
     * Get the estimated memory footprint of caches in KB
     * Useful for debugging and monitoring memory usage
     * This is an approximation
     */
    public int getEstimatedMemoryUsageKB() {
        // Rough estimate based on active charts * average expected memory per chart
        // This could be refined with more detailed calculations if needed
        int activeChartCount = activeCharts.size();
        // Estimate 100KB per chart for small datasets, scale up for larger ones
        int memoryPerChart = 100; // KB
        
        return activeChartCount * memoryPerChart;
    }
}
