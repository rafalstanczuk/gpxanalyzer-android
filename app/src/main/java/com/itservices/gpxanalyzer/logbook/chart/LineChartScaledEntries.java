package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.logbook.Measurement;
import com.itservices.gpxanalyzer.logbook.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementEntry;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.itservices.gpxanalyzer.logbook.chart.settings.MeasurementBoundariesPreferences;

@Singleton
public class LineChartScaledEntries {
	private static List<Drawable> curveValuesIndicatorDrawableIconList = new ArrayList<>();
	private static List<Drawable> singleValuesIndicatorDrawableIconList = new ArrayList<>();

	private StatisticResults measurementCurveStatisticResults = null;
	private StatisticResults measurementSingleStatisticResults = null;

	private final PaletteColorDeterminer paletteColorDeterminer;
	private MeasurementBoundariesPreferences boundariesPreferences;
	private double maxY = 100.0;
	private double minY = 0.0;



	@Inject
	LineChartScaledEntries(PaletteColorDeterminer paletteColorDeterminer, MeasurementBoundariesPreferences boundariesPreferences) {
		this.paletteColorDeterminer = paletteColorDeterminer;
		this.boundariesPreferences = boundariesPreferences;
		curveValuesIndicatorDrawableIconList = IconsUtil.generateDrawableIconForAreaList(10, 255);
		singleValuesIndicatorDrawableIconList = IconsUtil.generateDrawableIconForAreaList(15, 255);
	}

	public ArrayList<Entry> createSingleMeasurementEntryList(
		StatisticResults statisticResults
	) {
		Vector<Measurement> measurementVector = statisticResults.getMeasurements();

		int startXIndex = 0;
		int endXIndex = measurementVector.size();

		ArrayList<Entry> scaledEntries = new ArrayList<>();
		if (measurementVector.isEmpty()) {
			return scaledEntries;
		}

		this.measurementSingleStatisticResults = statisticResults;

		for (int i = startXIndex; i < endXIndex; i++) {
			double value = measurementVector.get(i).measurement;

			scaledEntries.add(
				SingleMeasurementEntry.create(singleValuesIndicatorDrawableIconList, statisticResults, i,
						(float) value
				));
		}
		return scaledEntries;
	}

	public ArrayList<Entry> createCurveMeasurementEntryList(
		StatisticResults statisticResults
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

			scaledEntries.add(CurveMeasurementEntry.create(paletteColorDeterminer, curveValuesIndicatorDrawableIconList, statisticResults, i, (float) value));
		}
		return scaledEntries;
	}

	public void update(MeasurementCurveLineChart lineChart) {

		List<Double> valYStatisticsList =
				Arrays.asList(
						0.0,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMinValue() : 0.0,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMaxValue() : 0.0,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMinValue() : 0.0,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMaxValue() : 0.0
				);

		List<Double> valYList =
				Arrays.asList(
						0.0,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMinValue() : 0.0,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMaxValue() : 0.0,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMinValue() : 0.0,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMaxValue() : 0.0,
						(double) boundariesPreferences.getLimitValue0(),
						(double) boundariesPreferences.getLimitValue1(),
						(double) boundariesPreferences.getLimitValue2(),
						(double) boundariesPreferences.getLimitValue3(),
						(double) boundariesPreferences.getLimitValue4(),
						(double) boundariesPreferences.getLimitValue5()
				);

		//combinedChart.setAutoScaleMinMaxEnabled(true);

		lineChart.setVisibleXRangeMinimum(0);

		double minY = valYList.stream().min(Comparator.naturalOrder()).get();
		double maxY = valYList.stream().max(Comparator.naturalOrder()).get();

		double maxStatisticsY = valYStatisticsList.stream().max(Comparator.naturalOrder()).get();

		if (maxY > 1 && maxY > minY) {
			this.maxY = maxY;
			this.minY = minY;

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

