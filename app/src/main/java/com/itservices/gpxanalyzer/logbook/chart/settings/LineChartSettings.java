package com.itservices.gpxanalyzer.logbook.chart.settings;

import static com.github.mikephil.charting.charts.Chart.PAINT_GRID_BACKGROUND;

import static com.itservices.gpxanalyzer.logbook.chart.settings.HourMinutesAxisValueFormatter.MAX_X_SCALED_TIME;
import static com.itservices.gpxanalyzer.logbook.chart.settings.HourMinutesAxisValueFormatter.MIN_X_SCALED_TIME;
import static com.itservices.gpxanalyzer.logbook.chart.settings.HourMinutesAxisValueFormatter.getFractionOfFullHourFromMinutes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.logbook.chart.CSGMLineChart;


import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class LineChartSettings {

	private static final float GRANULARITY = getFractionOfFullHourFromMinutes(1);
	private final CustomMarker customMarker;
	private final HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter;
	private final GlucoseAxisValueFormatter glucoseAxisValueFormatter;
	private final GlucoseBoundariesPreferences glucoseBoundariesPreferences;
	private final Paint paintGridBg = new Paint();
	private final int primaryColor;

	@Inject
	LineChartSettings(
		@ApplicationContext Context context, CustomMarker customMarker,
		HourMinutesAxisValueFormatter hourMinutesAxisValueFormatter,
		GlucoseAxisValueFormatter glucoseAxisValueFormatter,
		GlucoseBoundariesPreferences glucoseBoundariesPreferences
	) {
		this.customMarker = customMarker;
		this.hourMinutesAxisValueFormatter = hourMinutesAxisValueFormatter;
		primaryColor = context.getResources().getColor(R.color.colorPrimary);
		this.glucoseBoundariesPreferences = glucoseBoundariesPreferences;
		this.glucoseAxisValueFormatter = glucoseAxisValueFormatter;

		paintGridBg.setStyle(Paint.Style.FILL);
		paintGridBg.setColor(Color.WHITE);

		glucoseBoundariesPreferences.initValues(context);
		glucoseBoundariesPreferences.initLimitLines();
	}

	public void setChartSettingsFor(CSGMLineChart lineChart) {
		glucoseBoundariesPreferences.initValues(lineChart.getContext());
		glucoseBoundariesPreferences.initLimitLines();

		lineChart.setPaint(paintGridBg, PAINT_GRID_BACKGROUND);
		lineChart.setAutoScaleMinMaxEnabled(false);
		lineChart.setDrawGridBackground(true);
		lineChart.setNoDataText("No data...");
		customMarker.setChartView(lineChart);
		lineChart.setMarker(customMarker);
		lineChart.setDrawBorders(false);
		lineChart.setMaxHighlightDistance(10000.0f);

		lineChart.resetZoom();
		lineChart.setMaxVisibleValueCount(2000);
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

	private void setupXAxis(CSGMLineChart lineChart) {
		XAxis xAxis = lineChart.getXAxis();
		xAxis.setDrawAxisLine(false);
		xAxis.setDrawGridLines(false);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setGranularity(GRANULARITY);
		xAxis.setLabelCount(12, false);
		xAxis.setValueFormatter(hourMinutesAxisValueFormatter);
		xAxis.setAxisMinimum(MIN_X_SCALED_TIME);
		xAxis.setAxisMaximum(MAX_X_SCALED_TIME);
		xAxis.setLabelRotationAngle(-45.0f);
		xAxis.setTextColor(Color.BLACK);
	}

	private void setupDescriptions(CSGMLineChart lineChart) {
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

	private void setupYAxisRight(CSGMLineChart lineChart) {
		YAxis yAxisRight = lineChart.getAxisRight();
		yAxisRight.setEnabled(false);
		yAxisRight.setDrawAxisLine(false);
		yAxisRight.setDrawGridLines(false);
	}

	private void setupYAxisLeft(CSGMLineChart lineChart) {
		YAxis yAxisLeft = lineChart.getAxisLeft();
		yAxisLeft.setDrawAxisLine(false);
		yAxisLeft.setDrawGridLines(false);
//		yAxisLeft.setGridDashedLine(new DashPathEffect(new float[]{10f, 5f}, 0f));
//		yAxisLeft.setAxisLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		yAxisLeft.setGranularityEnabled(false);
		yAxisLeft.setValueFormatter(glucoseAxisValueFormatter);
		yAxisLeft.setEnabled(true);
		yAxisLeft.setTextColor(primaryColor);

		yAxisLeft.removeAllLimitLines();

		if (yAxisLeft.getLimitLines().size() == 0) {
			if (glucoseBoundariesPreferences.getLineMaxValue() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineMaxValue());
			}
			/*if (glucoseBoundariesPreferences.getLineHyperMiddle() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineHyperMiddle());
			}*/
			if (glucoseBoundariesPreferences.getLineUpperMax() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineUpperMax());
			}
			if (glucoseBoundariesPreferences.getLineMaxTargetGlucose() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineMaxTargetGlucose());
			}
			if (glucoseBoundariesPreferences.getLineMinTargetGlucose() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineMinTargetGlucose());
			}
			if (glucoseBoundariesPreferences.getLineHypoglycemiaGlucose() != null) {
				yAxisLeft.addLimitLine(glucoseBoundariesPreferences.getLineHypoglycemiaGlucose());
			}
		}
	}
}
