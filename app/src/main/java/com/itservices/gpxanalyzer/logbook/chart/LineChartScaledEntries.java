package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.logbook.Measurement;
import com.itservices.gpxanalyzer.logbook.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementEntry;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.itservices.gpxanalyzer.logbook.chart.settings.MeasurementBoundariesPreferences;

@Singleton
public class LineChartScaledEntries {
	private static List<Drawable> valuesIndicatorDrawableIconList = new ArrayList<>();
	private static List<Drawable> measurementDrawableIconList = new ArrayList<>();

	private StatisticResults measurementCurveStatisticResults = null;
	private StatisticResults measurementStatisticResults = null;
	private MeasurementBoundariesPreferences measurement;

	@Inject
	LineChartScaledEntries(MeasurementBoundariesPreferences measurement) {
		this.measurement = measurement;
		valuesIndicatorDrawableIconList = IconsUtil.generateDrawableIconForAreaList(10, 255);
		measurementDrawableIconList = IconsUtil.generateDrawableIconForAreaList(15, 255);
	}

	public ArrayList<Entry> createSingleMeasurementEntryList(
		Context context, StatisticResults statisticResults
	) {
		Vector<Measurement> measurementVector = statisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = measurementVector.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (measurementVector.isEmpty()) {
			return scaledEntries;
		}

		this.measurementStatisticResults = statisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = measurementVector.get(i).measurement;

			scaledEntries.add(
				SingleMeasurementEntry.create(context, measurementDrawableIconList, statisticResults, i,
						(float) value
				));
		}
		return scaledEntries;
	}

	public ArrayList<Entry> createCurveMeasurementEntryList(
		Context context, StatisticResults statisticResults
	) {
		Vector<Measurement> measurementVector = statisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = measurementVector.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (measurementVector.isEmpty()) {
			return scaledEntries;
		}

		this.measurementCurveStatisticResults = statisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = measurementVector.get(i).measurement;

			scaledEntries.add(CurveMeasurementEntry.create(context, valuesIndicatorDrawableIconList, statisticResults, i, (float) value));
		}
		return scaledEntries;
	}

	public void update(MeasurementCurveLineChart lineChart) {

		//combinedChart.setAutoScaleMinMaxEnabled(true);

		lineChart.setVisibleXRangeMinimum(0);

		double measurementMinStatisticsY = Double.MAX_VALUE;
		double measurementMaxStatisticsY = Double.MIN_VALUE;

		if (measurementStatisticResults != null) {
			measurementMinStatisticsY = measurementStatisticResults.getMinValue();
			measurementMaxStatisticsY = measurementStatisticResults.getMaxValue();
		}

		double measurementCurveMinStatisticsY = Double.MAX_VALUE;
		double measurementCurveMaxStatisticsY = Double.MIN_VALUE;

		if (measurementCurveStatisticResults != null) {
			measurementCurveMinStatisticsY = measurementCurveStatisticResults.getMinValue();
			measurementCurveMaxStatisticsY = measurementCurveStatisticResults.getMaxValue();
		}

		double minStatisticsY = Double.min(measurementCurveMinStatisticsY, measurementMinStatisticsY);
		double maxStatisticsY = Double.max(measurementCurveMaxStatisticsY, measurementMaxStatisticsY);

		double maxY = Double.max(maxStatisticsY, measurement.getMaxTargetMeasurement());
		double minY = Double.min(minStatisticsY, measurement.getMinTargetMeasurement());

		maxY = Double.max(maxY, measurement.getUpperMax());
		maxY = Double.max(maxY, measurement.getHyperMiddleValue());
		maxY = Double.max(maxY, measurement.getMaxYLimitValue());
		minY = Double.min(minY, measurement.getLowMeasurement());

		if (maxY > 1 && maxY > minY) {
			lineChart.setVisibleXRangeMaximum(lineChart.getXRange());

			double range = maxY - minY;
			double offset = range * 0.1f;

			YAxis leftAxis = lineChart.getAxisLeft();
			leftAxis.setAxisMinimum((float) (minY - offset));

			if (PrecisionUtil.isGreaterEqual((float) maxStatisticsY, (float) maxY, PrecisionUtil.NDIG_PREC_COMP)) {
				leftAxis.setAxisMaximum((float) (maxY + 2.0f * offset));
			} else {
				leftAxis.setAxisMaximum((float) maxY);
			}
		}
	}
}

