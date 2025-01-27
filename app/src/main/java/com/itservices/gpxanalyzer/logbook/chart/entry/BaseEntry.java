package com.itservices.gpxanalyzer.logbook.chart.entry;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.github.mikephil.charting.data.Entry;
import com.itservices.gpxanalyzer.data.gpx.StatisticResults;

public class BaseEntry extends Entry {
	protected final StatisticResults statisticResults;
	protected final Location location;


	public BaseEntry(
		float x, float y, Drawable icon, StatisticResults statisticResults,
		Location location
		) {
		super(x, y, icon);
		this.statisticResults = statisticResults;
		this.location = location;
	}

	public StatisticResults getStatisticResults() {
		return statisticResults;
	}

	public Location getLocation() {
		return location;
	}
}
