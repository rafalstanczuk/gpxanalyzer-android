package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.logbook.Measurement;
import com.itservices.gpxanalyzer.logbook.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.entry.CSGMEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.GlucoseEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.itservices.gpxanalyzer.logbook.chart.settings.GlucoseBoundariesPreferences;

@Singleton
public class LineChartScaledEntries {
	private static List<Drawable>  csgmDrawableIconList = new ArrayList<>();
	private static List<Drawable> glucoseDrawableIconList = new ArrayList<>();

	private StatisticResults csgmStatisticResults = null;
	private StatisticResults glucoseStatisticResults = null;
	private GlucoseBoundariesPreferences glucose;

	@Inject
	LineChartScaledEntries(GlucoseBoundariesPreferences glucose) {
		this.glucose = glucose;
		csgmDrawableIconList = IconsUtil.generateDrawableIconForAreaList(10, 255);
		glucoseDrawableIconList = IconsUtil.generateDrawableIconForAreaList(15, 255);
	}

	public ArrayList<Entry> createGlucoseEntryList(
		Context context, StatisticResults glucoseStatisticResults
	) {
		Vector<Measurement> glucoseValues = glucoseStatisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = glucoseValues.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (glucoseValues.isEmpty()) {
			return scaledEntries;
		}

		this.glucoseStatisticResults = glucoseStatisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = glucoseValues.get(i).measurement;

			scaledEntries.add(
				GlucoseEntry.create(context, glucoseDrawableIconList, glucoseStatisticResults, i,
						(float) value
				));
		}
		return scaledEntries;
	}

	public ArrayList<Entry> createCSGMEntryList(
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

			scaledEntries.add(CSGMEntry.create(context, csgmDrawableIconList, csgmStatisticResults, i, (float) value));
		}
		return scaledEntries;
	}

	public void update(CSGMLineChart lineChart) {

		//combinedChart.setAutoScaleMinMaxEnabled(true);

		lineChart.setVisibleXRangeMinimum(0);

		double glucoseMinStatisticsY = Double.MAX_VALUE;
		double glucoseMaxStatisticsY = Double.MIN_VALUE;

		if (glucoseStatisticResults != null) {
			glucoseMinStatisticsY = glucoseStatisticResults.getMinValue();
			glucoseMaxStatisticsY = glucoseStatisticResults.getMaxValue();
		}

		double csgmMinStatisticsY = Double.MAX_VALUE;
		double csgmMaxStatisticsY = Double.MIN_VALUE;

		if (csgmStatisticResults != null) {
			csgmMinStatisticsY = csgmStatisticResults.getMinValue();
			csgmMaxStatisticsY = csgmStatisticResults.getMaxValue();
		}

		double minStatisticsY = Double.min(csgmMinStatisticsY, glucoseMinStatisticsY);
		double maxStatisticsY = Double.max(csgmMaxStatisticsY, glucoseMaxStatisticsY);

		double maxY = Double.max(maxStatisticsY, glucose.getMaxTargetGlucose());
		double minY = Double.min(minStatisticsY, glucose.getMinTargetGlucose());

		maxY = Double.max(maxY, glucose.getUpperMax());
		maxY = Double.max(maxY, glucose.getHyperMiddleValue());
		maxY = Double.max(maxY, glucose.getMaxYLimitValue());
		minY = Double.min(minY, glucose.getHypoglycemiaGlucose());

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

