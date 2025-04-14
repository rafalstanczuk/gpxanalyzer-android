package com.itservices.gpxanalyzer.data.mapper;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

/**
 * Utility class for mapping LineDataSet collections to LineData objects.
 * <p>
 * This class provides utility methods to convert collections of LineDataSet objects
 * into LineData objects that can be directly applied to MPAndroidChart LineChart views.
 * It serves as an adapter between the application's data preparation logic and 
 * the chart library's data model.
 */
public class LineDataSetMapper {
    /**
     * Maps a list of LineDataSet objects into a single LineData object.
     * <p>
     * This method takes a list of prepared LineDataSet objects and combines them
     * into a single LineData object that can be used to update a chart's visualization.
     * Each LineDataSet in the input list is added as a separate dataset in the 
     * resulting LineData object.
     *
     * @param lineDataSetList The list of LineDataSet objects to combine
     * @return A non-null LineData object containing all the provided datasets
     */
    @NonNull
    public static LineData mapIntoLineData(List<LineDataSet> lineDataSetList) {
        LineData lineData = new LineData();
        lineDataSetList.forEach(lineData::addDataSet);
        return lineData;
    }
}
