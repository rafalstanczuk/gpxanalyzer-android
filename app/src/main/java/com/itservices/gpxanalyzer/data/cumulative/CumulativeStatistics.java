package com.itservices.gpxanalyzer.data.cumulative;

import java.util.Objects;

/**
 * Represents cumulative statistical values for GPX data analysis.
 * <p>
 * This class encapsulates a cumulative value, its accuracy, and the unit
 * of measurement. It is used to track accumulated changes in various metrics
 * (such as elevation gain/loss, distance traveled, etc.) across data entities
 * in a GPX track.
 * <p>
 * CumulativeStatistics objects are typically associated with DataEntity
 * instances to provide context about how values have accumulated up to that point in the track.
 */
public final class CumulativeStatistics {
    /** The accumulated value. */
    private Float value = 0.0f;
    
    /** The accuracy of the accumulated value. */
    private Float valueAccuracy = 0.0f;
    
    /** The unit of measurement for the value. */
    private String unit = "";

    /**
     * Creates a new CumulativeStatistics instance with default values.
     * <p>
     * Initializes the value and accuracy to 0.0 and unit to an empty string.
     */
    public CumulativeStatistics() {}

    /**
     * Creates a new CumulativeStatistics instance with the specified values.
     * 
     * @param value The accumulated value
     * @param valueAccuracy The accuracy of the accumulated value
     * @param unit The unit of measurement for the value
     */
    public CumulativeStatistics(
            Float value,
            Float valueAccuracy,
            String unit
    ) {
        this.value = value;
        this.valueAccuracy = valueAccuracy;
        this.unit = unit;
    }

    /**
     * Gets the accumulated value.
     * 
     * @return The accumulated value
     */
    public Float value() {
        return value;
    }

    /**
     * Gets the accuracy of the accumulated value.
     * 
     * @return The accuracy
     */
    public Float valueAccuracy() {
        return valueAccuracy;
    }

    /**
     * Gets the unit of measurement for the value.
     * 
     * @return The unit as a string
     */
    public String unit() {
        return unit;
    }

    /**
     * Compares this CumulativeStatistics object with another object for equality.
     * <p>
     * Two CumulativeStatistics objects are considered equal if they have the
     * same value, value accuracy, and unit.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CumulativeStatistics) obj;
        return Objects.equals(this.value, that.value) &&
                Objects.equals(this.valueAccuracy, that.valueAccuracy) &&
                Objects.equals(this.unit, that.unit);
    }

    /**
     * Generates a hash code for this CumulativeStatistics object.
     * 
     * @return A hash code based on the value, value accuracy, and unit
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, valueAccuracy, unit);
    }

    /**
     * Returns a string representation of this CumulativeStatistics object.
     * <p>
     * Includes the value, value accuracy, and unit in a formatted string.
     *
     * @return A formatted string representation of this CumulativeStatistics object
     */
    @Override
    public String toString() {
        return "CumulativeStatistics[" +
                "value=" + value + ", " +
                "valueAccuracy=" + valueAccuracy + ", " +
                "unit=" + unit + ']';
    }
}
