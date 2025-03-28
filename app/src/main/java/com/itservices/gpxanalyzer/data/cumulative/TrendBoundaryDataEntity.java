package com.itservices.gpxanalyzer.data.cumulative;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.data.entity.DataEntity;

import java.util.List;
import java.util.Vector;

/**
 * Represents a boundary of data entities that form a single trend segment in GPX data.
 * <p>
 * This record encapsulates a collection of related data entities that form a coherent 
 * trend segment (such as an ascent, descent, or flat section) within a GPX track.
 * Each boundary contains statistics about the trend, the actual data entities that
 * make up the segment, and any extra data associated with those entities.
 * <p>
 * TrendBoundaryDataEntity objects are typically created by the {@link TrendBoundaryCumulativeMapper}
 * when analyzing GPX tracks to identify meaningful segments based on changes in elevation,
 * speed, or other metrics.
 * 
 * @param id A unique identifier for this trend boundary
 * @param trendStatistics Statistics about the trend (type, magnitude, cumulative values)
 * @param dataEntityVector The collection of data entities that form this trend boundary
 * @param extraDataList Additional data associated with the entities in this boundary
 */
public record TrendBoundaryDataEntity(int id,
                                      TrendStatistics trendStatistics,
                                      Vector<DataEntity> dataEntityVector,
                                      List<Object> extraDataList) {

    /**
     * Gets a display label for this trend boundary.
     * <p>
     * Currently returns the string representation of the boundary's ID.
     * This method can be overridden in subclasses to provide more
     * descriptive labels based on the trend type or characteristics.
     *
     * @return A string label for this trend boundary
     */
    public String getLabel() {
        return String.valueOf(id);
    }

    /**
     * Returns a string representation of this trend boundary.
     * <p>
     * Includes the boundary ID, trend statistics, and timestamps of the
     * first and last data entities in the boundary.
     *
     * @return A formatted string representation of this trend boundary
     */
    @NonNull
    @Override
    public String toString() {
        return "TrendBoundaryDataEntity{" +
                "id=" + id +
                ", trendStatistics=" + trendStatistics.toString() +
                ", beginTimestamp=" + dataEntityVector.firstElement().timestampMillis() +
                ", endTimestamp=" + dataEntityVector.lastElement().timestampMillis() +"}\n";
    }
}
