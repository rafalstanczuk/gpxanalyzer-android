package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

public class BaseDataEntityEntry extends Entry {
    protected final DataEntity dataEntity;
    protected final StatisticResults statisticResults;

    public BaseDataEntityEntry(
            DataEntity dataEntity,
            float x, float y, Drawable icon, StatisticResults statisticResults
    ) {
        super(x, y, icon);
        this.dataEntity = dataEntity;
        this.statisticResults = statisticResults;

    }

    public StatisticResults getStatisticResults() {
        return statisticResults;
    }

    public DataEntity getDataEntity() {
        return dataEntity;
    }

    @Override
    public String toString() {
        return "BaseDataEntityEntry{" +
                "dataEntity=" + dataEntity +
                ", statisticResults=" + statisticResults +
                '}';
    }
}
