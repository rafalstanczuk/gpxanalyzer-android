package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

public class BaseEntry extends Entry {
    protected final DataEntity dataEntity;
    protected final DataEntityWrapper dataEntityWrapper;
    private final int dataSetIndex;

    public BaseEntry(
            DataEntity dataEntity,
            int dataSetIndex,
            float x, float y, Drawable icon, DataEntityWrapper dataEntityWrapper
    ) {
        super(x, y, icon);
        this.dataEntity = dataEntity;
        this.dataSetIndex = dataSetIndex;
        this.dataEntityWrapper = dataEntityWrapper;

    }

    public DataEntityWrapper getDataEntityWrapper() {
        return dataEntityWrapper;
    }

    public DataEntity getDataEntity() {
        return dataEntity;
    }

    @Override
    public String toString() {
        return "BaseEntry{" +
                "dataEntity=" + dataEntity +
                ", dataEntityWrapper=" + dataEntityWrapper +
                '}';
    }

    public int getDataSetIndex() {
        return dataSetIndex;
    }
}
