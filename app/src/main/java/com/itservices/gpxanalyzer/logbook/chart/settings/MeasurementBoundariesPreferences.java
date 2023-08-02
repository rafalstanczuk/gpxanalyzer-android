package com.itservices.gpxanalyzer.logbook.chart.settings;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class MeasurementBoundariesPreferences {
	public static final int VALUE_0_MEASUREMENT = 54;
	public static final int VALUE_1_MEASUREMENT = 70;
	public static final int VALUE_2_MEASUREMENT = 180;
	public static final int VALUE_3_MEASUREMENT = 250;
	public static final int VALUE_4_MEASUREMENT = 300;

	private int limitValue5 = (int) (VALUE_4_MEASUREMENT*1.2f);
	private int limitValue4 = VALUE_4_MEASUREMENT;
	private int limitValue3 = VALUE_3_MEASUREMENT;
	private int limitValue2 = VALUE_2_MEASUREMENT;
	private int limitValue1 = VALUE_1_MEASUREMENT;
	private int limitValue0 = VALUE_0_MEASUREMENT;

	private LimitLine line5 = new LimitLine(limitValue5);
	private LimitLine line4 = new LimitLine(limitValue4);
	private LimitLine line3 = new LimitLine(limitValue3);
	private LimitLine line2 = new LimitLine(limitValue2);
	private LimitLine line1 = new LimitLine(limitValue1);
	private LimitLine line0 = new LimitLine(limitValue0);

	private final int primaryColor;
	private static final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

	@Inject
	public MeasurementBoundariesPreferences(@ApplicationContext Context context) {
		this.primaryColor = context.getResources().getColor(R.color.colorPrimary);
	}

	public void initLimitLines() {
		line0 = createLimitLine(limitValue0, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		line1 = createLimitLine(limitValue1, LimitLine.LimitLabelPosition.LEFT_TOP);
		line2 = createLimitLine(limitValue2, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		line3 = createLimitLine(limitValue3, LimitLine.LimitLabelPosition.LEFT_TOP);
		line4 = createLimitLine(limitValue4, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
		line5 = createLimitLine(limitValue5, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
	}

	private static LimitLine createLimitLine(int limitValue, LimitLine.LimitLabelPosition labelPosition) {
		LimitLine line = new LimitLine(limitValue, String.valueOf(limitValue));
			line.setLabelPosition(labelPosition);
			line.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
			line.setLineWidth(0.4f);
			line.setTextSize(12f);
			line.enableDashedLine(10f, 5f, 0f);
			line.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		return line;
	}

	public int getLimitValue5() {
		return limitValue5;
	}

	public int getLimitValue4() {
		return limitValue4;
	}

	public int getLimitValue3() {
		return limitValue3;
	}

	public int getLimitValue1() {
		return limitValue1;
	}

	public int getLimitValue2() {
		return limitValue2;
	}

	public int getLimitValue0() {
		return limitValue0;
	}

	public LimitLine getLine5() {
		return line5;
	}

	public LimitLine getLine4() {
		return line4;
	}

	public LimitLine getLine3() {
		return line3;
	}

	public LimitLine getLine1() {
		return line1;
	}

	public LimitLine getLine2() {
		return line2;
	}

	public LimitLine getLine0() {
		return line0;
	}
}
