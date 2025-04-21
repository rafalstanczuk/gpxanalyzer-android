package com.itservices.gpxanalyzer.ui.components.chart.settings.axis;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.ui.components.chart.settings.background.LimitLinesBoundaries;

import java.util.List;

import javax.inject.Inject;

/**
 * Formatter for chart axis values (likely Y-axis) that implements both {@link IAxisValueFormatter}
 * and {@link IValueFormatter}.
 * Its intended purpose appears to be formatting numerical axis labels as integers,
 * but with logic to avoid drawing labels that are too close to existing {@link LimitLine} values
 * managed by a {@link LimitLinesBoundaries} instance, preventing visual clutter.
 * <p>
 * Note: The current implementation of {@link #getFormattedValue(float)} always returns an empty string,
 * effectively disabling the label drawing controlled by this formatter.
 */
public class AxisValueFormatter implements IAxisValueFormatter, IValueFormatter {

	/** Minimum distance a label value must be from any limit line value to be considered for drawing. */
	private static final int MIN_DISTANCE_VALUE_TO_DRAW = 5;
	/** Reference to the limit line boundaries, used to check for label proximity. */
	private LimitLinesBoundaries limitLinesBoundaries;

	/**
	 * Constructor used by Hilt for dependency injection.
	 */
	@Inject
	AxisValueFormatter() {}

	/**
	 * Formats the given numeric value for display as an axis label.
	 * Rounds the value to an integer and checks if it's allowed based on proximity to limit lines.
	 * Note: Currently returns an empty string, disabling label output.
	 *
	 * @param value The numeric value from the axis.
	 * @return The formatted label string (currently always "").
	 */
	public String getFormattedValue(float value) {

		String outStringLabel = "";

		if (isValueAllowedAsLabel(value)) {
			int intVal = Math.round(value);

			outStringLabel = String.valueOf(intVal);
		}

		return "";
	}

	/**
	 * Checks if a given value is allowed to be drawn as a label, based on its distance
	 * from existing limit lines.
	 *
	 * @param value The numeric value to check.
	 * @return {@code true} if the label is allowed, {@code false} otherwise.
	 */
	private boolean isValueAllowedAsLabel(float value) {
		int intVal = Math.round(value);

		return isAllowedDistance(intVal, limitLinesBoundaries.getLimitLineList());
	}

	/**
	 * Checks if a given integer value maintains a minimum distance from all limit line values.
	 *
	 * @param intVal        The integer value to check.
	 * @param limitLineList The list of existing limit lines.
	 * @return {@code true} if the value is sufficiently distant from all limit lines, {@code false} otherwise.
	 */
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

	/**
	 * Implementation of {@link IValueFormatter}. Delegates to {@link #getFormattedValue(float)}.
	 */
	@Override
	public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
		return getFormattedValue(value);
	}

	/**
	 * Implementation of {@link IAxisValueFormatter}. Delegates to {@link #getFormattedValue(float)}.
	 */
	@Override
	public String getFormattedValue(float value, AxisBase axis) {
		return getFormattedValue(value);
	}

	/**
	 * Sets the {@link LimitLinesBoundaries} instance used for checking label proximity.
	 *
	 * @param limitLinesBoundaries The limit lines boundaries manager.
	 */
	public void setLimitLinesBoundaries(LimitLinesBoundaries limitLinesBoundaries) {
		this.limitLinesBoundaries = limitLinesBoundaries;
	}
}
