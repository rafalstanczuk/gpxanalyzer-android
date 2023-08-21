package com.itservices.gpxanalyzer.utils.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;

public class DrawableUtil {

	public static Drawable scaledDrawable(Context context, @DrawableRes int id, int width, int height ) {
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false);

		return new BitmapDrawable(context.getResources(), bmpScaled);
	}
}