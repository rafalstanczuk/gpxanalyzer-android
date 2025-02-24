package com.itservices.gpxanalyzer.chart;

import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.data.statistics.StatisticResults;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class LineChartScaler {
	private static final double DEFAULT_MIN_Y_VALUE = 0.0;
	private double minY = DEFAULT_MIN_Y_VALUE;
	private StatisticResults statisticResults = null;

	private LimitLinesBoundaries limitLinesBoundaries;

	@Inject
	LineChartScaler() {
	}

	public void setStatisticResults(StatisticResults statisticResults) {
		this.statisticResults = statisticResults;
	}

	public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
		this.limitLinesBoundaries = limitLinesBoundaries;
	}

	public void scale(DataEntityLineChart lineChart) {
		double r = statisticResults.getMaxValue() - statisticResults.getMinValue();
		double o = r * 0.1f;
		minY = (statisticResults.getMinValue() - o);

		List<Double> valYStatisticsList =
				Arrays.asList(
						minY,
						statisticResults !=null ? statisticResults.getMinValue() : minY,
						statisticResults !=null ? statisticResults.getMaxValue() : minY
				);

		List<Double> limitLinesValues =
				limitLinesBoundaries.getLimitLineList()
						.stream()
						.map(limitLine -> (double)limitLine.getLimit())
						.collect(Collectors.toList());

		Vector<Double> valYList =
				new Vector<>(Arrays.asList(
						minY,
						statisticResults !=null ? statisticResults.getMinValue() : minY,
						statisticResults !=null ? statisticResults.getMaxValue() : minY
				));
		valYList.addAll(limitLinesValues);

		//combinedChart.setAutoScaleMinMaxEnabled(true);

		//lineChart.setVisibleXRangeMinimum(0);

		double minY = valYList.stream().min(Comparator.naturalOrder()).get();
		double maxY = valYList.stream().max(Comparator.naturalOrder()).get();

		double maxStatisticsY = valYStatisticsList.stream().max(Comparator.naturalOrder()).get();

		if (maxY > 1 && maxY > minY) {
			//lineChart.setVisibleXRangeMaximum(lineChart.getXRange());

			double range = maxY - minY;
			double offset = range * 0.1f;

			YAxis leftAxis = lineChart.getAxisLeft();
			leftAxis.setAxisMinimum((float) (minY - offset));

			if (PrecisionUtil.isGreaterEqual((float) maxStatisticsY, (float) maxY, PrecisionUtil.NDIG_PREC_COMP)) {
				leftAxis.setAxisMaximum((float) (maxStatisticsY + 2.0f * offset));
			} else {
				leftAxis.setAxisMaximum((float) (maxY + 2.0f * offset));
			}
		}
	}
}

