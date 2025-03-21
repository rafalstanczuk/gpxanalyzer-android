package com.itservices.gpxanalyzer.chart.settings.highlight;

import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE;
import static com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType.FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.LineChartSettings;
import com.itservices.gpxanalyzer.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeProcessedDataType;
import com.itservices.gpxanalyzer.data.cumulative.CumulativeStatistics;
import com.itservices.gpxanalyzer.data.cumulative.TrendStatistics;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.databinding.CustomMarkerViewBinding;
import com.itservices.gpxanalyzer.utils.common.FormatNumberUtil;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;
import com.itservices.gpxanalyzer.utils.ui.TextViewUtil;
import com.itservices.gpxanalyzer.utils.ui.ViewUtil;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.qualifiers.ApplicationContext;

@SuppressLint("ViewConstructor")
public class CustomMarker extends MarkerView {

    private static final int layoutResource = R.layout.custom_marker_view;

    CustomMarkerViewBinding binding;
    private LineChartSettings settings;

    @Inject
    public CustomMarker(@ActivityContext Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        binding = CustomMarkerViewBinding.inflate(inflater, this, true);

    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(getWidth() * 0.05f, getHeight() * 0.1f);
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        DataEntityLineChart chartView = (DataEntityLineChart) getChartView();

        ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
                .getLastGesture();

        if (entry instanceof CurveEntry curveDataEntityEntry) {
            DataEntity dataEntity = curveDataEntityEntry.getDataEntity();

            SpannableStringBuilder timeLine = TextViewUtil.getSpannableStringBuilder(
                    FormatNumberUtil.getFormattedTime(dataEntity.timestampMillis()), " h"
            );

            String unitString = curveDataEntityEntry.getDataEntityWrapper().getUnit(dataEntity);

            SpannableStringBuilder valueLine = TextViewUtil.getSpannableStringBuilder(
                    String.valueOf((int) curveDataEntityEntry.getY()), " " + unitString);

            binding.markerTextViewTime.setText(timeLine, TextView.BufferType.SPANNABLE);
            binding.markerTextViewValue.setText(valueLine, TextView.BufferType.SPANNABLE);

            setupTrendStatisticsLayout(curveDataEntityEntry.getTrendBoundaryDataEntity().trendStatistics(), unitString);

            setupCumulativeProcessedDataTypesLayout(dataEntity, curveDataEntityEntry.getDataEntityWrapper());
        }

        super.refreshContent(entry, highlight);
    }

    private void setupCumulativeProcessedDataTypesLayout(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper) {
        ViewUtil.setVisibility(binding.cumulativeProcessedDataTypesLayout, settings.isDrawAscDescSegEnabled());
        if (settings.isDrawAscDescSegEnabled()) {
            setupCumulativeProcessedDataTypes(dataEntity, dataEntityWrapper);
        }
    }

    private void setupCumulativeProcessedDataTypes(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper) {
        fillTextViewWithValueUnit(dataEntity, dataEntityWrapper,
                FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                binding.fromSegmentStartSumRealDeltaCumulativeValue);

        fillTextViewWithValueUnit(dataEntity, dataEntityWrapper,
                ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE,
                binding.allSumRealDeltaCumulativeValue);
    }

    private static void fillTextViewWithValueUnit(DataEntity dataEntity, DataEntityWrapper dataEntityWrapper, CumulativeProcessedDataType cumulativeProcessedDataType, TextView textView) {

        CumulativeStatistics cumulativeStatistics = dataEntityWrapper.getCumulativeStatistics(dataEntity, cumulativeProcessedDataType);

        float value = cumulativeStatistics.value();
        String unit = cumulativeStatistics.unit();

        String valueString =
                String.format(Locale.getDefault(), "%.2f", value);

        textView.setText(TextViewUtil.getSpannableStringBuilder(valueString, " " + unit));
    }

    private void setupTrendStatisticsLayout(TrendStatistics trendStatistics, String unitString) {
        ViewUtil.setVisibility(binding.trendStatisticsLayout, settings.isDrawAscDescSegEnabled());
        if (settings.isDrawAscDescSegEnabled()) {
            setupAscDescSegStatistics(trendStatistics, unitString);
        }
    }

    private void setupAscDescSegStatistics(TrendStatistics trendStatistics, String unitString) {

        TrendType trendType = trendStatistics.trendType();
        String text = "";
        switch (trendType) {
            case UP -> {
                text = "+ ";
            }
            case CONSTANT -> {
                text = "  ";
            }
            case DOWN -> {
                text = "- ";
            }
        }
        text +=
                String.format(Locale.getDefault(), "%.2f", trendStatistics.absDeltaVal());


        binding.markerTextViewDeltaValue.setText(TextViewUtil.getSpannableStringBuilder(text, " " + unitString));
        binding.markerTextViewDeltaValue.setBackgroundColor(ColorUtil.setAlphaInIntColor(trendType.getFillColor(), 128));

        String numberString =
                String.format(Locale.getDefault(), "%d.", trendStatistics.n());
        binding.markerTextViewNumber.setText(TextViewUtil.getSpannableStringBuilder(null, numberString));

        binding.trendTypeImageView.setImageResource(trendType.getDrawableId());
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        DataEntityLineChart chartView = (DataEntityLineChart) getChartView();

        ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
                .getLastGesture();


        RectF contentRect = getChartView().getViewPortHandler().getContentRect();

        MPPointF pointFCenter = getChartView().getViewPortHandler().getContentCenter();



/*		if (chartView.isFullyZoomedOut() ) {
			//drawMarker(posX, posY, canvas);
		} else {*/

        switch (chartGesture) {
            case ROTATE:
            case DOUBLE_TAP:
            case LONG_PRESS:
            case SINGLE_TAP: {

                drawMarker(posX, (contentRect.top + contentRect.bottom) * 0.25f, canvas);

                break;
            }
            case NONE:
            case X_ZOOM:
            case Y_ZOOM:
            case PINCH_ZOOM:
            case FLING:
            case DRAG:
                drawMarker(posX, (contentRect.top + contentRect.bottom) * 0.25f, canvas);
                break;
        }
        //}
    }

    private void drawMarker(float posX, float posY, Canvas canvas) {
        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);

        canvas.restoreToCount(saveId);
    }

    public void setSettings(final LineChartSettings settings) {
        this.settings = settings;
    }
}