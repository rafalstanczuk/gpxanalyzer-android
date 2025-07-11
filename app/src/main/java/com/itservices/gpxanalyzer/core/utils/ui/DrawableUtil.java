package com.itservices.gpxanalyzer.core.utils.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;

import com.itservices.gpxanalyzer.R;

/**
 * Utility class for working with Android {@link Drawable} and {@link Bitmap} objects.
 * Provides methods for scaling drawables/bitmaps and converting between them.
 */
public class DrawableUtil {

	/**
	 * Creates a new {@link Drawable} by scaling a resource drawable to the specified width and height.
	 *
	 * @param context The application context.
	 * @param id      The resource ID of the drawable to scale (e.g., {@code R.drawable.my_icon}).
	 * @param width   The desired width in pixels for the scaled drawable.
	 * @param height  The desired height in pixels for the scaled drawable.
	 * @return A new {@link BitmapDrawable} containing the scaled bitmap.
	 */
	public static Drawable createScaledDrawable(Context context, @DrawableRes int id, int width, int height ) {
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, true);

		return new BitmapDrawable(context.getResources(), bmpScaled);
	}

	/**
	 * Creates a new {@link Drawable} by scaling a resource drawable to fit the dimensions of a given {@link View}.
	 * The scaling uses the current width and height of the provided view.
	 *
	 * @param view The {@link View} whose dimensions will be used for scaling.
	 * @param id   The resource ID of the drawable to scale.
	 * @return A new {@link BitmapDrawable} scaled to fit the view.
	 */
	public static Drawable createScaledDrawableFitWith(View view, @DrawableRes int id) {
        return createScaledDrawable(view.getContext(), id, view.getWidth(), view.getHeight());
	}

	/**
	 * Creates a new {@link Bitmap} by scaling a resource drawable to fit the dimensions of a given {@link View}.
	 * The scaling uses the current width and height of the provided view.
	 * Note: This method internally sets the alpha of the intermediate Drawable to 128 before converting to Bitmap.
	 *
	 * @param view The {@link View} whose dimensions will be used for scaling.
	 * @param id   The resource ID of the drawable to scale.
	 * @return A new {@link Bitmap} scaled to fit the view, potentially with reduced alpha.
	 */
	public static Bitmap createScaledFitWith(View view, @DrawableRes int id) {
		return createScaled(view.getContext(), id, view.getWidth(), view.getHeight());
	}

	/**
	 * Creates a scaled {@link Bitmap} from a drawable resource with a specific alpha value applied.
	 * Note: The alpha (128) is hardcoded in this private method.
	 *
	 * @param context The application context.
	 * @param id      The resource ID of the drawable.
	 * @param width   The desired width.
	 * @param height  The desired height.
	 * @return A new {@link Bitmap} scaled and with alpha set to 128.
	 */
	private static Bitmap createScaled(Context context, int id, int width, int height) {
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, true);
		Drawable drawable = new BitmapDrawable(context.getResources(), bmpScaled);

		drawable.setAlpha(128);

		Bitmap bitmapWithAlpha = drawableToBitmap(drawable);

		return bitmapWithAlpha;
	}

	/**
	 * Converts a {@link Drawable} object into a {@link Bitmap}.
	 * If the drawable is already a {@link BitmapDrawable}, it returns the underlying bitmap directly.
	 * Otherwise, it creates a new Bitmap with the drawable's intrinsic dimensions (or 1x1 if dimensions are invalid)
	 * and draws the drawable onto a canvas associated with the new bitmap.
	 *
	 * @param drawable The {@link Drawable} to convert.
	 * @return The resulting {@link Bitmap}.
	 */
	public static Bitmap drawableToBitmap (Drawable drawable) {
		Bitmap bitmap = null;

		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
}