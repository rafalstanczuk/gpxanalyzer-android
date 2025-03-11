package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.StatisticResults;

public class BaseEntry extends Entry {
    protected final DataEntity dataEntity;
    protected final StatisticResults statisticResults;
    private final int dataSetIndex;

    public BaseEntry(
            DataEntity dataEntity,
            int dataSetIndex,
            float x, float y, Drawable icon, StatisticResults statisticResults
    ) {
        super(x, y, icon);
        this.dataEntity = dataEntity;
        this.dataSetIndex = dataSetIndex;
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
        return "BaseEntry{" +
                "dataEntity=" + dataEntity +
                ", statisticResults=" + statisticResults +
                '}';
    }

    public int getDataSetIndex() {
        return dataSetIndex;
    }
}
