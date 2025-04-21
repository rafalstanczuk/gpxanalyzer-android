package com.itservices.gpxanalyzer.utils.ui;

import android.graphics.Color;

/**
 * Utility class for working with Android color integers (ARGB).
 */
public class ColorUtil {
	/**
	 * Modifies the alpha component of an existing ARGB color integer.
	 *
	 * @param intColor The original color integer (e.g., obtained from {@code ContextCompat.getColor()}).
	 * @param alpha    The new alpha value (0-255, where 0 is fully transparent and 255 is fully opaque).
	 * @return A new color integer with the original RGB values and the specified alpha value.
	 */
	public static int setAlphaInIntColor(int intColor, int alpha) {
		int r = Color.red(intColor);
		int g = Color.green(intColor);
		int b = Color.blue(intColor);

		return Color.argb(alpha, r, g, b);
	}

	/**
	 * Creates an ARGB color integer from float components (ranging from 0.0f to 1.0f).
	 *
	 * @param alpha The alpha component (0.0f - 1.0f).
	 * @param red   The red component (0.0f - 1.0f).
	 * @param green The green component (0.0f - 1.0f).
	 * @param blue  The blue component (0.0f - 1.0f).
	 * @return The corresponding ARGB color integer.
	 */
	public static int argb(float alpha, float red, float green, float blue) {
		return ((int) (alpha * 255.0f + 0.5f) << 24) |
			((int) (red   * 255.0f + 0.5f) << 16) |
			((int) (green * 255.0f + 0.5f) <<  8) |
			(int) (blue  * 255.0f + 0.5f);
	}

	/**
	 * Creates an opaque RGB color integer (alpha is set to 255) from float components (ranging from 0.0f to 1.0f).
	 *
	 * @param red   The red component (0.0f - 1.0f).
	 * @param green The green component (0.0f - 1.0f).
	 * @param blue  The blue component (0.0f - 1.0f).
	 * @return The corresponding opaque RGB color integer (alpha = 255).
	 */
	public static int rgb(float red, float green, float blue) {
		return 0xff000000 |
			((int) (red   * 255.0f + 0.5f) << 16) |
			((int) (green * 255.0f + 0.5f) <<  8) |
			(int) (blue  * 255.0f + 0.5f);
	}
}
