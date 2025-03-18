package com.itservices.gpxanalyzer.chart;

import static com.github.mikephil.charting.charts.Chart.PAINT_GRID_BACKGROUND;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.settings.axis.AxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.chart.settings.highlight.CustomMarker;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class LineChartSettings {

    private static final float GRANULARITY = HourMinutesAxisValueFormatter.getFractionOfFullHourFromSeconds(1);
    private final CustomMarker customMarker;
    private final HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter;
    private final AxisValueFormatter dataEntityAxisValueFormatter;
    private LimitLinesBoundaries limitLinesBoundaries;
    private final Paint paintGridBg = new Paint();
    private final int primaryColor;
    private boolean drawXLabels = true;
    private boolean dragDecelerationEnabled = false;

    private boolean drawIconsEnabled = false;
    private boolean drawAscDescSegEnabled = false;

    @Inject
    LineChartSettings(
            @ApplicationContext Context context, CustomMarker customMarker,
            HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter,
            AxisValueFormatter dataEntityAxisValueFormatter
    ) {
        primaryColor = ContextCompat.getColor(context, R.color.lightBlue);
        this.customMarker = customMarker;
        this.hourMinutesAxisValueFormatter = hourMinutesAxisValueFormatter;
        this.dataEntityAxisValueFormatter = dataEntityAxisValueFormatter;

        paintGridBg.setStyle(Paint.Style.FILL);
        paintGridBg.setColor(Color.WHITE);
    }

    public boolean isDrawIconsEnabled() {
        return drawIconsEnabled;
    }

    public void setDrawIconsEnabled(boolean drawIconsEnabled) {
        this.drawIconsEnabled = drawIconsEnabled;
    }

    public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
        this.limitLinesBoundaries = limitLinesBoundaries;
        dataEntityAxisValueFormatter.setLimitLinesBoundaries(limitLinesBoundaries);
    }

    public void setDrawXLabels(boolean drawXLabels) {
        this.drawXLabels = drawXLabels;
    }

    public void setDragDecelerationEnabled(boolean dragDecelerationEnabled) {
        this.dragDecelerationEnabled = dragDecelerationEnabled;
    }


    public void setChartSettingsFor(DataEntityLineChart lineChart) {

        if (lineChart.getData() != null) {

            lineChart.getData().getDataSets().forEach(
                    lineDataSet -> lineDataSet.setDrawIcons(drawIconsEnabled)
            );
        }

        lineChart.setDragDecelerationEnabled(dragDecelerationEnabled);
        lineChart.setPaint(paintGridBg, PAINT_GRID_BACKGROUND);
        lineChart.setAutoScaleMinMaxEnabled(false);
        lineChart.setDrawGridBackground(true);

        lineChart.setNoDataText("Load data to show here.");
        lineChart.setNoDataTextColor(Color.RED);

        customMarker.setChartView(lineChart);
        customMarker.setSettings(this);
        lineChart.setMarker(customMarker);
        lineChart.setDrawBorders(false);
        lineChart.setMaxHighlightDistance(10000.0f);

        lineChart.resetZoom();
        lineChart.setMaxVisibleValueCount(20000);
        lineChart.setTouchEnabled(true);

        lineChart.setDragYEnabled(false);
        lineChart.setScaleYEnabled(false);

        lineChart.setDragXEnabled(true);
        lineChart.setScaleXEnabled(true);

        lineChart.setPinchZoom(false);

        setupYAxisRight(lineChart);
        setupYAxisLeft(lineChart);
        setupXAxis(lineChart);
        setupDescriptions(lineChart);
    }

    private void setupXAxis(DataEntityLineChart lineChart) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(drawXLabels);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(GRANULARITY);
        xAxis.setLabelCount(12, false);
        xAxis.setValueFormatter(hourMinutesAxisValueFormatter);

        xAxis.setLabelRotationAngle(-45.0f);
        xAxis.setTextColor(Color.BLACK);
    }

    private void setupDescriptions(DataEntityLineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
/*
		lineChart.getDescription().setEnabled(true);

		Description description = new Description();
		description.setText(descriptionString);
		description.setTextColor(Color.BLACK);
		description.setTextSize(12);
		description.setEnabled(true);
		description.setTextAlign(Paint.Align.LEFT);
		description.setPosition(10, 20);

		lineChart.setDescription(description);*/
    }

    private void setupYAxisRight(DataEntityLineChart lineChart) {
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
    }

    private void setupYAxisLeft(DataEntityLineChart lineChart) {
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setDrawGridLines(false);
//		yAxisLeft.setGridDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
//		yAxisLeft.setAxisLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
        yAxisLeft.setGranularityEnabled(false);
        yAxisLeft.setValueFormatter(dataEntityAxisValueFormatter);
        yAxisLeft.setEnabled(true);
        yAxisLeft.setTextColor(primaryColor);

        yAxisLeft.removeAllLimitLines();

        if (yAxisLeft.getLimitLines().isEmpty()) {
            limitLinesBoundaries.addLimitLinesInto(yAxisLeft);
        }
    }

    public boolean isDrawAscDescSegEnabled() {
        return drawAscDescSegEnabled;
    }

    public void setDrawAscDescSegEnabled(boolean drawAscDescSegEnabled) {
        this.drawAscDescSegEnabled = drawAscDescSegEnabled;
    }

    public List<LineDataSet> updateSettingsFor(List<LineDataSet> lineDataSetList) {
        lineDataSetList.forEach(
                lineDataSet -> {
                    lineDataSet.setDrawFilled(isDrawAscDescSegEnabled());
                }
        );

        return lineDataSetList;
    }
}
