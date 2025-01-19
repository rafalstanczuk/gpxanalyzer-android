package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itservices.gpxanalyzer.R;

public class MeasurementInfoLayoutView extends LinearLayout {

	private TextView textViewTime;
	private TextView textViewValue;
	private TextView textViewValueUnit;

	public MeasurementInfoLayoutView(Context context) {
		super(context);
		initView(context);
	}

	public MeasurementInfoLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public MeasurementInfoLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public MeasurementInfoLayoutView(
		Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
	) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}

	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.measurement_chart_info_view, this, true);

		textViewTime = findViewById(R.id.textViewTime);
		textViewValue = findViewById(R.id.textViewValue);
		textViewValueUnit = findViewById(R.id.textViewValueUnit);
	}

	public void setTime(String time) {
		textViewTime.setText(time);
	}

	public void setValue(String value) {
		textViewValue.setText(value);
	}

	public void setValueUnit(String valueUnit) {
		textViewValueUnit.setText(valueUnit);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
}