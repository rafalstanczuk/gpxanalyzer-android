package com.itservices.gpxanalyzer.chart.settings.highlight;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.ChartHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.IHighlighter;
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.entry.BaseDataEntityEntry;
import com.itservices.gpxanalyzer.chart.entry.CurveDataEntityEntry;

import java.util.ArrayList;
import java.util.List;

public class StaticChartHighlighter<T extends BarLineScatterCandleBubbleDataProvider> extends ChartHighlighter<T> implements IHighlighter {

	private final BarLineChartTouchListener chartTouchListener;

	public StaticChartHighlighter(
		T chart, BarLineChartTouchListener chartTouchListener
	) {
		super(chart);
		this.chartTouchListener = chartTouchListener;
	}

	@Override
	protected List<Highlight> buildHighlights(
		IDataSet set, int dataSetIndex, float xVal, DataSet.Rounding rounding
	) {
		ChartTouchListener.ChartGesture chartGesture = chartTouchListener.getLastGesture();

		ArrayList<Highlight> highlights = new ArrayList<>();

		if (((DataEntityLineChart) mChart).isFullyZoomedOut()) {
			highlights = getHighlightsForClassEntries(
					CurveDataEntityEntry.class, set, dataSetIndex, xVal, rounding);
		} else {

			switch (chartGesture) {
				case NONE:
				case X_ZOOM:
				case Y_ZOOM:
				case PINCH_ZOOM:
				case ROTATE:
				case DOUBLE_TAP:
				case LONG_PRESS:
				case SINGLE_TAP:
					highlights = getHighlightsForClassEntries(
							CurveDataEntityEntry.class, set, dataSetIndex, xVal, rounding);
					break;

				case FLING:
				case DRAG:
					highlights = getHighlightsForClassEntries(
						CurveDataEntityEntry.class, set, dataSetIndex, xVal, rounding);
					break;
			}
		}

		return highlights;
	}

	private ArrayList<Highlight> getHighlightsForClassEntries(
		Class<?> entryClass, IDataSet set, int dataSetIndex, float xVal, DataSet.Rounding rounding
	) {
		ArrayList<Highlight> highlights = new ArrayList<>();

		//noinspection unchecked
		List<Entry> rawEntries = set.getEntriesForXValue(xVal);

		List<Entry> entries = new ArrayList<>();

		for (Entry entry : rawEntries) {
			if (entryClass.isInstance(entry)) {
				entries.add(entry);
			}
		}

		if (entries.size() == 0) {
			// Try to find closest x-value and take all entries for that x-value
			final Entry closest = set.getEntryForXValue(xVal, Float.NaN, rounding);
			if (closest != null && (entryClass.isInstance(closest))) {
				//noinspection unchecked
				entries = set.getEntriesForXValue(closest.getX());
			}
		}

		if (entries.size() == 0) return highlights;

		for (Entry e : entries) {
			if (entryClass.isInstance(e)) {
				MPPointD pixels = mChart.getTransformer(set.getAxisDependency())
					.getPixelForValues(e.getX(), e.getY());

				highlights.add(new Highlight(e.getX(), e.getY(), (float) pixels.x, (float) pixels.y,
					dataSetIndex, set.getAxisDependency()
				));
			}
		}
		return highlights;
	}

	@Override
	protected float getDistance(float x1, float y1, float x2, float y2) {
		// Match only closest by x
		return Math.abs(x1 - x2);
	}
}
