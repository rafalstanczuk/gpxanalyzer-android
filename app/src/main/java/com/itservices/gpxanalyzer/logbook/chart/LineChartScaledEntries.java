package com.itservices.gpxanalyzer.logbook.chart;

import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;
import com.itservices.gpxanalyzer.logbook.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LineChartScaledEntries {
	private static final double DEFAULT_MIN_Y_VALUE = 0.0;
	private double minY = DEFAULT_MIN_Y_VALUE;
	private StatisticResults measurementCurveStatisticResults = null;
	private StatisticResults measurementSingleStatisticResults = null;

	@Inject
	LimitLinesBoundaries boundariesPreferences;

	@Inject
	LineChartScaledEntries() {
	}

	public void setMeasurementCurveStatisticResults(StatisticResults measurementCurveStatisticResults) {
		this.measurementCurveStatisticResults = measurementCurveStatisticResults;
	}

	public void setMeasurementSingleStatisticResults(StatisticResults measurementSingleStatisticResults) {
		this.measurementSingleStatisticResults = measurementSingleStatisticResults;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public void update(MeasurementCurveLineChart lineChart) {
		double r = measurementCurveStatisticResults.getMaxValue() - measurementCurveStatisticResults.getMinValue();
		double o = r * 0.1f;
		minY = (measurementCurveStatisticResults.getMinValue() - 2.0f * o);

		List<Double> valYStatisticsList =
				Arrays.asList(
						minY,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMinValue() : minY,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMaxValue() : minY,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMinValue() : minY,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMaxValue() : minY
				);

		List<Double> limitlinesValues =
				boundariesPreferences.getLimitLineList()
						.stream()
						.map(limitLine -> (double)limitLine.getLimit())
						.collect(Collectors.toList());

		Vector<Double> valYList =
				new Vector<>(Arrays.asList(
						minY,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMinValue() : minY,
						measurementSingleStatisticResults !=null ? measurementSingleStatisticResults.getMaxValue() : minY,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMinValue() : minY,
						measurementCurveStatisticResults!=null ? measurementCurveStatisticResults.getMaxValue() : minY
				));
		valYList.addAll(limitlinesValues);

		//combinedChart.setAutoScaleMinMaxEnabled(true);

		lineChart.setVisibleXRangeMinimum(0);

		double minY = valYList.stream().min(Comparator.naturalOrder()).get();
		double maxY = valYList.stream().max(Comparator.naturalOrder()).get();

		double maxStatisticsY = valYStatisticsList.stream().max(Comparator.naturalOrder()).get();

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

