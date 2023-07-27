package com.itservices.gpxanalyzer.logbook.chart;

import static com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry.CURVE_MEASUREMENT;
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
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementMeasurementEntry;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import com.itservices.gpxanalyzer.logbook.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.settings.MeasurementBoundariesPreferences;
import com.itservices.gpxanalyzer.logbook.chart.settings.StaticChartHighlighter;

@AndroidEntryPoint
public class MeasurementCurveLineChart extends LineChart {

	MeasurementInfoLayoutView measurementInfoLayoutView;

	@Inject
	MeasurementBoundariesPreferences measurement;

	public MeasurementCurveLineChart(Context context) {
		super(context);
		initPreferencesValues();
	}

	public MeasurementCurveLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		initPreferencesValues();
	}

	public MeasurementCurveLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initPreferencesValues();
	}

	public static int getDataSetIndexForEntryWithTimeInt(
		MeasurementCurveLineChart lineChart, int csgmEntryTimeInt
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
		measurementInfoLayoutView = new MeasurementInfoLayoutView(getContext());
		measurementInfoLayoutView.setDrawingCacheEnabled(true);

		try {
			this.addView(measurementInfoLayoutView);
		} catch (Exception ignored) {
		}

		measurement.initValues(getContext());
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

		//measurementInfoLayoutView.getMeasuredWidth()

		measurementInfoLayoutView.layout(getWidth() / 2 +
				(int) (getContext().getResources().getDisplayMetrics().density * 20.0f),
			(int) (getContext().getResources().getDisplayMetrics().density * 20.0f), getWidth(),
			getHeight()
		);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		measurementInfoLayoutView.measure(measurementInfoLayoutView.getMeasuredWidth(),
			measurementInfoLayoutView.getMeasuredHeight()
		);
	}

	public void drawRectArea(Canvas canvas, Context context, int areaid) {
		if (hasMinMaxMeasurement()) {
			int colorForAreaId = getColorForAreaId(areaid);

			ViewPortHandler viewPort = mViewPortHandler;

			MPPointF leftMin = null;
			MPPointF leftMax = null;

			switch (areaid) {
				case RANGE_ID_ABOVE_HYPER_LIMIT_ORANGE: {
					if (measurement.getUpperMax() > 0) {
						leftMin = getPosition(new Entry(MIN_X_SCALED_TIME, measurement.getUpperMax()),
							YAxis.AxisDependency.LEFT
						);
					} else {
						leftMin = getPosition(
							new Entry(MIN_X_SCALED_TIME, measurement.getMaxTargetMeasurement()),
							YAxis.AxisDependency.LEFT
						);
					}
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, getYChartMax()), YAxis.AxisDependency.LEFT);
					break;
				}
				case RANGE_ID_ABOVE_TARGET_MAX_BELOW_HYPER_LIMIT_YELLOW: {
					if (measurement.getUpperMax() > 0) {
						leftMin = getPosition(
							new Entry(MIN_X_SCALED_TIME, measurement.getMaxTargetMeasurement()),
							YAxis.AxisDependency.LEFT
						);
						leftMax = getPosition(new Entry(MIN_X_SCALED_TIME, measurement.getUpperMax()),
							YAxis.AxisDependency.LEFT
						);
					}
					break;
				}
				case RANGE_ID_IN_TARGET_MIN_MAX_GREEN: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, measurement.getMinTargetMeasurement()),
						YAxis.AxisDependency.LEFT
					);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, measurement.getMaxTargetMeasurement()),
						YAxis.AxisDependency.LEFT
					);
					break;
				}
				case RANGE_ID_BELOW_TARGET_MIN_ABOVE_HYPO_LIMIT_PINK: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, measurement.getLowMeasurement()),
						YAxis.AxisDependency.LEFT
					);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, measurement.getMinTargetMeasurement()),
						YAxis.AxisDependency.LEFT
					);
					break;
				}
				case RANGE_ID_BELOW_HYPO_LIMIT_RED: {
					leftMin = getPosition(
						new Entry(MIN_X_SCALED_TIME, getYChartMin()), YAxis.AxisDependency.LEFT);
					leftMax = getPosition(
						new Entry(MIN_X_SCALED_TIME, measurement.getLowMeasurement()),
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

	public boolean hasMinMaxMeasurement() {
		return measurement.getMinTargetMeasurement() != Integer.MAX_VALUE &&
			measurement.getMaxTargetMeasurement() != Integer.MIN_VALUE;
	}

	@Override
	public void init() {
		super.init();

		StaticChartHighlighter<MeasurementCurveLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
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

		determineSettingsMeasurementCurveLineHighlightIndicator(chartGesture);

		BaseEntry baseEntry = (BaseEntry) selectedEntry;

		Calendar calendar = baseEntry.getCalendar();

		String unit = "[A]";

		if (selectedEntry instanceof CurveMeasurementEntry) {

		} else if (selectedEntry instanceof SingleMeasurementMeasurementEntry) {

		}

		activity.runOnUiThread(() -> {
			measurementInfoLayoutView.setTime(formatTime(calendar));
			measurementInfoLayoutView.setValue(String.valueOf((int) baseEntry.getY()));
			measurementInfoLayoutView.setValueUnit(unit);

			measurementInfoLayoutView.invalidate();
		});
	}

	private void determineSettingsMeasurementCurveLineHighlightIndicator(
		ChartTouchListener.ChartGesture chartGesture
	) {
		if (getLineData()==null) {
			return;
		}

		LineDataSet measurementCurveLineDataSet = ((LineDataSet) getLineData().getDataSetByLabel(CURVE_MEASUREMENT, false));

		if (measurementCurveLineDataSet != null) {

			if (isFullyZoomedOut()) {
				measurementCurveLineDataSet.setDrawHorizontalHighlightIndicator(true);
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
						measurementCurveLineDataSet.setDrawHorizontalHighlightIndicator(true);

						break;

					case FLING:
					case DRAG:
						measurementCurveLineDataSet.setDrawHorizontalHighlightIndicator(false);

						break;
				}
			}
		}
	}
}
