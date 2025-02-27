package com.itservices.gpxanalyzer.data.gpx.calculation;

public class PrimitiveDataEntity {

    private long timestamp;
    private float value;
    private float accuracy;

    public PrimitiveDataEntity(long timestamp, float value, float accuracy) {
        this.timestamp = timestamp;
        this.value = value;
        this.accuracy = accuracy;
    }

    public static PrimitiveDataEntity copy(PrimitiveDataEntity entity) {
        return new PrimitiveDataEntity(
                entity.getTimestamp(),
                entity.getValue(),
                entity.getAccuracy()
        );
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getValue() {
        return value;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public boolean hasAccuracy() {
        return accuracy > 0.0f;
    }
}