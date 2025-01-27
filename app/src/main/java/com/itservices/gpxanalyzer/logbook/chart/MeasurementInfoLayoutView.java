package com.itservices.gpxanalyzer.logbook.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itservices.gpxanalyzer.R;

public class MeasurementInfoLayoutView extends LinearLayout {

	private TextView textViewTime;
	private TextView textViewValue1;
	private TextView textViewValue1Unit;

	private TextView textViewValue2;
	private TextView textViewValue2Unit;

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
		textViewValue1 = findViewById(R.id.textViewValue1);
		textViewValue1Unit = findViewById(R.id.textViewValue1Unit);

		textViewValue2 = findViewById(R.id.textViewValue2);
		textViewValue2Unit = findViewById(R.id.textViewValue2Unit);
	}

	public void setTime(String time) {
		textViewTime.setText(time);
	}

	public void setValue1(String value) {
		textViewValue1.setText(value);
	}

	public void setValue1Unit(String valueUnit) {
		textViewValue1Unit.setText(valueUnit);
	}

	public void setValue2(String value) {
		textViewValue2.setText(value);
	}

	public void setValue2Unit(String valueUnit) {
		textViewValue2Unit.setText(valueUnit);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
}