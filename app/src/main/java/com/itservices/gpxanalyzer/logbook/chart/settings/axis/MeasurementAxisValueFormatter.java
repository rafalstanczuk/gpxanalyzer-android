package com.itservices.gpxanalyzer.logbook.chart.settings.axis;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.logbook.chart.settings.MeasurementBoundariesPreferences;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MeasurementAxisValueFormatter implements IAxisValueFormatter, IValueFormatter {

	private static final int MIN_DISTANCE_VALUE_TO_DRAW = 5;

	private final MeasurementBoundariesPreferences measurementBoundariesPreferences;

	@Inject
	MeasurementAxisValueFormatter(MeasurementBoundariesPreferences measurementBoundariesPreferences) {
		this.measurementBoundariesPreferences = measurementBoundariesPreferences;
	}

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

		List<Integer> allowedValues = Arrays.asList(
			measurementBoundariesPreferences.getUpperMax(),
			measurementBoundariesPreferences.getMinTargetMeasurement(),
			measurementBoundariesPreferences.getMaxTargetMeasurement(),
			measurementBoundariesPreferences.getLowMeasurement()
		);

		return isAllowedDistance(intVal, allowedValues);
	}

	private boolean isAllowedDistance(
		int intVal, List<Integer> allowedValues
	) {

		for(int boundValToCheck: allowedValues) {
			if (Math.abs(intVal - boundValToCheck) < MIN_DISTANCE_VALUE_TO_DRAW) {
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
}
