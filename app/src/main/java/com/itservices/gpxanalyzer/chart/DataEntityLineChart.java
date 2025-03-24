package com.itservices.gpxanalyzer.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.chart.entry.BaseEntry;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.chart.settings.background.GridBackgroundDrawer;
import com.itservices.gpxanalyzer.chart.settings.background.LimitLinesBoundaries;
import com.itservices.gpxanalyzer.chart.settings.highlight.StaticChartHighlighter;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observable;
import io.reactivex.Single;

@AndroidEntryPoint
public class DataEntityLineChart extends LineChart {

/*	@Inject
	DataEntityInfoLayoutView dataEntityInfoLayoutView;*/

	@Inject
	PaletteColorDeterminer paletteColorDeterminer;

	@Inject
	GridBackgroundDrawer gridBackgroundDrawer;

	@Inject
	LineChartScaler scaler;

	@Inject
	LimitLinesBoundaries limitLinesBoundaries;

	@Nullable
	private MainActivity mainActivity;

	public DataEntityLineChart(Context context) {
		super(context);
		initDataEntityInfoLayoutView();
	}

	public DataEntityLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);

		initDataEntityInfoLayoutView();
	}

	public DataEntityLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initDataEntityInfoLayoutView();
	}

	private void initDataEntityInfoLayoutView() {
		//dataEntityInfoLayoutView.setDrawingCacheEnabled(true);

/*		try {
			this.addView(dataEntityInfoLayoutView);
		} catch (Exception ignored) {
		}*/
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

/*		dataEntityInfoLayoutView.layout(
				90 ,getHeight() / 20,
				getWidth(),	getHeight()
		);*/
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
/*
		dataEntityInfoLayoutView.measure(dataEntityInfoLayoutView.getMeasuredWidth(),
			dataEntityInfoLayoutView.getMeasuredHeight()
		);*/
	}

	public Single<RequestStatus> initChart(LineChartSettings settings) {
		return Single.fromCallable(() -> {
			StaticChartHighlighter<DataEntityLineChart> staticChartHighlighter = new StaticChartHighlighter<>(
					this, (BarLineChartTouchListener) mChartTouchListener);
			setHighlighter(staticChartHighlighter);

			setData(new LineData());

			loadChartSettings(settings);

			invalidate();
			return RequestStatus.DONE;
		});
	}

	public BarLineChartTouchListener getChartTouchListener() {
		return (BarLineChartTouchListener) mChartTouchListener;
	}

	public void highlightCenterValueInTranslation() {
		MPPointF pointFCenter = mViewPortHandler.getContentCenter();

		Entry entry = getEntryByTouchPoint(
				pointFCenter.getX(),
				pointFCenter.getY()
		);

		if (entry instanceof BaseEntry) {
			BaseEntry baseDataEntityEntry = (BaseEntry) entry;

			highlightValue(baseDataEntityEntry.getX(), baseDataEntityEntry.getY(), baseDataEntityEntry.getDataSetIndex(), true);
		}
	}

	public void setHighlightedEntry(Entry selectedEntry) {
		if (!(selectedEntry instanceof BaseEntry)) {
			return;
		}

		ChartTouchListener.ChartGesture chartGesture = getChartTouchListener().getLastGesture();

		determineSettingsDataEntityCurveLineHighlightIndicator(chartGesture);
	}

	private void determineSettingsDataEntityCurveLineHighlightIndicator(
		ChartTouchListener.ChartGesture chartGesture
	) {
		if (getLineData()==null) {
			return;
		}

		AtomicBoolean shouldDraw = new AtomicBoolean(false);
		List<ILineDataSet> dataEntityCurveLineDataSet = getLineData().getDataSets();

		if (dataEntityCurveLineDataSet != null) {

			if (isFullyZoomedOut()) {
				shouldDraw.set(false);
			} else {
				switch (chartGesture) {
					case X_ZOOM:
					case Y_ZOOM:
					case PINCH_ZOOM:
					case ROTATE:
					case DOUBLE_TAP:
					case LONG_PRESS:
					case SINGLE_TAP:
						shouldDraw.set(false);

						break;

					case NONE:
					case FLING:
					case DRAG:
						shouldDraw.set(false);

						break;
				}
			}

			dataEntityCurveLineDataSet.forEach( iLineDataSet -> {
                ((LineDataSet) iLineDataSet).setDrawHorizontalHighlightIndicator (shouldDraw.get() );
            });
		}
	}

	public PaletteColorDeterminer getPaletteColorDeterminer() {
		return paletteColorDeterminer;
	}

	public void loadChartSettings(LineChartSettings settings) {
		limitLinesBoundaries.initLimitLines(paletteColorDeterminer);
		scaler.setLimitLinesBoundaries(limitLinesBoundaries);
		scaler.scale(this);
		settings.setLimitLinesBoundaries(limitLinesBoundaries);

		settings.setChartSettingsFor(this);
	}

	public void animateZoomToCenter(final float targetScaleX, final float targetScaleY, long duration) {
		super.animateZoomToCenter(targetScaleX, targetScaleY, duration, null);
	}

    @Override
	protected void onDetachedFromWindow() {
		if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
			((LineChartRenderer) mRenderer).releaseBitmap();
		}
		super.onDetachedFromWindow();
	}

	public void setDataEntityWrapper(DataEntityWrapper dataEntityWrapper) {
		paletteColorDeterminer.setDataEntityWrapper(dataEntityWrapper);
		scaler.setDataEntityWrapper(dataEntityWrapper);
	}
}
