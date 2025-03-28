package com.itservices.gpxanalyzer.data.entity;

import android.util.Log;

import com.itservices.gpxanalyzer.data.cache.type.DataEntityCachedProvider;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;

import java.util.Objects;
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
    private DataEntityCachedProvider dataEntityCachedProvider;
    private long dataEntityTimestampHash = -1;
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
     * @param dataEntityCachedProvider The provider that manages the cached data entities
     */
    public DataEntityWrapper(int primaryDataIndex, DataEntityCachedProvider dataEntityCachedProvider) {
        this.primaryDataIndex = primaryDataIndex;
        this.dataEntityCachedProvider = dataEntityCachedProvider;
        dataEntityTimestampHash = computeDataEntityTimestampHash();
    }

    /**
     * Computes a hash value based on the primary data index and the sum of timestamps
     * of all data entities.
     *
     * @return The computed hash value
     */
    private long computeDataEntityTimestampHash() {
        if (dataEntityCachedProvider == null) {
            return dataEntityTimestampHash;
        }

        return Objects.hash(
                getPrimaryDataIndex(),
                dataEntityCachedProvider.getDataEntitityVector()
                        .stream()
                        .mapToLong(DataEntity::timestampMillis)
                        .sum()
        );
    }

    /**
     * Returns a hash value that uniquely identifies the current state of the data.
     * If the hash hasn't been computed yet, it will be computed and cached.
     *
     * @return The hash value
     */
    public long getDataHash() {
        if (dataEntityTimestampHash > 0)
            return dataEntityTimestampHash;
        else {
            dataEntityTimestampHash = computeDataEntityTimestampHash();
        }
        return dataEntityTimestampHash;
    }

    /**
     * Returns the vector of data entities managed by this wrapper.
     *
     * @return The vector of DataEntity objects
     */
    public Vector<DataEntity> getData() {
        if (dataEntityCachedProvider == null) {
            return new Vector<>();
        }
        return dataEntityCachedProvider.getDataEntitityVector();
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
        if (dataEntityCachedProvider == null)
            return 0.0;
        return dataEntityCachedProvider.getDataEntityStatistics().getMax(primaryDataIndex);
    }

    /**
     * Returns the minimum value for the primary data measure.
     *
     * @return The minimum value, or 0.0 if no data is available
     */
    public double getMinValue() {
        if (dataEntityCachedProvider == null)
            return 0.0;
        return dataEntityCachedProvider.getDataEntityStatistics().getMin(primaryDataIndex);
    }

    /**
     * Returns the accuracy of the measure at the specified index.
     *
     * @param index The index of the data entity
     * @return The accuracy value
     */
    public float getAccuracy(int index) {
        return dataEntityCachedProvider.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).valueAccuracy();
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
        return dataEntityCachedProvider.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).name();
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
        return dataEntityCachedProvider.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).unit();
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
        return dataEntityCachedProvider.getDataEntitityVector().get(index).getMeasures().get(primaryDataIndex).value();
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

    public static boolean isNotEqualByHash(DataEntityWrapper firstWrapper, DataEntityWrapper secondWrapper) {
        Log.d(TAG, "isNotEqualByHash() called with: firstWrapper = [" + firstWrapper + "], secondWrapper = [" + secondWrapper + "]");

        Vector<DataEntity> data = firstWrapper.getData();
        Vector<DataEntity> dataSecond = secondWrapper.getData();

        if (data == dataSecond) return false;
        if (data == null || dataSecond == null) return true;
        if (data.size() != dataSecond.size()) return true;

        // More reliable hash comparison that only compares essential data
        long hash1 = firstWrapper.getDataHash();
        long hash2 = secondWrapper.getDataHash();

        Log.i(TAG, "isNotEqualByHash() called with: hash1 = [" + hash1 + "], hash2 = [" + hash2 + "]");

        return hash1 != hash2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntityWrapper that)) return false;
        return Double.compare(getMaxValue(), that.getMaxValue()) == 0 && Double.compare(getMinValue(), that.getMinValue()) == 0 && getPrimaryDataIndex() == that.getPrimaryDataIndex() && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        if (hashCode > 0) {
            return hashCode;
        }

        hashCode = computeHash();

        return hashCode;
    }

    private int computeHash() {
        return Objects.hash(getMaxValue(), getMinValue(), getData(), getPrimaryDataIndex());
    }
}
