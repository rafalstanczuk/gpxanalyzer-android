package com.itservices.gpxanalyzer.data.cache.type;

import static com.itservices.gpxanalyzer.data.entity.DataEntityUtils.calculateDataFingerprint;

import android.util.Log;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataEntityWrapperCachedProvider {

    private static final String TAG = DataEntityWrapperCachedProvider.class.getSimpleName();
    // Default wrapper used when no data is available

    @Inject
    DataEntityCachedProvider dataEntityCachedProvider;

    private static final DataEntityWrapper DEFAULT_DATA_ENTITY_WRAPPER = new DataEntityWrapper(0, null);

    // A single shared reference to the current data Vector
    private final AtomicReference<Vector<DataEntity>> sharedDataReference = new AtomicReference<>();

    // Cache of wrappers that use the shared data reference
    private ConcurrentMap<Short, DataEntityWrapper> concurrentMap;

    // Hash fingerprint of the current data for quick comparison
    private long currentDataFingerprint = -1;

    @Inject
    public DataEntityWrapperCachedProvider() {
        concurrentMap = new ConcurrentHashMap<>();
    }

    /**
     * Clears all cached DataEntityWrapper instances.
     * Note: This does not clear the shared data reference.
     */
    public synchronized void clear() {
        if (concurrentMap != null) {
            concurrentMap.clear();
        } else {
            concurrentMap = new ConcurrentHashMap<>();
        }
        Log.d(TAG, "Cache cleared");
    }

    public synchronized DataEntityWrapper provide(short primaryDataIndex) {
        // Handle null or empty data case
        if (primaryDataIndex < 0) {
            Log.w(TAG, "primaryDataIndex < 0");
            return DEFAULT_DATA_ENTITY_WRAPPER;
        }

        // Calculate fingerprint for the new data for comparison
        long newDataFingerprint = calculateDataFingerprint( dataEntityCachedProvider.getDataEntitityVector() );

        // Check if we have new data that needs to replace the current shared reference
        if (isNewData(dataEntityCachedProvider.getDataEntitityVector(), newDataFingerprint)) {
            Log.i(TAG, "Detected new data set with fingerprint: " + newDataFingerprint);

            // Update the shared reference and fingerprint
            sharedDataReference.set(dataEntityCachedProvider.getDataEntitityVector());
            currentDataFingerprint = newDataFingerprint;

            // Clear the cache since all wrappers should use the new data
            concurrentMap.clear();
            Log.i(TAG, "Cached wrappers cleared due to new data");
        }

        // Get or create the wrapper for this primary index
        DataEntityWrapper dataEntityWrapperForIndex = concurrentMap.get(primaryDataIndex);

        // Create a new wrapper if needed
        if (dataEntityWrapperForIndex == null) {
            Log.i(TAG, "Creating new wrapper for primaryDataIndex: " + primaryDataIndex);

            dataEntityWrapperForIndex = new DataEntityWrapper(primaryDataIndex, dataEntityCachedProvider);

            // Cache the new wrapper
            concurrentMap.put(primaryDataIndex, dataEntityWrapperForIndex);
        }

        return dataEntityWrapperForIndex;
    }

    /**
     * Determines if the provided data is different from currently cached data
     *
     * @param newData        New data vector
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
