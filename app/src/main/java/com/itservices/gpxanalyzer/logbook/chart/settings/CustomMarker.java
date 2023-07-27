package com.itservices.gpxanalyzer.logbook.chart.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.logbook.chart.MeasurementCurveLineChart;
import com.itservices.gpxanalyzer.logbook.chart.entry.CurveMeasurementEntry;
import com.itservices.gpxanalyzer.logbook.chart.entry.SingleMeasurementMeasurementEntry;

import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
@SuppressLint("ViewConstructor")
public class CustomMarker extends MarkerView {

	private static final int layoutResource = R.layout.custom_marker_view;

	private final TextView markerTextViewTime;
	private final TextView markerTextViewValue;

	private Entry currentEntry = null;

	@Inject
	public CustomMarker(@ApplicationContext Context context) {
		super(context, layoutResource);

		markerTextViewTime = findViewById(R.id.markerTextViewTime);
		markerTextViewValue = findViewById(R.id.markerTextViewValue);
	}

	public static String formatTime(@NonNull Calendar calendar) {
		int min = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		String time;

		if (min < 10) {
			time = hour + ":0" + min;
		} else {
			time = hour + ":" + min;
		}

		return time;
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
		return new MPPointF(getHeight() * 0.1f, getHeight() * 0.1f);
	}

	@Override
	public void refreshContent(Entry entry, Highlight highlight) {
		MeasurementCurveLineChart chartView = (MeasurementCurveLineChart) getChartView();
		currentEntry = entry;

		ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
			.getLastGesture();

		if (entry instanceof CurveMeasurementEntry) {
			CurveMeasurementEntry curveMeasurementEntry = (CurveMeasurementEntry) entry;

			Calendar calendar = curveMeasurementEntry.getCalendar();

			/*String timeText = "\u231A ";
			String valueString = "\uD83D\uDCA7 " ;*/

			String unit = "[A]";

			SpannableStringBuilder timeLine = getSpannableStringBuilder(formatTime(calendar), " h");
			SpannableStringBuilder valueLine = getSpannableStringBuilder(
				String.valueOf((int) curveMeasurementEntry.getY()), " " + unit);

			markerTextViewTime.setText(timeLine, TextView.BufferType.SPANNABLE);
			markerTextViewValue.setText(valueLine, TextView.BufferType.SPANNABLE);
		} else if (entry instanceof SingleMeasurementMeasurementEntry) {

			SingleMeasurementMeasurementEntry singleMeasurementMeasurementEntry = (SingleMeasurementMeasurementEntry) entry;

			Calendar calendar = singleMeasurementMeasurementEntry.getCalendar();

			String unit = "[A]";

			SpannableStringBuilder timeLine = getSpannableStringBuilder(formatTime(calendar), " h");
			SpannableStringBuilder valueLine = getSpannableStringBuilder(
				String.valueOf((int) singleMeasurementMeasurementEntry.getY()), " " + unit);

			markerTextViewTime.setText(timeLine, TextView.BufferType.SPANNABLE);
			markerTextViewValue.setText(valueLine, TextView.BufferType.SPANNABLE);
		}

		super.refreshContent(entry, highlight);
	}

	@Override
	public void draw(Canvas canvas, float posX, float posY) {
		MeasurementCurveLineChart chartView = (MeasurementCurveLineChart) getChartView();

		ChartTouchListener.ChartGesture chartGesture = chartView.getChartTouchListener()
			.getLastGesture();

/*		if (chartView.isFullyZoomedOut() ) {
			//drawMarker(posX, posY, canvas);
		} else {*/

		switch (chartGesture) {
			case NONE:
			case ROTATE:
			case DOUBLE_TAP:
			case LONG_PRESS:
			case SINGLE_TAP: {

				drawMarker(posX, posY, canvas);

				break;
			}
			case X_ZOOM:
			case Y_ZOOM:
			case PINCH_ZOOM:
			case FLING:
			case DRAG:
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