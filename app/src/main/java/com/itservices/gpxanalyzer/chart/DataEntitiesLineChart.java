package com.itservices.gpxanalyzer.chart;

import static com.itservices.gpxanalyzer.chart.entry.CurveMeasurementEntry.CURVE_MEASUREMENT;
import static com.itservices.gpxanalyzer.utils.common.FormatNumberUtil.getFormattedTime;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.LineChartSettings;
import com.itservices.gpxanalyzer.chart.settings.background.GridBackgroundDrawer;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.chart.settings.highlight.StaticChartHighlighter;
import com.itservices.gpxanalyzer.data.DataEntity;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DataEntitiesLineChart extends LineChart {

	@Inject
	DataEntityInfoLayoutView dataEntityInfoLayoutView;

	@Inject
	PaletteColorDeterminer paletteColorDeterminer;

	@Inject
	GridBackgroundDrawer gridBackgroundDrawer;

	@Inject
	LineChartScaler lineChartScaler;

	@Inject
	LimitLinesBoundaries limitLinesBoundaries;

	@Nullable
	private MainActivity mainActivity;
	private LineChartSettings lineChartSettings;

	public DataEntitiesLineChart(Context context) {
		super(context);
		initMeasurementInfoLayoutView();
	}

	public DataEntitiesLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		initMeasurementInfoLayoutView();
	}

	public DataEntitiesLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initMeasurementInfoLayoutView();
	}

	public void bindActivity(@NonNull MainActivity requireActivity) {
		this.mainActivity = requireActivity;
	}

	public static int getDataSetIndexForEntryWithTimeInt(
			DataEntitiesLineChart lineChart, long entryTimeInt
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


				long timeInt = ((BaseEntry) entryLineData).getDataEntity().getTimestampMillis();

				if (timeInt == entryTimeInt) {
					dataSetIndexToHighlight = dataSetIndex;

					break;
				}
			}
		}
		return dataSetIndexToHighlight;
	}

	private void initMeasurementInfoLayoutView() {
		dataEntityInfoLayoutView.setDrawingCacheEnabled(true);

		try {
			this.addView(dataEntityInfoLayoutView);
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

		gridBackgroundDrawer.drawGridBackground(this, paletteColorDeterminer, canvas);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		dataEntityInfoLayoutView.layout(
				90 ,getHeight() / 20,
				getWidth(),	getHeight()
		);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		dataEntityInfoLayoutView.measure(dataEntityInfoLayoutView.getMeasuredWidth(),
			dataEntityInfoLayoutView.getMeasuredHeight()
		);
	}

	public void initChart(LineChartSettings lineChartSettings) {
		StaticChartHighlighter<DataEntitiesLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
				this, (BarLineChartTouchListener) mChartTouchListener);
		setHighlighter(staticChartHighlighter);

		this.lineChartSettings = lineChartSettings;

		clear();
		setData(new LineData());

		loadChartSettings();

		invalidate();
	}

	public BarLineChartTouchListener getChartTouchListener() {
		return (BarLineChartTouchListener) mChartTouchListener;
	}

	public void highlightCenterValueInTranslation() {
		Entry entry = getEntryByTouchPoint(getWidth() / 2.0f, getHeight() / 2.0f);

		if (entry instanceof BaseEntry) {
			BaseEntry baseEntry = (BaseEntry) entry;

			long entryTimeInt = baseEntry.getDataEntity().getTimestampMillis();
			int dataSetIndexToHighlight = getDataSetIndexForEntryWithTimeInt(
				this, entryTimeInt);

			highlightValue(baseEntry.getX(), baseEntry.getY(), dataSetIndexToHighlight, true);
		}
	}

	public void setHighlightedEntry(Entry selectedEntry) {
		if (!(selectedEntry instanceof BaseEntry)) {
			return;
		}

		ChartTouchListener.ChartGesture chartGesture = getChartTouchListener().getLastGesture();

		// isFullyZoomedOut() 		

		determineSettingsMeasurementCurveLineHighlightIndicator(chartGesture);

		DataEntity dataEntity = ( (BaseEntry)selectedEntry).getDataEntity();

		assert mainActivity != null;

		int primaryDataIndex = dataEntity.getPrimaryDataIndex();

		mainActivity.runOnUiThread(() -> {
			dataEntityInfoLayoutView.setTime( getFormattedTime(dataEntity.getTimestampMillis()) );
			dataEntityInfoLayoutView.setValue1(
					String.format(Locale.getDefault(), "%.1f", dataEntity.getValueList().get(primaryDataIndex))
			);
			String unit1 = dataEntity.getUnitList().get(primaryDataIndex);
			dataEntityInfoLayoutView.setValue1Unit(unit1);

		/*	dataEntityInfoLayoutView.setValue2(
					String.format(Locale.getDefault(), "%.2f", dataEntity.getValueList().get(1))
			);
			String unit2 = dataEntity.getUnitList().get(1);
			dataEntityInfoLayoutView.setValue2Unit(unit2);*/

			dataEntityInfoLayoutView.invalidate();
		});
	}

	private void determineSettingsMeasurementCurveLineHighlightIndicator(
		ChartTouchListener.ChartGesture chartGesture
	) {
		if (getLineData()==null) {
			return;
		}

		LineDataSet dataEntityCurveLineDataSet = ((LineDataSet) getLineData().getDataSetByLabel(CURVE_MEASUREMENT, false));

		if (dataEntityCurveLineDataSet != null) {

			if (isFullyZoomedOut()) {
				dataEntityCurveLineDataSet.setDrawHorizontalHighlightIndicator(true);
			} else {
				switch (chartGesture) {
					case X_ZOOM:
					case Y_ZOOM:
					case PINCH_ZOOM:
					case ROTATE:
					case DOUBLE_TAP:
					case LONG_PRESS:
					case SINGLE_TAP:
						dataEntityCurveLineDataSet.setDrawHorizontalHighlightIndicator(true);

						break;

					case NONE:
					case FLING:
					case DRAG:
						dataEntityCurveLineDataSet.setDrawHorizontalHighlightIndicator(false);

						break;
				}
			}
		}
	}

	public PaletteColorDeterminer getPaletteColorDeterminer() {
		return paletteColorDeterminer;
	}

	public void loadChartSettings() {
		limitLinesBoundaries.initLimitLines(paletteColorDeterminer);
		lineChartScaler.setLimitLinesBoundaries(limitLinesBoundaries);
		lineChartSettings.setLimitLinesBoundaries(limitLinesBoundaries);

		lineChartSettings.setChartSettingsFor(this);
	}

	public LineChartScaler getLineChartScaler() {
		return lineChartScaler;
	}

	public void scale() {
		lineChartScaler.scale(this);
	}

	public void resetTimeScale() {
		assert mainActivity != null;

		mainActivity.runOnUiThread(
				DataEntitiesLineChart.this::fitScreen
		);
	}
}
