package com.itservices.gpxanalyzer.data.cache.processed.chart;

import android.util.Log;

import com.itservices.gpxanalyzer.ui.components.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.data.model.entity.DataEntityWrapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

/**
 * Cache mapping system for chart entries that provides efficient storage and retrieval
 * of {@link BaseEntry} objects indexed by timestamp.
 * <p>
 * This class uses a thread-safe concurrent map implementation to store chart entries,
 * enabling quick access by timestamp. It includes methods for adding, retrieving,
 * and clearing entries, as well as initializing with data from a {@link DataEntityWrapper}.
 * <p>
 * The cache has a maximum size limit to prevent out-of-memory issues when dealing
 * with large datasets.
 */
public class EntryCacheMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAX_CACHE_SIZE = 50000; // Limit cache size to prevent OOM

    private ConcurrentMap<Long, BaseEntry> entryMap = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);

    /**
     * Creates a new instance of the entry cache map.
     * <p>
     * This constructor is intended to be used with Dagger dependency injection.
     */
    @Inject
    public EntryCacheMap() {
    }

    /**
     * Adds an entry to the cache, using timestamp as the key.
     * <p>
     * If the cache has reached its maximum capacity, the entry will not be added
     * to prevent memory issues, and a log message will be generated.
     *
     * @param timestampMillis The timestamp in milliseconds to use as the key
     * @param entry The BaseEntry to store in the cache
     */
    public void add(long timestampMillis, BaseEntry entry) {
        // Don't add if we're at capacity to prevent memory issues
        if (entryMap.size() >= MAX_CACHE_SIZE) {
            Log.i(EntryCacheMap.class.getSimpleName(), "add() Don't add if we're at capacity to prevent memory issues = [" + timestampMillis + "], entry = [" + entry + "]");
            return;
        }
        entryMap.put(timestampMillis, entry);
    }

    /**
     * Retrieves an entry from the cache by its timestamp.
     *
     * @param timestampMillis The timestamp in milliseconds used as the key
     * @return The BaseEntry associated with the timestamp, or null if not found
     */
    public BaseEntry get(long timestampMillis) {
        return entryMap.get(timestampMillis);
    }

    /**
     * Initializes the cache with a new capacity based on the size of the provided data.
     * <p>
     * This method clears the existing cache and creates a new one with an appropriate
     * initial capacity based on the expected size of the data.
     *
     * @param dataEntityWrapper The data wrapper containing entities to be processed
     */
    public void init(DataEntityWrapper dataEntityWrapper) {
        int n = dataEntityWrapper.getData().size();

        entryMap.clear();
        // Use a reasonable initial capacity based on expected size
        int capacity = Math.min(n + 1, MAX_CACHE_SIZE);
        entryMap = new ConcurrentHashMap<>(capacity);
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        entryMap.clear();
    }
}
