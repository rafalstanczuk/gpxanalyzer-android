package com.itservices.gpxanalyzer.logbook;

import static com.itservices.gpxanalyzer.logbook.ViewMode.TREND_CURVE;

import android.content.res.Configuration;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LogbookViewModel extends ViewModel {

	private static final float CHART_PERCENTAGE_HEIGHT_LANDSCAPE = 70f;
	private static final float CHART_PERCENTAGE_HEIGHT_PORTRAIT = 50f;
	private static final float DEFAULT_MAX_100_PERCENT = 100f;
	private static final float DEFAULT_FLOAT_RELATIVE_PERCENT_VALUE = 1.0f;
	private final MutableLiveData<ViewMode> viewModeLiveData = new MutableLiveData<>(TREND_CURVE);

	public MutableLiveData<Float> chartPercentageHeightLiveData = new MutableLiveData<>(
		DEFAULT_MAX_100_PERCENT);

	public void setOrientation(int orientation) {
		chartPercentageHeightLiveData.setValue(
			(orientation == Configuration.ORIENTATION_LANDSCAPE) ? CHART_PERCENTAGE_HEIGHT_LANDSCAPE
				: CHART_PERCENTAGE_HEIGHT_PORTRAIT
		);
	}

	public float getMeasurementChartPercentageHeight() {
		return chartPercentageHeightLiveData.getValue()!=null
			? chartPercentageHeightLiveData.getValue() / DEFAULT_MAX_100_PERCENT : DEFAULT_FLOAT_RELATIVE_PERCENT_VALUE;
	}

	@BindingAdapter("layout_constraintHeight_percent")
	public static void setLayoutConstraintHeightPercent(View view, float percentHeight) {

		ConstraintLayout layout = (ConstraintLayout) view.getParent();
		ConstraintSet constraintSet = new ConstraintSet();
		constraintSet.clone(layout);
		constraintSet.constrainPercentHeight(view.getId(), percentHeight);
		constraintSet.applyTo(layout);
	}

	@Inject
	public LogbookViewModel() {}

	public LiveData<ViewMode> getViewMode() {
		return viewModeLiveData;
	}

	public void switchViewMode() {
		ViewMode current = viewModeLiveData.getValue();
		if (current != null)
			viewModeLiveData.setValue( current.getNextCyclic() );
	}

	public boolean isTrendCurveMode() {
		return viewModeLiveData.getValue() != null && (viewModeLiveData.getValue() == TREND_CURVE);
	}


}
