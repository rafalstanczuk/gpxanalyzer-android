package com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode;


import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.itservices.gpxanalyzer.R;

/**
 * Enumerates the different data views available for a single GPX chart.
 * Each view mode represents a specific type of data plotted against time (e.g., Altitude vs. Time).
 * It holds references to string resources for names (short and long) and a drawable resource for an icon,
 * as well as the string resource ID for the primary data key (e.g., "Altitude").
 */
public enum GpxViewMode {
	/** Represents Altitude (Above Sea Level) vs. Time chart view. */
	ASL_T_1(R.string.one_chart_asl_t_short, R.string.one_chart_asl_t, R.drawable.ic_altitude_selector, R.string.altitude ),
	/** Represents Velocity vs. Time chart view. */
	V_T_1(R.string.one_chart_v_t_short, R.string.one_chart_v_t, R.drawable.ic_speed_selector, R.string.speed);

	/** String resource ID for the short name of the view mode (e.g., "ASL/t"). */
	@StringRes
	private final int idShortName;
	/** String resource ID for the longer, descriptive name of the view mode (e.g., "Altitude vs Time"). */
	@StringRes
    private final int idLongName;
	/** Drawable resource ID for the icon representing the view mode. */
	@DrawableRes
	private final int drawableIconResId;

	/** String resource ID for the name of the primary data key being displayed (e.g., R.string.altitude). */
	@StringRes
	private final int primaryKeyStringId;


	/**
	 * Enum constructor.
	 *
	 * @param idShortName        String resource ID for the short name.
	 * @param idLongName         String resource ID for the long name.
	 * @param drawableIconResId Drawable resource ID for the icon.
	 * @param primaryKeyStringId String resource ID for the primary data key.
	 */
    GpxViewMode(@StringRes int idShortName, @StringRes int idLongName, @DrawableRes int drawableIconResId, @StringRes int primaryKeyStringId){
        this.idShortName = idShortName;
        this.idLongName = idLongName;
        this.drawableIconResId = drawableIconResId;
        this.primaryKeyStringId = primaryKeyStringId;
    }

	/**
	 * Gets the next view mode in the enum sequence, cycling back to the first one if currently at the last.
	 *
	 * @return The next {@link GpxViewMode}.
	 */
	public GpxViewMode getNextCyclic() {
		int currOrdinal  = ordinal();
		int maxOrdinal = values().length-1;

		return currOrdinal==maxOrdinal ? values()[ASL_T_1.ordinal()] : values()[currOrdinal+1];
	}

	/**
	 * Gets the localized short name for this view mode.
	 *
	 * @param context The application context for resolving string resources.
	 * @return The short name string.
	 */
	public String getShortName(Context context) {
		return context.getText(idShortName).toString();
	}

	/**
	 * Gets the localized long name for this view mode.
	 *
	 * @param context The application context for resolving string resources.
	 * @return The long name string.
	 */
    public String getLongName(Context context) {
        return context.getText(idLongName).toString();
    }

	/**
	 * Gets the string resource ID for the primary data key associated with this view mode.
	 *
	 * @return The string resource ID (e.g., R.string.altitude).
	 */
	public int getPrimaryKeyStringId() {
		return primaryKeyStringId;
	}

	/**
	 * Gets the drawable resource ID for the icon associated with this view mode.
	 *
	 * @return The drawable resource ID.
	 */
	@DrawableRes
	public int getDrawableIconResId() {
		return drawableIconResId;
	}

	/**
	 * Retrieves a {@link GpxViewMode} based on its ordinal value (index in the enum declaration).
	 *
	 * @param primaryDataIndex The ordinal value (0 for ASL_T_1, 1 for V_T_1, etc.).
	 * @return The corresponding {@link GpxViewMode}.
	 * @throws IndexOutOfBoundsException if the index is invalid.
	 */
	public static GpxViewMode from(int primaryDataIndex) throws IndexOutOfBoundsException {
		return GpxViewMode.values()[primaryDataIndex];
	}
}
