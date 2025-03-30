package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;


import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.itservices.gpxanalyzer.R;

public enum GpxViewMode {
	ASL_T_1(R.string.one_chart_asl_t_short, R.string.one_chart_asl_t, R.drawable.ic_altitude_selector, R.string.altitude ),
	V_T_1(R.string.one_chart_v_t_short, R.string.one_chart_v_t, R.drawable.ic_speed_selector, R.string.speed);

	@StringRes
	private final int idShortName;
    private final int idLongName;
	@DrawableRes
	private final int drawableIconResId;

	@StringRes
	private final int primaryKeyStringId;


    GpxViewMode(@StringRes int idShortName, @StringRes int idLongName, @DrawableRes int drawableIconResId, @StringRes int primaryKeyStringId){
        this.idShortName = idShortName;
        this.idLongName = idLongName;
        this.drawableIconResId = drawableIconResId;
        this.primaryKeyStringId = primaryKeyStringId;
    }

	public GpxViewMode getNextCyclic() {
		int currOrdinal  = ordinal();
		int maxOrdinal = values().length-1;

		return currOrdinal==maxOrdinal ? values()[ASL_T_1.ordinal()] : values()[currOrdinal+1];
	}

	public String getShortName(Context context) {
		return context.getText(idShortName).toString();
	}
    public String getLongName(Context context) {
        return context.getText(idLongName).toString();
    }

	public int getPrimaryKeyStringId() {
		return primaryKeyStringId;
	}

	@DrawableRes
	public int getDrawableIconResId() {
		return drawableIconResId;
	}

	public static GpxViewMode from(int primaryDataIndex) throws IndexOutOfBoundsException {
		return GpxViewMode.values()[primaryDataIndex];
	}
}
