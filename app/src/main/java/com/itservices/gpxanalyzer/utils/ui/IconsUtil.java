package com.itservices.gpxanalyzer.utils.ui;

import static com.itservices.gpxanalyzer.utils.ui.ColorUtil.setAlphaInIntColor;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.itservices.gpxanalyzer.ui.components.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.ui.components.chart.palette.PaletteColorDeterminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IconsUtil {

	public static List<Drawable> generateDrawableIconForAreaList(int size, int alpha, PaletteColorDeterminer paletteColorDeterminer) {
		List<Drawable> dataEntityDrawableIconList = new ArrayList<>();

		for (Map.Entry<Integer, BoundaryColorSpan> entry: paletteColorDeterminer.getPalette().entrySet()) {
			int color = entry.getValue().color();

			int colorWithAlpha = setAlphaInIntColor(color, alpha);

			dataEntityDrawableIconList.add(
				getDrawableIconForAreaColorId(colorWithAlpha, size, true)
			);
		}

		addDefaultIcon(size, alpha, dataEntityDrawableIconList);

		return dataEntityDrawableIconList;
	}

	private static void addDefaultIcon(int size, int alpha, List<Drawable> dataEntityDrawableIconList) {
		int defaultWithAlpha = setAlphaInIntColor(Color.BLACK, alpha);

		dataEntityDrawableIconList.add(
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
