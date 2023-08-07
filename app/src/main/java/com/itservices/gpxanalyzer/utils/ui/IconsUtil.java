package com.itservices.gpxanalyzer.utils.ui;

import static com.itservices.gpxanalyzer.utils.ui.ColorUtil.setAlphaInIntColor;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.MEASUREMENT_5RANGES_COLOR_LIST;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import java.util.ArrayList;
import java.util.List;

public class IconsUtil {

	public static List<Drawable> generateDrawableIconForAreaList(int size, int alpha) {
		List<Drawable> measurementDrawableIconList = new ArrayList<>();

		for (int color : MEASUREMENT_5RANGES_COLOR_LIST) {

			int colorWithAlpha = setAlphaInIntColor(color, alpha);

			measurementDrawableIconList.add(
				getDrawableIconForAreaColorId(colorWithAlpha, size)
			);
		}

		addDefaultIcon(size, alpha, measurementDrawableIconList);

		return measurementDrawableIconList;
	}

	private static void addDefaultIcon(int size, int alpha, List<Drawable> measurementDrawableIconList) {
		int defaultWithAlpha = setAlphaInIntColor(Color.BLACK, alpha);

		measurementDrawableIconList.add(
			getDrawableIconForAreaColorId(defaultWithAlpha, size)
		);
	}

	public static Drawable getDrawableIconForAreaColorId(int color, int size) {

		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.OVAL);
		shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
		shape.setColor(color);
		//shape.setStroke(1, Color.BLACK);
		shape.setSize(size, size);

		return shape;
	}

}
