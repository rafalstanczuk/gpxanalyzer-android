package com.itservices.gpxanalyzer.logbook;

import static com.itservices.gpxanalyzer.logbook.ViewMode.TREND_CURVE;
import static com.itservices.gpxanalyzer.utils.ui.BindingAdapters.CHART_PERCENTAGE_HEIGHT_LANDSCAPE;
import static com.itservices.gpxanalyzer.utils.ui.BindingAdapters.CHART_PERCENTAGE_HEIGHT_PORTRAIT;
import static com.itservices.gpxanalyzer.utils.ui.BindingAdapters.DEFAULT_FLOAT_RELATIVE_PERCENT_VALUE;
import static com.itservices.gpxanalyzer.utils.ui.BindingAdapters.DEFAULT_MAX_100_PERCENT;

import android.content.Context;
import android.content.res.Configuration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.logbook.chart.data.DataProvider;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class LogbookViewModel extends ViewModel {

	@Inject
	DataProvider dataProvider;

	private final MutableLiveData<ViewMode> viewModeLiveData = new MutableLiveData<>(TREND_CURVE);

	private final MutableLiveData<RequestType> requestTypeLiveData = new MutableLiveData<>(RequestType.DEFAULT);

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

	@Inject
	public LogbookViewModel() {}

	public LiveData<ViewMode> getViewMode() {
		return viewModeLiveData;
	}

	public LiveData<RequestType> getRequestType() {
		return requestTypeLiveData;
	}

	public void switchViewMode() {
		ViewMode current = viewModeLiveData.getValue();
		if (current != null)
			viewModeLiveData.setValue( current.getNextCyclic() );
	}

	public boolean isTrendCurveMode() {
		return viewModeLiveData.getValue() != null && (viewModeLiveData.getValue() == TREND_CURVE);
	}

	public Observable<StatisticResults> loadData(Context requireContext, int rawId) {
		requestTypeLiveData.postValue(RequestType.LOADING);

		return dataProvider.provide(requireContext, rawId)
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(measurements -> {
					requestTypeLiveData.postValue(RequestType.PROCESSING);
					return new StatisticResults(measurements);
				}).map(statisticResults -> {
					requestTypeLiveData.postValue(RequestType.DONE);
					return statisticResults;
				});
	}
}
