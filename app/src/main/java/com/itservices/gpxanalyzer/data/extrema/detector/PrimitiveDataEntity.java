package com.itservices.gpxanalyzer.data.extrema.detector;

/**
 * A simplified data entity class used for detecting extrema (minima and maxima) in data series.
 * This class represents a single data point with its timestamp, value, and accuracy information.
 *
 * The class is designed to be used in conjunction with extrema detection algorithms
 * to identify significant points in time series data, such as speed peaks or elevation changes.
 */
public final class PrimitiveDataEntity {


    private final long index;
    private long timestamp;
    private double value;
    private float accuracy;

    /**
     * Creates a new PrimitiveDataEntity with the specified values.
     *
     * @param index The unique identifier for this data point
     * @param timestamp The time at which this data point was recorded
     * @param value The measured value at this point
     * @param accuracy The accuracy of the measurement
     */
    public PrimitiveDataEntity(long index, long timestamp, double value, float accuracy) {
        this.index = index;
        this.timestamp = timestamp;
        this.value = value;
        this.accuracy = accuracy;
    }

    /**
     * Creates a copy of an existing PrimitiveDataEntity.
     *
     * @param entity The entity to copy
     * @return A new PrimitiveDataEntity with the same values as the input entity
     */
    public static PrimitiveDataEntity copy(PrimitiveDataEntity entity) {
        return new PrimitiveDataEntity(
                entity.getIndex(),
                entity.getTimestamp(),
                entity.getValue(),
                entity.getAccuracy()
        );
    }

    /**
     * Returns the unique identifier of this data point.
     *
     * @return The index value
     */
    private long getIndex() {
        return index;
    }

    /**
     * Returns the timestamp of this data point.
     *
     * @return The timestamp value
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the measured value at this data point.
     *
     * @return The value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the accuracy of the measurement.
     *
     * @return The accuracy value
     */
    public float getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the timestamp of this data point.
     *
     * @param timestamp The new timestamp value
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the measured value at this data point.
     *
     * @param value The new value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Sets the accuracy of the measurement.
     *
     * @param accuracy The new accuracy value
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Checks if this data point has a valid accuracy value.
     *
     * @return true if the accuracy is greater than 0, false otherwise
     */
    public boolean hasAccuracy() {
        return accuracy > 0.0f;
    }
}