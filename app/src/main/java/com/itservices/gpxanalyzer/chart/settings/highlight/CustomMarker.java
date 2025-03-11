package com.itservices.gpxanalyzer.chart.settings.highlight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.entry.CurveEntry;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.TrendType;
import com.itservices.gpxanalyzer.databinding.CustomMarkerViewBinding;
import com.itservices.gpxanalyzer.utils.common.FormatNumberUtil;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

@SuppressLint("ViewConstructor")
public class CustomMarker extends MarkerView {

    private static final int layoutResource = R.layout.custom_marker_view;

    CustomMarkerViewBinding binding;

    @Inject
    public CustomMarker(@ApplicationContext Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        binding = CustomMarkerViewBinding.inflate(inflater, this, true);

    }

    @NonNull
    private SpannableStringBuilder getSpannableStringBuilder(String value, String postFixText) {
        SpannableStringBuilder valueLine = new SpannableStringBuilder();

        SpannableString valueSpannable = new SpannableString(value);
        valueLine.append(valueSpannable);

        SpannableString labelSpannable = new SpannableString(postFixText);
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        labelSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, labelSpannable.length(), flag);

        valueLine.append(labelSpannable);

        return valueLine;
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

            SpannableStringBuilder timeLine = getSpannableStringBuilder(
                    FormatNumberUtil.getFormattedTime(dataEntity.timestampMillis()), " h"
            );

            String unitString = dataEntity.unitList().get(curveDataEntityEntry.getStatisticResults().getPrimaryDataIndex());

            SpannableStringBuilder valueLine = getSpannableStringBuilder(
                    String.valueOf((int) curveDataEntityEntry.getY()), " " + unitString);

            binding.markerTextViewTime.setText(timeLine, TextView.BufferType.SPANNABLE);
            binding.markerTextViewValue.setText(valueLine, TextView.BufferType.SPANNABLE);

            TrendType trendType = curveDataEntityEntry.getTrendBoundaryDataEntity().trendStatistics().trendType();

            String text = "";
            switch (trendType) {
                case UP -> {
                    text = "+";
                }
                case CONSTANT -> {
                    text = "";
                }
                case DOWN -> {
                    text = "-";
                }
            }
            text += curveDataEntityEntry.getTrendBoundaryDataEntity().trendStatistics().deltaVal();


            binding.markerTextViewDeltaAsl.setText(getSpannableStringBuilder(text, " " + unitString));
            binding.markerTextViewDeltaAsl.setBackgroundColor(ColorUtil.setAlphaInIntColor(trendType.getFillColor(), 128));

            binding.trendTypeImageView.setImageResource(trendType.getDrawableId());
        }

        super.refreshContent(entry, highlight);
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
}