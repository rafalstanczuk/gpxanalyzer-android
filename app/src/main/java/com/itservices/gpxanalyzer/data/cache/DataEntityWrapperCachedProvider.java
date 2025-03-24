package com.itservices.gpxanalyzer.data.cache;

import android.util.Log;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataEntityWrapperCachedProvider {

    private static final String TAG = DataEntityWrapperCachedProvider.class.getSimpleName();
    private static final int MAX_CACHE_SIZE = 20;
    
    // Default wrapper used when no data is available
    private static final DataEntityWrapper DEFAULT_DATA_ENTITY_WRAPPER = new DataEntityWrapper(
            new Vector<>(
                    List.of(
                            new DataEntity(0,
                                    0L,
                                    List.of(0.0f),
                                    List.of(0.0f),
                                    List.of(""),
                                    List.of("")
                            )
                    )
            ),
            0);
    
    // A single shared reference to the current data Vector
    private AtomicReference<Vector<DataEntity>> sharedDataReference = new AtomicReference<>();
    
    // Cache of wrappers that use the shared data reference
    private ConcurrentMap<Short, DataEntityWrapper> concurrentMap;
    
    // Hash fingerprint of the current data for quick comparison
    private long currentDataFingerprint = -1;

    @Inject
    public DataEntityWrapperCachedProvider() {
        concurrentMap = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    }

    /**
     * Clears all cached DataEntityWrapper instances.
     * Note: This does not clear the shared data reference.
     */
    public synchronized void clear() {
        if (concurrentMap != null) {
            concurrentMap.clear();
        } else {
            concurrentMap = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
        }
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Provide a DataEntityWrapper for the given data and primary index
     * Uses shared references to avoid duplicate copies of data
     *
     * @param data Vector of DataEntity objects
     * @param primaryDataIndex Index of primary data to focus on
     * @return DataEntityWrapper instance that uses shared reference
     */
    public synchronized DataEntityWrapper provide(final Vector<DataEntity> data, short primaryDataIndex) {
        // Handle null or empty data case
        if (data == null || data.isEmpty()) {
            Log.w(TAG, "provide called with null or empty data");
            return DEFAULT_DATA_ENTITY_WRAPPER;
        }
        
        // Calculate fingerprint for the new data for comparison
        long newDataFingerprint = calculateDataFingerprint(data);
        
        // Check if we have new data that needs to replace the current shared reference
        if (isNewData(data, newDataFingerprint)) {
            Log.d(TAG, "Detected new data set with fingerprint: " + newDataFingerprint);
            // Update the shared reference and fingerprint
            sharedDataReference.set(data);
            currentDataFingerprint = newDataFingerprint;
            // Clear the cache since all wrappers should use the new data
            concurrentMap.clear();
            Log.d(TAG, "Cached wrappers cleared due to new data");
        }
        
        // Get or create the wrapper for this primary index
        DataEntityWrapper dataEntityWrapperForIndex = concurrentMap.get(primaryDataIndex);
        
        // Create a new wrapper if needed
        if (dataEntityWrapperForIndex == null) {
            Log.d(TAG, "Creating new wrapper for primaryDataIndex: " + primaryDataIndex);
            dataEntityWrapperForIndex = new DataEntityWrapper(sharedDataReference.get(), primaryDataIndex);
            
            // Check if we're at capacity and need to remove an entry
            if (concurrentMap.size() >= MAX_CACHE_SIZE) {
                Short keyToRemove = concurrentMap.keySet().iterator().next();
                concurrentMap.remove(keyToRemove);
                Log.d(TAG, "Cache limit reached, removing entry for index: " + keyToRemove);
            }
            
            // Cache the new wrapper
            concurrentMap.put(primaryDataIndex, dataEntityWrapperForIndex);
        }

        return dataEntityWrapperForIndex;
    }
    
    /**
     * Calculates a fingerprint for quick data comparison.
     * Optimized for large datasets by sampling a few entries rather than a full comparison.
     * 
     * @param data The data vector to fingerprint
     * @return A long value representing the data fingerprint
     */
    private long calculateDataFingerprint(Vector<DataEntity> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        
        // For very large datasets, we sample rather than check everything
        int dataSize = data.size();
        
        // Use sample size based on data size, max 10 samples
        int sampleSize = Math.min(10, dataSize);
        
        // Include data size as part of fingerprint
        long fingerprint = dataSize;
        
        // Sample distributed points throughout the data
        for (int i = 0; i < sampleSize; i++) {
            int index = (int)(((float)i / sampleSize) * dataSize);
            if (index < dataSize) {
                DataEntity entity = data.get(index);
                // Combine timestamp and first value if available
                fingerprint = 31 * fingerprint + entity.timestampMillis();
                if (!entity.valueList().isEmpty()) {
                    fingerprint = 31 * fingerprint + Float.floatToIntBits(entity.valueList().get(0));
                }
            }
        }
        
        // Also include first and last entries for better comparison
        if (dataSize > 0) {
            fingerprint = 31 * fingerprint + data.firstElement().timestampMillis();
            fingerprint = 31 * fingerprint + data.lastElement().timestampMillis();
        }
        
        return fingerprint;
    }
    
    /**
     * Determines if the provided data is different from currently cached data
     * 
     * @param newData New data vector
     * @param newFingerprint Pre-calculated fingerprint for the new data
     * @return true if the data is new/different
     */
    private boolean isNewData(Vector<DataEntity> newData, long newFingerprint) {
        // If we don't have cached data yet, this is new
        if (sharedDataReference.get() == null) {
            return true;
        }
        
        // If it's the same object instance, not new
        if (sharedDataReference.get() == newData) {
            return false;
        }
        
        // Compare fingerprints for quick check
        if (currentDataFingerprint != newFingerprint) {
            return true;
        }
        
        // Final size check as last verification
        return sharedDataReference.get().size() != newData.size();
    }
}
