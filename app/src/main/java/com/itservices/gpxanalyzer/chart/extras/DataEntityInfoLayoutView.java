package com.itservices.gpxanalyzer.chart.extras;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.itservices.gpxanalyzer.databinding.DataEntityChartInfoViewBinding;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class DataEntityInfoLayoutView extends LinearLayout {

	DataEntityChartInfoViewBinding binding;

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
/*		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.data_entity_chart_info_view, this, true);*/

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		binding = DataEntityChartInfoViewBinding.inflate(inflater, this, true);
	}

	public void setTime(String time) {
		binding.textViewTime.setText(time);
	}

	public void setValue1(String value) {
		binding.textViewValue1.setText(value);
	}

	public void setValue1Unit(String valueUnit) {
		binding.textViewValue1Unit.setText(valueUnit);
	}


}