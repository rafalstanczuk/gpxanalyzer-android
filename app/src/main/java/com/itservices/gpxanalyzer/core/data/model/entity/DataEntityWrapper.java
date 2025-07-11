package com.itservices.gpxanalyzer.core.data.model.entity;

import android.util.Log;

import com.itservices.gpxanalyzer.core.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.domain.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.domain.cumulative.CumulativeStatistics;

import java.util.Vector;

/**
 * A wrapper class that provides a simplified interface for accessing and analyzing
 * DataEntity objects. This class manages a collection of DataEntity objects and provides
 * methods for accessing their values, statistics, and metadata.
 *
 * The wrapper maintains a primary data index that determines which measure is used
 * for statistical calculations and value retrievals. It also provides methods for
 * accessing accuracy, names, units, and values for specific measures.
 */
public final class DataEntityWrapper {
    /** Default index for the primary data measure */
    public static final int DEFAULT_PRIMARY_DATA_INDEX = 0;
    private static final String TAG = DataEntityWrapper.class.getSimpleName();

    private int primaryDataIndex = DEFAULT_PRIMARY_DATA_INDEX;
    private DataEntityCache dataEntityCache;
    private long dataHash = -1;
    private int hashCode = -1;

    /**
     * Private constructor to prevent instantiation without parameters.
     */
    private DataEntityWrapper() {
    }

    /**
     * Creates a new DataEntityWrapper with the specified parameters.
     *
     * @param primaryDataIndex The index of the primary data measure to use
     * @param dataEntityCache The provider that manages the cached data entities
     */
    public DataEntityWrapper(int primaryDataIndex, DataEntityCache dataEntityCache) {
        this.primaryDataIndex = primaryDataIndex;
        this.dataEntityCache = dataEntityCache;
        dataHash = computeDataHash();
    }

    /**
     * Computes a hash value based on the primary data index and the sum of timestamps
     * of all data entities.
     *
     * @return The computed hash value
     */
    private long computeDataHash() {
        if (dataEntityCache == null) {
            return dataHash;
        }

        return dataEntityCache.getDataEntitityVector()
                        .stream()
                        .mapToLong(DataEntity::timestampMillis)
                        .sum();
    }

    /**
     * Returns a hash value that uniquely identifies the current state of the data.
     * If the hash hasn't been computed yet, it will be computed and cached.
     *
     * @return The hash value
     */
    public long getDataHash() {
        if (dataHash > 0)
            return dataHash;
        else {
            dataHash = computeDataHash();
        }
        return dataHash;
    }

    /**
     * Returns the vector of data entities managed by this wrapper.
     *
     * @return The vector of DataEntity objects
     */
    public Vector<DataEntity> getData() {
        if (dataEntityCache == null) {
            return new Vector<>();
        }
        return dataEntityCache.getDataEntitityVector();
    }

    /**
     * Returns the index of the primary data measure.
     *
     * @return The primary data index
     */
    public int getPrimaryDataIndex() {
        return primaryDataIndex;
    }

    /**
     * Returns the maximum value for the primary data measure.
     *
     * @return The maximum value, or 0.0 if no data is available
     */
    public double getMaxValue() {
        if (dataEntityCache == null)
            return 0.0;
        return dataEntityCache.getDataEntityStatistics().getMax(primaryDataIndex);
    }

    /**
     * Returns the minimum value for the primary data measure.
     *
     * @return The minimum value, or 0.0 if no data is available
     */
    public double getMinValue() {
        if (dataEntityCache == null)
            return 0.0;
        return dataEntityCache.getDataEntityStatistics().getMin(primaryDataIndex);
    }

    /**
     * Returns the accuracy of the measure at the specified index.
     *
     * @param index The index of the data entity
     * @return The accuracy value
     */
    public float getAccuracy(int index) {
        return dataEntityCache.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).valueAccuracy();
    }

    /**
     * Returns the accuracy of the measure for the specified data entity.
     *
     * @param dataEntity The data entity to get accuracy for
     * @return The accuracy value
     */
    public float getAccuracy(DataEntity dataEntity) {
        return dataEntity.getMeasures().get(primaryDataIndex).valueAccuracy();
    }

    /**
     * Returns the name of the measure at the specified index.
     *
     * @param index The index of the data entity
     * @return The measure name
     */
    public String getName(int index) {
        return dataEntityCache.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).name();
    }

    /**
     * Returns the name of the measure for the specified data entity.
     *
     * @param dataEntity The data entity to get name for
     * @return The measure name
     */
    public String getName(DataEntity dataEntity) {
        return dataEntity.getMeasures().get(primaryDataIndex).name();
    }

    /**
     * Returns the unit of the measure at the specified index.
     *
     * @param index The index of the data entity
     * @return The measure unit
     */
    public String getUnit(int index) {
        return dataEntityCache.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).unit();
    }

    /**
     * Returns the unit of the measure for the specified data entity.
     *
     * @param dataEntity The data entity to get unit for
     * @return The measure unit
     */
    public String getUnit(DataEntity dataEntity) {
        return dataEntity.getMeasures().get(primaryDataIndex).unit();
    }

    /**
     * Returns the value of the measure at the specified index.
     *
     * @param index The index of the data entity
     * @return The measure value
     */
    public float getValue(int index) {
        return dataEntityCache.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).value();
    }

    /**
     * Returns the value of the measure for the specified data entity.
     *
     * @param dataEntity The data entity to get value for
     * @return The measure value
     */
    public float getValue(DataEntity dataEntity) {
        return dataEntity.getMeasures().get(primaryDataIndex).value();
    }

    /**
     * Returns the cumulative statistics for the specified data entity and type.
     *
     * @param dataEntity The data entity to get statistics for
     * @param type The type of cumulative statistics to retrieve
     * @return The cumulative statistics
     */
    public CumulativeStatistics getCumulativeStatistics(DataEntity dataEntity, CumulativeProcessedDataType type) {
        return dataEntity.get(primaryDataIndex, type);
    }

    public void putCumulativeStatistics(DataEntity dataEntity, CumulativeProcessedDataType type, CumulativeStatistics statistics) {
        dataEntity.put(primaryDataIndex, type, statistics);
    }

    public static boolean isNotEqualByData(DataEntityWrapper firstWrapper, DataEntityWrapper secondWrapper) {
        Log.d(TAG, "isNotEqualByData() called with: firstWrapper = [" + firstWrapper + "], secondWrapper = [" + secondWrapper + "]");


        return firstWrapper.getPrimaryDataIndex() != secondWrapper.getPrimaryDataIndex();
    }

    public static boolean isNotEqualByDataHash(long firstWrapperDataHash, DataEntityWrapper secondWrapper) {
        Log.d(TAG, "isNotEqualByHash() called with: firstWrapperDataHash = [" + firstWrapperDataHash + "], secondWrapper = [" + secondWrapper + "]");

        // More reliable hash comparison that only compares essential data
        long hash1 = firstWrapperDataHash;
        long hash2 = secondWrapper.getDataHash();

        return hash1 != hash2;
    }
}
