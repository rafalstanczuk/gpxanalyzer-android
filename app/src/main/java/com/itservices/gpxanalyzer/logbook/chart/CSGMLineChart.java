package com.itservices.gpxanalyzer.logbook.chart;

import static com.itservices.gpxanalyzer.logbook.chart.entry.CSGMEntry.CGSM;
import static com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil.getTimeAsIntFromDate;
import static com.itservices.gpxanalyzer.logbook.chart.settings.CustomMarker.formatTime;
import static com.itservices.gpxanalyzer.logbook.chart.settings.HourMinutesAxisValueFormatter.MIN_X_SCALED_TIME;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.RANGE_ID_BELOW_HYPO_LIMIT_RED;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.RANGE_ID_IN_TARGET_MIN_MAX_GREEN;
import static com.itservices.gpxanalyzer.logbook.chart.settings.Measurement5RangesUtil.getColorForAreaId;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.logbook.chart.entry.GlucoseEntry;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import com.itservices.gpxanalyzer.logbook.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.CSGMEntry;
import com.itservices.gpxanalyzer.logbook.chart.settings.GlucoseBoundariesPreferences;
import com.itservices.gpxanalyzer.logbook.chart.settings.StaticChartHighlighter;

@AndroidEntryPoint
public class CSGMLineChart extends LineChart {

	CsgmInfoLayoutView csgmInfoLayoutView;

	@Inject
	GlucoseBoundariesPreferences glucose;

	public CSGMLineChart(Context context) {
		super(context);
		initPreferencesValues();
	}

