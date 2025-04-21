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

/**
 * Utility class for generating {@link Drawable} icons, specifically colored circular shapes,
 * often used for representing data series in legends or lists.
 */
public class IconsUtil {

	/**
	 * Generates a list of circular {@link Drawable} icons based on a color palette and a default color.
	 * Each icon represents a color span defined in the {@link PaletteColorDeterminer}.
	 * A default black icon is appended to the list.
	 * The alpha channel of each color is set according to the provided alpha value.
	 * Each icon is an oval shape (effectively a circle given equal size) with an optional black stroke.
	 *
	 * @param size                   The desired size (width and height) of the icons in pixels.
	 * @param alpha                  The alpha value (0-255) to apply to the icon colors.
	 * @param paletteColorDeterminer The determiner containing the color palette (mapping index to {@link BoundaryColorSpan}).
	 * @return A {@link List} of {@link Drawable} icons, one for each color in the palette plus a default black one.
	 */
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

	/**
	 * Adds a default black circular icon (with the specified alpha and size) to the provided list.
	 *
	 * @param size                       The size (width and height) of the icon in pixels.
	 * @param alpha                      The alpha value (0-255) for the icon color.
	 * @param dataEntityDrawableIconList The list to which the default icon will be added.
	 */
	private static void addDefaultIcon(int size, int alpha, List<Drawable> dataEntityDrawableIconList) {
		int defaultWithAlpha = setAlphaInIntColor(Color.BLACK, alpha);

		dataEntityDrawableIconList.add(
			getDrawableIconForAreaColorId(defaultWithAlpha, size, true)
		);
	}

	/**
	 * Creates a circular {@link Drawable} (specifically a {@link GradientDrawable} with OVAL shape)
	 * with the specified color, size, and an optional stroke.
	 *
	 * @param color      The ARGB color integer for the icon's fill.
	 * @param size       The desired size (width and height) of the icon in pixels.
	 * @param drawStroke If true, a 1-pixel black stroke is drawn around the circle.
	 * @return A {@link GradientDrawable} representing the colored circle icon.
	 */
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
