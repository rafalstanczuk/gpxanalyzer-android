package com.itservices.gpxanalyzer.utils.ui;

import static com.itservices.gpxanalyzer.utils.ui.ColorUtil.setAlphaInIntColor;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.itservices.gpxanalyzer.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IconsUtil {

	public static List<Drawable> generateDrawableIconForAreaList(int size, int alpha, PaletteColorDeterminer paletteColorDeterminer) {
		List<Drawable> measurementDrawableIconList = new ArrayList<>();

		for (Map.Entry<Integer, BoundaryColorSpan> entry: paletteColorDeterminer.getPalette().entrySet()) {
			int color = entry.getValue().getColor();

			int colorWithAlpha = setAlphaInIntColor(color, alpha);

			measurementDrawableIconList.add(
				getDrawableIconForAreaColorId(colorWithAlpha, size, true)
			);
		}

		addDefaultIcon(size, alpha, measurementDrawableIconList);

		return measurementDrawableIconList;
	}

	private static void addDefaultIcon(int size, int alpha, List<Drawable> measurementDrawableIconList) {
		int defaultWithAlpha = setAlphaInIntColor(Color.BLACK, alpha);

		measurementDrawableIconList.add(
			getDrawableIconForAreaColorId(defaultWithAlpha, size, true)
		);
	}

	public static Drawable getDrawableIconForAreaColorId(int color, int size, boolean drawStroke) {

		GradientDrawable shape = new GradientDrawable();
		shape.setShape(GradientDrawable.OVAL);
		shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
		shape.setColor(color);
		shape.setSize(size, size);

		if (drawStroke) {
			shape.setStroke(1, Color.BLACK);
		}

		return shape;
	}

}
