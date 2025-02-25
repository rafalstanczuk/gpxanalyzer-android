package com.itservices.gpxanalyzer.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itservices.gpxanalyzer.R;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class DataEntityInfoLayoutView extends LinearLayout {

	private TextView textViewTime;
	private TextView textViewValue1;
	private TextView textViewValue1Unit;

	@Inject
	public DataEntityInfoLayoutView(@ApplicationContext Context context) {
		super(context);
		inflateView(context);
	}

	public DataEntityInfoLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflateView(context);
	}

	public DataEntityInfoLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflateView(context);
	}

	public DataEntityInfoLayoutView(
		Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes
	) {
		super(context, attrs, defStyleAttr, defStyleRes);
		inflateView(context);
	}

	private void inflateView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.data_entity_chart_info_view, this, true);

		textViewTime = findViewById(R.id.textViewTime);
		textViewValue1 = findViewById(R.id.textViewValue1);
		textViewValue1Unit = findViewById(R.id.textViewValue1Unit);
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

/*	public void setValue2(String value) {
		textViewValue2.setText(value);
	}

	public void setValue2Unit(String valueUnit) {
		textViewValue2Unit.setText(valueUnit);
	}*/
}