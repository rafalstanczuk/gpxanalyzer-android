package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.logbook.Measurement;
import com.itservices.gpxanalyzer.logbook.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.itservices.gpxanalyzer.logbook.chart.settings.MeasurementBoundariesPreferences;

@Singleton
public class LineChartScaledEntries {
	private static List<Drawable>  csgmDrawableIconList = new ArrayList<>();
	private static List<Drawable> measurementDrawableIconList = new ArrayList<>();

	private StatisticResults csgmStatisticResults = null;
	private StatisticResults measurementStatisticResults = null;
	private MeasurementBoundariesPreferences measurement;

	@Inject
	LineChartScaledEntries(MeasurementBoundariesPreferences measurement) {
		this.measurement = measurement;
		csgmDrawableIconList = IconsUtil.generateDrawableIconForAreaList(10, 255);
		measurementDrawableIconList = IconsUtil.generateDrawableIconForAreaList(15, 255);
	}

	public ArrayList<Entry> createMeasurementEntryList(
		Context context, StatisticResults measurementStatisticResults
	) {
		Vector<Measurement> measurementValues = measurementStatisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = measurementValues.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (measurementValues.isEmpty()) {
			return scaledEntries;
		}

		this.measurementStatisticResults = measurementStatisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = measurementValues.get(i).measurement;

			scaledEntries.add(
				SingleMeasurementMeasurementEntry.create(context, measurementDrawableIconList, measurementStatisticResults, i,
						(float) value
				));
		}
		return scaledEntries;
	}

	public ArrayList<Entry> createMeasurementCurveEntryList(
		Context context, StatisticResults csgmStatisticResults
	) {
		Vector<Measurement> csgmValues = csgmStatisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = csgmValues.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (csgmValues.isEmpty()) {
			return scaledEntries;
		}

		this.csgmStatisticResults = csgmStatisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = csgmValues.get(i).measurement;

			scaledEntries.add(CurveMeasurementEntry.create(context, csgmDrawableIconList, csgmStatisticResults, i, (float) value));
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

		double csgmMinStatisticsY = Double.MAX_VALUE;
		double csgmMaxStatisticsY = Double.MIN_VALUE;

		if (csgmStatisticResults != null) {
			csgmMinStatisticsY = csgmStatisticResults.getMinValue();
			csgmMaxStatisticsY = csgmStatisticResults.getMaxValue();
		}

		double minStatisticsY = Double.min(csgmMinStatisticsY, measurementMinStatisticsY);
		double maxStatisticsY = Double.max(csgmMaxStatisticsY, measurementMaxStatisticsY);

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

			if (IconsUtil.isGreaterEqual((float) maxStatisticsY, (float) maxY, IconsUtil.NDIG_PREC_COMP)) {
				leftAxis.setAxisMaximum((float) (maxY + 2.0f * offset));
			} else {
				leftAxis.setAxisMaximum((float) maxY);
			}
		}
	}
}

