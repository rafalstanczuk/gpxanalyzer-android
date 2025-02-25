package com.itservices.gpxanalyzer.chart;

import android.graphics.Color;

import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.entry.TrendBoundaryEntry;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.data.provider.TrendStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class ColorFilledLineDataSetListProvider {


    private List<LineDataSet> dataSetList = new ArrayList<>();

    @Inject
    public ColorFilledLineDataSetListProvider() {
    }

    List<LineDataSet> createAndProvide(List<TrendBoundaryEntry> trendBoundaryEntryList, LineChartSettings settings) {
        dataSetList = trendBoundaryEntryList.stream()
                .map(boundaryEntry -> {
                    LineDataSet lineDataSet = new LineDataSet(boundaryEntry.entryList(), boundaryEntry.getLabel());

                    lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                    lineDataSet.setHighlightEnabled(true);
                    lineDataSet.setDrawCircles(false);

                    lineDataSet.setLineWidth(1.0f);
                    lineDataSet.setColor(Color.BLACK);

                    lineDataSet.setDrawHorizontalHighlightIndicator(false);

                    lineDataSet.setHighLightColor(Color.BLACK);
                    lineDataSet.setDrawIcons(settings.isDrawIconsEnabled());
                    lineDataSet.setDrawValues(false);

                    lineDataSet.setDrawFilled(true);

                    TrendStatistics trendStatistics = boundaryEntry.trendBoundaryDataEntity().trendStatistics();
                    lineDataSet.setFillColor(trendStatistics.trendType().getFillColor());
                    lineDataSet.setFillAlpha(trendStatistics.trendType().getFillAlpha());

                    return lineDataSet;
                })
                .collect(Collectors.toList());


        return dataSetList;
    }

    public boolean hasList() {
        return !dataSetList.isEmpty();
    }

    public List<LineDataSet> provide() {
        return dataSetList;
    }
}