	public CSGMLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		initPreferencesValues();
	}

	public CSGMLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initPreferencesValues();
	}

	public static int getDataSetIndexForEntryWithTimeInt(
		CSGMLineChart lineChart, int csgmEntryTimeInt
	) {
		int dataSetIndexToHighlight = 0;

		for (int dataSetIndex = 0;
			dataSetIndex < lineChart.getData().getDataSets().size(); dataSetIndex++) {

			ILineDataSet iLineDataSet = lineChart.getData().getDataSets().get(dataSetIndex);

			LineDataSet lineDataSet = (LineDataSet) iLineDataSet;

			for (Entry entryLinedata : lineDataSet.getValues()) {

				if (!(entryLinedata instanceof BaseEntry)) {
					break;
				}

				Calendar calendar = ((BaseEntry) entryLinedata).getCalendar();

				int timeInt = getTimeAsIntFromDate(calendar);

				if (timeInt == csgmEntryTimeInt) {
					dataSetIndexToHighlight = dataSetIndex;

					break;
				}
			}
		}
		return dataSetIndexToHighlight;
	}

	private void initPreferencesValues() {
		csgmInfoLayoutView = new CsgmInfoLayoutView(getContext());
		csgmInfoLayoutView.setDrawingCacheEnabled(true);

		try {
			this.addView(csgmInfoLayoutView);
		} catch (Exception ignored) {
		}

		glucose.initValues(getContext());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
		} catch (Exception ignore) {
		}
	}

	@Override
	protected void drawGridBackground(Canvas canvas) {
		super.drawGridBackground(canvas);

		drawRectArea(canvas, getContext(), RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW);
		drawRectArea(canvas, getContext(), RANGE_ID_IN_TARGET_MIN_MAX_GREEN);
		drawRectArea(canvas, getContext(), RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE);
		drawRectArea(canvas, getContext(), RANGE_ID_BELOW_HYPO_LIMIT_RED);
		drawRectArea(canvas, getContext(), RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		//csgmInfoLayoutView.getMeasuredWidth()

		csgmInfoLayoutView.layout(getWidth() / 2 +
				(int) (getContext().getResources().getDisplayMetrics().density * 20.0f),
			(int) (getContext().getResources().getDisplayMetrics().density * 20.0f), getWidth(),
			getHeight()
		);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		csgmInfoLayoutView.measure(csgmInfoLayoutView.getMeasuredWidth(),
			csgmInfoLayoutView.getMeasuredHeight()
		);
	}

	public void drawRectArea(Canvas canvas, Context context, int areaid) {
		if (hasMinMaxGlucose()) {
			int colorForAreaId = getColorForAreaId(areaid);

			ViewPortHandler viewPort = mViewPortHandler;

			MPPointF leftMin = null;
			MPPointF leftMax = null;

			switch (areaid) {
				case RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE: {
					if (glucose.getUpperMax() > 0) {
						leftMin = getPosition(new Entry(MIN_X_SCALED_TIME, glucose.getUpperMax()),
							YAxis.AxisDependency.LEFT
						);
					} else {
						leftMin = getPosition(
							new Entry(MIN_X_SCALED_TIME, glucose.getMaxTargetGlucose()),
							YAxis.AxisDependency.LEFT
						);
					}
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, getYChartMax()), YAxis.AxisDependency.LEFT);
					break;
				}
				case RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW: {
					if (glucose.getUpperMax() > 0) {
						leftMin = getPosition(
							new Entry(MIN_X_SCALED_TIME, glucose.getMaxTargetGlucose()),
							YAxis.AxisDependency.LEFT
						);
						leftMax = getPosition(new Entry(MIN_X_SCALED_TIME, glucose.getUpperMax()),
							YAxis.AxisDependency.LEFT
						);
					}
					break;
				}
				case RANGE_ID_IN_TARGET_MIN_MAX_GREEN: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, glucose.getMinTargetGlucose()),
						YAxis.AxisDependency.LEFT
					);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, glucose.getMaxTargetGlucose()),
						YAxis.AxisDependency.LEFT
					);
					break;
				}
				case RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, glucose.getHypoglycemiaGlucose()),
						YAxis.AxisDependency.LEFT
					);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, glucose.getMinTargetGlucose()),
						YAxis.AxisDependency.LEFT
					);
					break;
				}
				case RANGE_ID_BELOW_HYPO_LIMIT_RED: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, getYChartMin()), YAxis.AxisDependency.LEFT);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, glucose.getHypoglycemiaGlucose()),
						YAxis.AxisDependency.LEFT
					);
					break;
				}
				default: {
					break;
				}
			}

			Paint paintMinMax = new Paint();
			paintMinMax.setColor(colorForAreaId);

			if (leftMax != null) {
				RectF rectMinMax = new RectF(viewPort.contentLeft(), leftMax.y,
					viewPort.contentRight(), leftMin.y
				);

				canvas.drawRect(rectMinMax, paintMinMax);
			}
		}
	}

	public boolean hasMinMaxGlucose() {
		return glucose.getMinTargetGlucose() != Integer.MAX_VALUE &&
			glucose.getMaxTargetGlucose() != Integer.MIN_VALUE;
	}

	@Override
	public void init() {
		super.init();

		StaticChartHighlighter<CSGMLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
			this, (BarLineChartTouchListener) mChartTouchListener);
		setHighlighter(staticChartHighlighter);
	}

	public BarLineChartTouchListener getChartTouchListener() {
		return (BarLineChartTouchListener) mChartTouchListener;
	}

	public void highlightCenterValueInTranslation() {
		Entry entry = getEntryByTouchPoint(getWidth() / 2.0f, getHeight() / 2.0f);

		if (entry instanceof BaseEntry) {
			BaseEntry baseEntry = (BaseEntry) entry;

			int csgmEntryTimeInt = getTimeAsIntFromDate(baseEntry.getCalendar());
			int dataSetIndexToHighlight = getDataSetIndexForEntryWithTimeInt(
				this, csgmEntryTimeInt);

			if (baseEntry != null) {
				highlightValue(baseEntry.getX(), baseEntry.getY(), dataSetIndexToHighlight, true);
			}
		}
	}

	public void setHighlightedEntry(MainActivity activity, Entry selectedEntry) {
		if (!(selectedEntry instanceof BaseEntry)) {
			return;
		}

		ChartTouchListener.ChartGesture chartGesture = getChartTouchListener().getLastGesture();

		// isFullyZoomedOut() 		

		determineSettingsCGSMLineHighlightIndicator(chartGesture);

		BaseEntry baseEntry = (BaseEntry) selectedEntry;

		Calendar calendar = baseEntry.getCalendar();

		String unit = "[A]";

		if (selectedEntry instanceof CSGMEntry) {

		} else if (selectedEntry instanceof GlucoseEntry) {

		}

		activity.runOnUiThread(() -> {
			csgmInfoLayoutView.setTime(formatTime(calendar));
			csgmInfoLayoutView.setValue(String.valueOf((int) baseEntry.getY()));
			csgmInfoLayoutView.setValueUnit(unit);

			csgmInfoLayoutView.invalidate();
		});
	}

	private void determineSettingsCGSMLineHighlightIndicator(
		ChartTouchListener.ChartGesture chartGesture
	) {
		if (getLineData()==null) {
			return;
		}

		LineDataSet cgsmLineDataSet = ((LineDataSet) getLineData().getDataSetByLabel(CGSM, false));

		if (cgsmLineDataSet != null) {

			if (isFullyZoomedOut()) {
				cgsmLineDataSet.setDrawHorizontalHighlightIndicator(true);
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
						cgsmLineDataSet.setDrawHorizontalHighlightIndicator(true);

						break;

					case FLING:
					case DRAG:
						cgsmLineDataSet.setDrawHorizontalHighlightIndicator(false);

						break;
				}
			}
		}
	}
}
