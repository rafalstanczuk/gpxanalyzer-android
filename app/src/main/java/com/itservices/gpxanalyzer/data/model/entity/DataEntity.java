package com.itservices.gpxanalyzer.data.model.entity;

import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single data point in the GPX analyzer application.
 * This class encapsulates all measurements and statistics associated with a single
 * point in time, including various data measures and cumulative statistics.
 *
 * The class is immutable except for the extraData field and cumulative statistics,
 * which can be modified after construction. It supports multiple data measures and
 * maintains cumulative statistics for each measure.
 */
public final class DataEntity {
    private final int id;
    private final long timestampMillis;

    private final List<DataMeasure> dataMeasureList;

    private Object extraData;
    private final List<Map<CumulativeProcessedDataType, CumulativeStatistics>>
            cumulativeStatisticsMapList
            = new ArrayList<>(
            Arrays.asList(
                    new HashMap<>(CumulativeProcessedDataType.values().length),
                    new HashMap<>(CumulativeProcessedDataType.values().length)
            )
    );

    /**
     * Creates a new DataEntity with the specified parameters.
     *
     * @param id The unique identifier for this data point
     * @param timestampMillis The timestamp in milliseconds when this data was recorded
     * @param dataMeasureList The list of measurements associated with this data point
     * @param extraData Additional data that can be associated with this entity
     */
    public DataEntity(
            int id,
            long timestampMillis,
            List<DataMeasure> dataMeasureList,
            Object extraData) {
        this.id = id;
        this.timestampMillis = timestampMillis;
        this.dataMeasureList = dataMeasureList;
        this.extraData = extraData;
    }

    /**
     * Returns the list of measurements associated with this data point.
     *
     * @return The list of DataMeasure objects
     */
    public List<DataMeasure> getMeasures() {
        return dataMeasureList;
    }

    /**
     * Returns the unique identifier of this data point.
     *
     * @return The ID value
     */
    public int id() {
        return id;
    }

    /**
     * Returns the timestamp when this data was recorded.
     *
     * @return The timestamp in milliseconds
     */
    public long timestampMillis() {
        return timestampMillis;
    }

    /**
     * Returns the extra data associated with this entity.
     *
     * @return The extra data object, or null if not set
     */
    public Object getExtraData() {
        return extraData;
    }

    /**
     * Sets the extra data for this entity.
     *
     * @param extraData The extra data to associate with this entity
     */
    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }

    /**
     * Returns the cumulative statistics for a specific measure and type.
     * If the statistics don't exist, they will be created.
     *
     * @param index The index of the measure to get statistics for
     * @param type The type of cumulative statistics to retrieve
     * @return The cumulative statistics for the specified measure and type
     */
    public CumulativeStatistics get(int index, CumulativeProcessedDataType type) {
        if (cumulativeStatisticsMapList.get(index).containsKey(type)) {
            return cumulativeStatisticsMapList.get(index).get(type);
        }
        cumulativeStatisticsMapList.get(index).put(type, new CumulativeStatistics());

        return cumulativeStatisticsMapList.get(index).get(type);
    }

    public void put(int index, CumulativeProcessedDataType type, CumulativeStatistics statistics) {
        cumulativeStatisticsMapList.get(index).put(type, statistics);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntity that)) return false;
        return id == that.id && timestampMillis == that.timestampMillis && Objects.equals(dataMeasureList, that.dataMeasureList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestampMillis, dataMeasureList);
    }
}
