package com.itservices.gpxanalyzer.data.entity;

import java.util.Objects;

public record DataMeasure(Float value,
                          Float valueAccuracy,
                          String name,
                          String unit) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataMeasure that)) return false;
        return Objects.equals(value, that.value) && Objects.equals(name, that.name) && Objects.equals(unit, that.unit) && Objects.equals(valueAccuracy, that.valueAccuracy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, valueAccuracy, name, unit);
    }
}
