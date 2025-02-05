package com.itservices.gpxanalyzer.chart.settings;

import static com.github.mikephil.charting.charts.Chart.PAINT_GRID_BACKGROUND;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.MeasurementLineChart;
import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.axis.MeasurementAxisValueFormatter;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.chart.settings.highlight.CustomMarker;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class LineChartSettings {

	private static final float GRANULARITY = HourMinutesAxisValueFormatter.getFractionOfFullHourFromSeconds(1);
	private final CustomMarker customMarker;
	private final HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter;
	private final MeasurementAxisValueFormatter measurementAxisValueFormatter;
	private LimitLinesBoundaries limitLinesBoundaries;
	private final Paint paintGridBg = new Paint();
	private final int primaryColor;
	private boolean drawXLabels = true;
	private boolean dragDecelerationEnabled = true;

	@Inject
	LineChartSettings(
		@ApplicationContext Context context, CustomMarker customMarker,
		HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter,
		MeasurementAxisValueFormatter measurementAxisValueFormatter
	) {
		primaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
		this.customMarker = customMarker;
		this.hourMinutesAxisValueFormatter = hourMinutesAxisValueFormatter;
		this.measurementAxisValueFormatter = measurementAxisValueFormatter;

		paintGridBg.setStyle(Paint.Style.FILL);
		paintGridBg.setColor(Color.WHITE);
	}

	public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
		this.limitLinesBoundaries = limitLinesBoundaries;
		measurementAxisValueFormatter.setLimitLinesBoundaries(limitLinesBoundaries);
	}

	public void setDrawXLabels(boolean drawXLabels) {
		this.drawXLabels = drawXLabels;
	}

	public void setDragDecelerationEnabled(boolean dragDecelerationEnabled) {
		this.dragDecelerationEnabled = dragDecelerationEnabled;
	}


	public void setChartSettingsFor(MeasurementLineChart lineChart) {

		lineChart.setDragDecelerationEnabled(dragDecelerationEnabled);
		lineChart.setPaint(paintGridBg, PAINT_GRID_BACKGROUND);
		lineChart.setAutoScaleMinMaxEnabled(false);
		lineChart.setDrawGridBackground(true);
		lineChart.setNoDataText("No data...");
		customMarker.setChartView(lineChart);
		lineChart.setMarker(customMarker);
		lineChart.setDrawBorders(false);
		lineChart.setMaxHighlightDistance(10000.0f);

		lineChart.resetZoom();
		lineChart.setMaxVisibleValueCount(10000);
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

	private void setupXAxis(MeasurementLineChart lineChart) {
		XAxis xAxis = lineChart.getXAxis();
		xAxis.setDrawAxisLine(false);
		xAxis.setDrawGridLines(false);
		xAxis.setDrawLabels(drawXLabels);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setGranularity(GRANULARITY);
		xAxis.setLabelCount(12, false);
		xAxis.setValueFormatter(hourMinutesAxisValueFormatter);
		xAxis.setAxisMinimum(HourMinutesAxisValueFormatter.MIN_X_SCALED_TIME);
		xAxis.setAxisMaximum(HourMinutesAxisValueFormatter.MAX_X_SCALED_TIME);
		xAxis.setLabelRotationAngle(-45.0f);
		xAxis.setTextColor(Color.BLACK);
	}

	private void setupDescriptions(MeasurementLineChart lineChart) {
		lineChart.getDescription().setEnabled(false);
		lineChart.getLegend().setEnabled(false);
/*
		lineChart.getDescription().setEnabled(true);
		String descriptionString = Profile.sharedInstance(lineChart.getContext())
			.getBloodSugarUnit();

		Description description = new Description();
		description.setText(descriptionString);
		description.setTextColor(Color.BLACK);
		description.setTextSize(12);
		description.setEnabled(true);
		description.setTextAlign(Paint.Align.LEFT);
		description.setPosition(10, 20);

		lineChart.setDescription(description);*/
	}

	private void setupYAxisRight(MeasurementLineChart lineChart) {
		YAxis yAxisRight = lineChart.getAxisRight();
		yAxisRight.setEnabled(false);
		yAxisRight.setDrawAxisLine(false);
		yAxisRight.setDrawGridLines(false);
	}

	private void setupYAxisLeft(MeasurementLineChart lineChart) {
		YAxis yAxisLeft = lineChart.getAxisLeft();
		yAxisLeft.setDrawAxisLine(false);
		yAxisLeft.setDrawGridLines(false);
//		yAxisLeft.setGridDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
//		yAxisLeft.setAxisLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		yAxisLeft.setGranularityEnabled(false);
		yAxisLeft.setValueFormatter(measurementAxisValueFormatter);
		yAxisLeft.setEnabled(true);
		yAxisLeft.setTextColor(primaryColor);

		yAxisLeft.removeAllLimitLines();

		if (yAxisLeft.getLimitLines().size() == 0) {
			limitLinesBoundaries.addLimitLinesInto(yAxisLeft);
		}
	}
}
