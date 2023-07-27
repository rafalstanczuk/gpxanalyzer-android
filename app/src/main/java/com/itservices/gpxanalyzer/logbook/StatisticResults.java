package com.itservices.gpxanalyzer.logbook;


import static com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil.getTimeAsIntFromDate;

import java.util.Calendar;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StatisticResults {

    private double maxValue;
    private double minValue;

    private Vector<Measurement> measurements = new Vector<>();

    @Inject
    public StatisticResults() {
    }

    public void initStat() {

        Calendar cal = Calendar.getInstance();

        clear();

        for(int i = 0; i<100; i++) {
            Measurement measurement = new Measurement();

            measurement.measurement = (double) (Math.random() * 100 + 50);

            cal.add(Calendar.MINUTE, 1);

            measurement.timestamp = (long) getTimeAsIntFromDate(cal);

            addMeasurements(measurement);
        }

        compute();
    }

    public void clear() {
        measurements.clear();

        maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
    }

    public void compute() {

        maxValue = measurements
                .stream()
                .map(m -> m.measurement)
                .reduce(Double.MIN_VALUE, Double::max);

        minValue = measurements
                .stream()
                .map(m -> m.measurement)
                .reduce(Double.MAX_VALUE, Double::min);
    }

    public Vector<Measurement> getMeasurements() {
        return measurements;
    }

    public void addMeasurements(Measurement measurement) {
        measurements.add(measurement);
    }

    public void setMeasurements(Vector<Measurement> measurements) {
        this.measurements = measurements;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }
}
