package com.itservices.gpxanalyzer.data.cumulative;

import java.util.Objects;

public final class CumulativeStatistics {
    private Float value = 0.0f;
    private Float valueAccuracy = 0.0f;
    private String unit = "";

    public CumulativeStatistics() {}

    public CumulativeStatistics(
            Float value,
            Float valueAccuracy,
            String unit
    ) {
        this.value = value;
        this.valueAccuracy = valueAccuracy;
        this.unit = unit;
    }

    public Float value() {
        return value;
    }

    public Float valueAccuracy() {
        return valueAccuracy;
    }

    public String unit() {
        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CumulativeStatistics) obj;
        return Objects.equals(this.value, that.value) &&
                Objects.equals(this.valueAccuracy, that.valueAccuracy) &&
                Objects.equals(this.unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, valueAccuracy, unit);
    }

    @Override
    public String toString() {
        return "CumulativeStatistics[" +
                "value=" + value + ", " +
                "valueAccuracy=" + valueAccuracy + ", " +
                "unit=" + unit + ']';
    }

}
