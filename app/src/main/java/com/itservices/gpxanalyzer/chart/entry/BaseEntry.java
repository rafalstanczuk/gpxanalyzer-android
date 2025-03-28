package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

/**
 * Represents a data point on a GPX chart.
 * This class extends the MPAndroidChart Entry class to associate chart data points
 * with GPX data entities. It maintains references to both the individual data entity
 * represented by the point and the data entity wrapper that contains the overall dataset.
 * 
 * BaseEntry serves as the foundation for all chart entries in the application,
 * enabling selection synchronization between charts and providing access to the
 * underlying GPX data for each point on a chart.
 */
public class BaseEntry extends Entry {
    protected final DataEntity dataEntity;
    protected final DataEntityWrapper dataEntityWrapper;
    private final int dataSetIndex;

    /**
     * Creates a new BaseEntry with the specified properties.
     *
     * @param dataEntity The data entity represented by this entry
     * @param dataSetIndex The index of the dataset this entry belongs to
     * @param x The x-coordinate (typically time or distance)
     * @param y The y-coordinate (the value to display)
     * @param icon The icon to display at this entry point, or null for no icon
     * @param dataEntityWrapper The wrapper containing the dataset this entry belongs to
     */
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

    /**
     * Gets the data entity wrapper that contains the dataset this entry belongs to.
     *
     * @return The DataEntityWrapper
     */
    public DataEntityWrapper getDataEntityWrapper() {
        return dataEntityWrapper;
    }

    /**
     * Gets the data entity represented by this entry.
     *
     * @return The DataEntity
     */
    public DataEntity getDataEntity() {
        return dataEntity;
    }

    /**
     * Returns a string representation of this entry.
     *
     * @return A string containing the data entity and wrapper information
     */
    @Override
    public String toString() {
        return "BaseEntry{" +
                "dataEntity=" + dataEntity +
                ", dataEntityWrapper=" + dataEntityWrapper +
                '}';
    }

    /**
     * Gets the index of the dataset this entry belongs to.
     * This is used to identify which dataset in a chart contains this entry.
     *
     * @return The dataset index
     */
    public int getDataSetIndex() {
        return dataSetIndex;
    }
}
