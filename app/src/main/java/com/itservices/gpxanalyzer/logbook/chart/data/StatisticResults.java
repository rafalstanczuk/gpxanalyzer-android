package com.itservices.gpxanalyzer.logbook.chart.data;


import java.util.Vector;

public class StatisticResults {

    private double maxValue;
    private double minValue;

    private Vector<Measurement> measurements = new Vector<>();

    public StatisticResults(Vector<Measurement> measurements) {
        setMeasurements(measurements);
    }

    private void clear() {
        measurements.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
    }

    private void compute() {

        maxValue = measurements
                .stream()
                .map(m -> m.measurement)
                .reduce(Double.MIN_VALUE, Double::max);

        minValue = measurements
                .stream()
                .map(m -> m.measurement)
                .reduce(Double.MAX_VALUE, Double::min);
    }

    public final Vector<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Vector<Measurement> measurements) {
        clear();
        this.measurements = measurements;
        compute();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }
}
