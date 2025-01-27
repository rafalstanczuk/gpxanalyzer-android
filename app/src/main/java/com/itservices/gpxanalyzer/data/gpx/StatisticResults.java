package com.itservices.gpxanalyzer.data.gpx;

import android.location.Location;

import java.util.DoubleSummaryStatistics;
import java.util.Vector;
import java.util.stream.Collectors;

public class StatisticResults {

    private double maxValue;
    private double minValue;

    private Vector<Location> locationVector = new Vector<>();

    public StatisticResults(Vector<Location> locationVector) {
        setMeasurements(locationVector);
    }

    private void clear() {
        locationVector.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
    }

    private void compute() {
        DoubleSummaryStatistics stats = locationVector.stream()
                .collect(Collectors.summarizingDouble(Location::getAltitude));
        minValue = stats.getMin();
        maxValue = stats.getMax();
    }

    public final Vector<Location> getMeasurements() {
        return locationVector;
    }

    public void setMeasurements(Vector<Location> locationVector) {
        clear();
        this.locationVector = locationVector;
        compute();
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }
}
