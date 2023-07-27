package com.itservices.gpxanalyzer.logbook.chart.settings;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.logbook.chart.entry.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class MeasurementBoundariesPreferences {
	public static final float LOW_VALUE_MEASUREMENT_DEFAULT = 54.0f;

	public static final int MEASUREMENT_TARGET_DEFAULT_MIN = 70;
	public static final int MEASUREMENT_TARGET_DEFAULT_MAX = 180;
	public static final int MEASUREMENT_UPPER_MAX = 250;
	public static final int MEASUREMENT_HYPER_MIDDLE = computeHyperMiddle(MEASUREMENT_UPPER_MAX);

	public static int computeHyperMiddle(int hyper) {
		return ( (int)Math.ceil(hyper/50.0) + 1 )*50;
	}
	private final Context context;
	private int maxYLimitValue = computeMaxYLimit(MEASUREMENT_UPPER_MAX);
	private int hyperMiddleValue = MEASUREMENT_HYPER_MIDDLE;
	private int upperMax = 0;
	private int minTargetMeasurement = 0;
	private int maxTargetMeasurement = 0;
	private int lowMeasurement = 0;

	private LimitLine lineMaxValue = new LimitLine(maxYLimitValue);
	private LimitLine lineHyperMiddle = new LimitLine(hyperMiddleValue);
	private LimitLine lineUpperMax = new LimitLine(upperMax);
	private LimitLine lineMinTargetMeasurement = new LimitLine(minTargetMeasurement);
	private LimitLine lineMaxTargetMeasurement = new LimitLine(maxTargetMeasurement);
	private LimitLine lineLowMeasurement = new LimitLine(lowMeasurement);
	private final int primaryColor;
	private final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

	@Inject
	public MeasurementBoundariesPreferences(@ApplicationContext Context context) {
		this.context = context;
		this.primaryColor = context.getResources().getColor(R.color.colorPrimary);
		initValues(context);
	}

	public void initValues(Context context) {

		maxTargetMeasurement = MEASUREMENT_TARGET_DEFAULT_MAX;

		upperMax = MEASUREMENT_UPPER_MAX;

		maxYLimitValue = computeMaxYLimit(upperMax);
		hyperMiddleValue = computeHyperMiddle(upperMax);

		minTargetMeasurement = MEASUREMENT_TARGET_DEFAULT_MIN;
		lowMeasurement = (int) LOW_VALUE_MEASUREMENT_DEFAULT;
	}

	private static int computeMaxYLimit(int hyper) {
		return ( (int)Math.ceil(hyper/50.0) + 2 )*50;
	}

	public void initLimitLines() {
		if (maxYLimitValue > 0) {
			lineMaxValue = new LimitLine(maxYLimitValue, String.valueOf(maxYLimitValue));
			lineMaxValue.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
			lineMaxValue.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
			lineMaxValue.setLineWidth(0.4f);

			lineMaxValue.setTextSize(12f);
			lineMaxValue.enableDashedLine(10f, 5f, 0f);
			lineMaxValue.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		} else {
			lineMaxValue = null;
		}

		if (hyperMiddleValue > 0) {
			lineHyperMiddle = new LimitLine(hyperMiddleValue, String.valueOf(hyperMiddleValue));
			lineHyperMiddle.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
			lineHyperMiddle.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
			lineHyperMiddle.setLineWidth(0.4f);

			lineHyperMiddle.setTextSize(12f);
			lineHyperMiddle.enableDashedLine(10f, 5f, 0f);
			lineHyperMiddle.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		} else {
			lineHyperMiddle = null;
		}

		if (upperMax > 0) {
			lineUpperMax = new LimitLine(upperMax, String.valueOf(upperMax));
			lineUpperMax.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
			lineUpperMax.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
			lineUpperMax.setLineWidth(0.4f);

			lineUpperMax.setTextSize(12f);
			lineUpperMax.enableDashedLine(10f, 5f, 0f);
			lineUpperMax.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));

		} else {
			lineUpperMax = null;
		}

		lineMinTargetMeasurement = new LimitLine(minTargetMeasurement, String.valueOf(minTargetMeasurement));
		lineMinTargetMeasurement.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
		lineMinTargetMeasurement.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineMinTargetMeasurement.setLineWidth(0.4f);
		lineMinTargetMeasurement.enableDashedLine(10f, 5f, 0f);
		lineMinTargetMeasurement.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineMinTargetMeasurement.setTextSize(12f);

		lineMaxTargetMeasurement = new LimitLine(maxTargetMeasurement, String.valueOf(maxTargetMeasurement));
		lineMaxTargetMeasurement.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		lineMaxTargetMeasurement.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineMaxTargetMeasurement.setLineWidth(0.4f);
		lineMaxTargetMeasurement.enableDashedLine(10f, 5f, 0f);
		lineMaxTargetMeasurement.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineMaxTargetMeasurement.setTextSize(12f);

		lineLowMeasurement = new LimitLine(
				lowMeasurement, String.valueOf(lowMeasurement));
		lineLowMeasurement.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		lineLowMeasurement.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineLowMeasurement.setLineWidth(0.4f);
		lineLowMeasurement.enableDashedLine(10f, 5f, 0f);
		lineLowMeasurement.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineLowMeasurement.setTextSize(12f);
	}

	public int getMaxYLimitValue() {
		return maxYLimitValue;
	}

	public int getHyperMiddleValue() {
		return hyperMiddleValue;
	}

	public int getUpperMax() {
		return upperMax;
	}

	public int getMinTargetMeasurement() {
		return minTargetMeasurement;
	}

	public int getMaxTargetMeasurement() {
		return maxTargetMeasurement;
	}

	public int getLowMeasurement() {
		return lowMeasurement;
	}

	public LimitLine getLineMaxValue() {
		return lineMaxValue;
	}

	public LimitLine getLineHyperMiddle() {
		return lineHyperMiddle;
	}

	public LimitLine getLineUpperMax() {
		return lineUpperMax;
	}

	public LimitLine getLineMinTargetMeasurement() {
		return lineMinTargetMeasurement;
	}

	public LimitLine getLineMaxTargetMeasurement() {
		return lineMaxTargetMeasurement;
	}

	public LimitLine getLineLowMeasurement() {
		return lineLowMeasurement;
	}
}
