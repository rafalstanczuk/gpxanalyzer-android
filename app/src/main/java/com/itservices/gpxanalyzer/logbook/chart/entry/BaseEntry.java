package com.itservices.gpxanalyzer.logbook.chart.entry;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;

import java.util.Calendar;

public class BaseEntry extends Entry {
	protected final StatisticResults statisticResults;
	protected final Calendar calendar;

	public BaseEntry(
		float x, float y, Drawable icon, StatisticResults statisticResults,
		Calendar calendar
	) {
		super(x, y, icon);
		this.statisticResults = statisticResults;
		this.calendar = calendar;
	}

	public StatisticResults getStatisticResults() {
		return statisticResults;
	}

	public Calendar getCalendar() {
		return calendar;
	}
}
