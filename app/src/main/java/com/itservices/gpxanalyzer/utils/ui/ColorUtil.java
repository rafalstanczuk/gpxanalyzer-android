package com.itservices.gpxanalyzer.utils.ui;

import android.graphics.Color;

public class ColorUtil {
	public static int setAlphaInIntColor(int intColor, int alpha) {
		int r = Color.red(intColor);
		int g = Color.green(intColor);
		int b = Color.blue(intColor);

		return Color.argb(alpha, r, g, b);
	}

	public static int argb(float alpha, float red, float green, float blue) {
		return ((int) (alpha * 255.0f + 0.5f) << 24) |
			((int) (red   * 255.0f + 0.5f) << 16) |
			((int) (green * 255.0f + 0.5f) <<  8) |
			(int) (blue  * 255.0f + 0.5f);
	}

	public static int rgb(float red, float green, float blue) {
		return 0xff000000 |
			((int) (red   * 255.0f + 0.5f) << 16) |
			((int) (green * 255.0f + 0.5f) <<  8) |
			(int) (blue  * 255.0f + 0.5f);
	}
}
