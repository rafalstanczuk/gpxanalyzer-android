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
public class GlucoseBoundariesPreferences {
	public static final float LIGHT_HYPOGLYCEMIA_TREND_DEFAULT = 70.0f;
	public static final float HEAVY_HYPOGLYCEMIA_TREND_DEFAULT = 54.0f;

	public static final int GLUCOSE_TARGET_DEFAULT_MIN = 70;
	public static final int GLUCOSE_TARGET_DEFAULT_MAX = 180;
	public static final int GLUCOSE_UPPER_MAX = 250;
	public static final int GLUCOSE_HYPER_MIDDLE = computeHyperMiddle(GLUCOSE_UPPER_MAX);
	public static final int DEFAULT_BOLUS_GLU_TARGET_VALUE = 120;

	public static boolean hasHypoRate(float glucoseValue) {
		return glucoseValue < LIGHT_HYPOGLYCEMIA_TREND_DEFAULT;
	}

	public static int computeHyperMiddle(int hyper) {
		return ( (int)Math.ceil(hyper/50.0) + 1 )*50;
	}
	private final Context context;
	private int maxYLimitValue = computeMaxYLimit(GLUCOSE_UPPER_MAX);
	private int hyperMiddleValue = GLUCOSE_HYPER_MIDDLE;
	private int upperMax = 0;
	private int minTargetGlucose = 0;
	private int maxTargetGlucose = 0;
	private int hypoglycemiaGlucose = 0;

	private LimitLine lineMaxValue = new LimitLine(maxYLimitValue);
	private LimitLine lineHyperMiddle = new LimitLine(hyperMiddleValue);
	private LimitLine lineUpperMax = new LimitLine(upperMax);
	private LimitLine lineMinTargetGlucose = new LimitLine(minTargetGlucose);
	private LimitLine lineMaxTargetGlucose = new LimitLine(maxTargetGlucose);
	private LimitLine lineHypoglycemiaGlucose = new LimitLine(hypoglycemiaGlucose);
	private final int primaryColor;
	private final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

	@Inject
	public GlucoseBoundariesPreferences(@ApplicationContext Context context) {
		this.context = context;
		this.primaryColor = context.getResources().getColor(R.color.colorPrimary);
		initValues(context);
	}

	public void initValues(Context context) {

		maxTargetGlucose = GLUCOSE_TARGET_DEFAULT_MAX;

		upperMax = GLUCOSE_UPPER_MAX;

		maxYLimitValue = computeMaxYLimit(upperMax);
		hyperMiddleValue = computeHyperMiddle(upperMax);

		minTargetGlucose = GLUCOSE_TARGET_DEFAULT_MIN;
		hypoglycemiaGlucose = (int) HEAVY_HYPOGLYCEMIA_TREND_DEFAULT;
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

		lineMinTargetGlucose = new LimitLine(minTargetGlucose, String.valueOf(minTargetGlucose));
		lineMinTargetGlucose.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
		lineMinTargetGlucose.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineMinTargetGlucose.setLineWidth(0.4f);
		lineMinTargetGlucose.enableDashedLine(10f, 5f, 0f);
		lineMinTargetGlucose.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineMinTargetGlucose.setTextSize(12f);

		lineMaxTargetGlucose = new LimitLine(maxTargetGlucose, String.valueOf(maxTargetGlucose));
		lineMaxTargetGlucose.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		lineMaxTargetGlucose.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineMaxTargetGlucose.setLineWidth(0.4f);
		lineMaxTargetGlucose.enableDashedLine(10f, 5f, 0f);
		lineMaxTargetGlucose.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineMaxTargetGlucose.setTextSize(12f);

		lineHypoglycemiaGlucose = new LimitLine(
				hypoglycemiaGlucose, String.valueOf(hypoglycemiaGlucose));
		lineHypoglycemiaGlucose.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		lineHypoglycemiaGlucose.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
		lineHypoglycemiaGlucose.setLineWidth(0.4f);
		lineHypoglycemiaGlucose.enableDashedLine(10f, 5f, 0f);
		lineHypoglycemiaGlucose.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		lineHypoglycemiaGlucose.setTextSize(12f);
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

	public int getMinTargetGlucose() {
		return minTargetGlucose;
	}

	public int getMaxTargetGlucose() {
		return maxTargetGlucose;
	}

	public int getHypoglycemiaGlucose() {
		return hypoglycemiaGlucose;
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

	public LimitLine getLineMinTargetGlucose() {
		return lineMinTargetGlucose;
	}

	public LimitLine getLineMaxTargetGlucose() {
		return lineMaxTargetGlucose;
	}

	public LimitLine getLineHypoglycemiaGlucose() {
		return lineHypoglycemiaGlucose;
	}
}
