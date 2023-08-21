package com.itservices.gpxanalyzer.logbook.chart;

import static com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry.CURVE_MEASUREMENT;
import static com.itservices.gpxanalyzer.utils.common.FormatNumberUtil.getFormattedTime;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.logbook.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.logbook.chart.settings.background.GridBackgroundDrawer;
import com.itservices.gpxanalyzer.logbook.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.logbook.chart.settings.highlight.StaticChartHighlighter;

import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MeasurementLineChart extends LineChart {

	MeasurementInfoLayoutView measurementInfoLayoutView;

	@Inject
	LimitLinesBoundaries measurement;

	@Inject
	GridBackgroundDrawer gridBackgroundDrawer;

	public MeasurementLineChart(Context context) {
		super(context);
		initMeasurementInfoLayoutView();
	}

	public MeasurementLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		initMeasurementInfoLayoutView();
	}

	public MeasurementLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initMeasurementInfoLayoutView();
	}

	public static int getDataSetIndexForEntryWithTimeInt(
			MeasurementLineChart lineChart, long entryTimeInt
	) {
		int dataSetIndexToHighlight = 0;

		for (int dataSetIndex = 0;
			dataSetIndex < lineChart.getData().getDataSets().size(); dataSetIndex++) {

			ILineDataSet iLineDataSet = lineChart.getData().getDataSets().get(dataSetIndex);

			LineDataSet lineDataSet = (LineDataSet) iLineDataSet;

			for (Entry entryLineData : lineDataSet.getEntries()) {

				if (!(entryLineData instanceof BaseEntry)) {
					break;
				}

				Calendar calendar = ((BaseEntry) entryLineData).getCalendar();

				long timeInt = calendar.getTime().getTime();

				if (timeInt == entryTimeInt) {
					dataSetIndexToHighlight = dataSetIndex;

					break;
				}
			}
		}
		return dataSetIndexToHighlight;
	}

	private void initMeasurementInfoLayoutView() {
		measurementInfoLayoutView = new MeasurementInfoLayoutView(getContext());
		measurementInfoLayoutView.setDrawingCacheEnabled(true);

		try {
			this.addView(measurementInfoLayoutView);
		} catch (Exception ignored) {
		}
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

		gridBackgroundDrawer.drawGridBackground(this, canvas);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

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

	@Override
	public void init() {
		super.init();

		StaticChartHighlighter<MeasurementLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
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

			long entryTimeInt = baseEntry.getCalendar().getTime().getTime();
			int dataSetIndexToHighlight = getDataSetIndexForEntryWithTimeInt(
				this, entryTimeInt);

			highlightValue(baseEntry.getX(), baseEntry.getY(), dataSetIndexToHighlight, true);
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

		String unit = getContext().getResources().getString(R.string.default_measurement_unit);

/*		if (selectedEntry instanceof CurveMeasurementEntry) {

		} else if (selectedEntry instanceof SingleMeasurementEntry) {

		}*/

		activity.runOnUiThread(() -> {
			measurementInfoLayoutView.setTime(getFormattedTime(calendar));
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
