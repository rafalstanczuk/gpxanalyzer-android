package com.itservices.gpxanalyzer.chart.settings.axis;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;

import java.util.List;

import javax.inject.Inject;

public class AxisValueFormatter implements IAxisValueFormatter, IValueFormatter {

	private static final int MIN_DISTANCE_VALUE_TO_DRAW = 5;
	private LimitLinesBoundaries limitLinesBoundaries;

	@Inject
	AxisValueFormatter() {}

	public String getFormattedValue(float value) {

		String outStringLabel = "";

		if (isValueAllowedAsLabel(value)) {
			int intVal = Math.round(value);

			outStringLabel = String.valueOf(intVal);
		}

		return "";
	}

	private boolean isValueAllowedAsLabel(float value) {
		int intVal = Math.round(value);

		return isAllowedDistance(intVal, limitLinesBoundaries.getLimitLineList());
	}

	private boolean isAllowedDistance(
			int intVal, List<LimitLine> limitLineList
	) {

		for(LimitLine limitLine: limitLineList) {
			if (Math.abs(intVal - limitLine.getLimit()) < MIN_DISTANCE_VALUE_TO_DRAW) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
		return getFormattedValue(value);
	}

	@Override
	public String getFormattedValue(float value, AxisBase axis) {
		return getFormattedValue(value);
	}

	public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
		this.limitLinesBoundaries = limitLinesBoundaries;
	}
}
