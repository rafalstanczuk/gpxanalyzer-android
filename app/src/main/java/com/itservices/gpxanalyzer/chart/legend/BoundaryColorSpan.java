package com.itservices.gpxanalyzer.chart.legend;

public class BoundaryColorSpan {
    private final int id;
    private final String name;
    private final float min;
    private final float max;
    private final int color;

    public BoundaryColorSpan(int id, String name, float min, float max, int color) {
        this.id = id;
        this.name = name;
        this.min = min;
        this.max = max;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public float getMin() {
        return min;
    }


    public float getMax() {
        return max;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}
