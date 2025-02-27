package com.itservices.gpxanalyzer.data.extrema;

final class PrimitiveDataEntity {


    private final long index;
    private long timestamp;
    private double value;
    private float accuracy;

    public PrimitiveDataEntity(long index, long timestamp, double value, float accuracy) {
        this.index = index;
        this.timestamp = timestamp;
        this.value = value;
        this.accuracy = accuracy;
    }

    public static PrimitiveDataEntity copy(PrimitiveDataEntity entity) {
        return new PrimitiveDataEntity(
                entity.getIndex(),
                entity.getTimestamp(),
                entity.getValue(),
                entity.getAccuracy()
        );
    }

    private long getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public boolean hasAccuracy() {
        return accuracy > 0.0f;
    }
}