package com.itservices.gpxanalyzer.ui.components.chart.settings.highlight;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.ChartHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.IHighlighter;
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.itservices.gpxanalyzer.ui.components.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.ui.components.chart.entry.CurveEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom chart highlighter for GPX data visualization.
 * 
 * This class extends the MPAndroidChart highlighter to provide specialized highlighting
 * behavior for GPX data points. It filters highlights based on chart gesture types and
 * zoom levels, and provides specialized entry type filtering to ensure only appropriate
 * entries are highlighted.
 * 
 * The highlighter is designed to work with the {@link DataEntityLineChart} class and
 * handles different highlighting behaviors based on the current interaction mode of the chart
 * (dragging, zooming, tapping, etc.).
 *
 * @param <T> The type of chart data provider this highlighter works with
 */
public class StaticChartHighlighter<T extends BarLineScatterCandleBubbleDataProvider> extends ChartHighlighter<T> implements IHighlighter {

	/** The touch listener from the chart, used to determine current gesture type */
	private final BarLineChartTouchListener chartTouchListener;

	/**
	 * Creates a new StaticChartHighlighter for the specified chart.
	 *
	 * @param chart The chart this highlighter will work with
	 * @param chartTouchListener The touch listener from the chart
	 */
	public StaticChartHighlighter(
		T chart, BarLineChartTouchListener chartTouchListener
	) {
		super(chart);
		this.chartTouchListener = chartTouchListener;
	}

	/**
	 * Builds highlight objects for a specific dataset at the specified x-value.
	 * This method overrides the parent implementation to apply gesture-specific
	 * highlighting behavior, adapting the highlighting based on zoom level and
	 * current interaction mode.
	 *
	 * @param set The dataset to build highlights for
	 * @param dataSetIndex The index of the dataset
	 * @param xVal The x-value to highlight
	 * @param rounding The rounding method to use
	 * @return A list of highlight objects
	 */
	@Override
	protected List<Highlight> buildHighlights(
		IDataSet set, int dataSetIndex, float xVal, DataSet.Rounding rounding
	) {
		ChartTouchListener.ChartGesture chartGesture = chartTouchListener.getLastGesture();

		ArrayList<Highlight> highlights = new ArrayList<>();

		if (((DataEntityLineChart) mChart).isFullyZoomedOut()) {
			highlights = getHighlightsForClassEntries(
					CurveEntry.class, (LineDataSet) set, dataSetIndex, xVal, rounding);
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
							CurveEntry.class, (LineDataSet)set, dataSetIndex, xVal, rounding);
					break;

				case FLING:
				case DRAG:
					highlights = getHighlightsForClassEntries(
						CurveEntry.class, (LineDataSet) set, dataSetIndex, xVal, rounding);
					break;
			}
		}

		return highlights;
	}

	/**
	 * Gets highlights for entries of a specific class type.
	 * This method filters entries by their class type before creating highlight objects,
	 * ensuring that only the appropriate type of entries are highlighted.
	 *
	 * @param entryClass The class type of entries to highlight
	 * @param set The dataset containing the entries
	 * @param dataSetIndex The index of the dataset
	 * @param xVal The x-value to highlight
	 * @param rounding The rounding method to use
	 * @return A list of highlight objects for the matching entries
	 */
	private ArrayList<Highlight> getHighlightsForClassEntries(
		Class<?> entryClass, LineDataSet set, int dataSetIndex, float xVal, DataSet.Rounding rounding
	) {
		ArrayList<Highlight> highlights = new ArrayList<>();

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

	/**
	 * Calculates the distance between two points.
	 * This method overrides the parent implementation to only consider the x-axis distance,
	 * which is more appropriate for time-series data like GPX tracks.
	 *
	 * @param x1 The x-coordinate of the first point
	 * @param y1 The y-coordinate of the first point
	 * @param x2 The x-coordinate of the second point
	 * @param y2 The y-coordinate of the second point
	 * @return The distance between the points (in this case, the absolute x-axis difference)
	 */
	@Override
	protected float getDistance(float x1, float y1, float x2, float y2) {
		// Match only closest by x
		return Math.abs(x1 - x2);
	}
}
