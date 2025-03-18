package com.itservices.gpxanalyzer.chart;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

class LineDataSetMapper {
    @NonNull
    public static LineData mapIntoLineData(List<LineDataSet> lineDataSetList) {
        LineData lineData = new LineData();
        lineDataSetList.forEach(lineData::addDataSet);
        return lineData;
    }
}
