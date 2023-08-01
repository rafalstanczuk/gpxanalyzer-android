package com.itservices.gpxanalyzer.logbook;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

	private final MutableLiveData<StatisticResults> _measurementStatisticResults = new MutableLiveData<>();

	private final MutableLiveData<StatisticResults> _curveMeasurementStatisticResults = new MutableLiveData<>();

	public LiveData<StatisticResults> getMeasurementStatisticResults() {
		return _measurementStatisticResults;
	}

	public LiveData<StatisticResults> getCurveMeasurementsStatisticResults() {
		return _curveMeasurementStatisticResults;
	}

	@Inject
	StatisticResults statisticResults;

	@Inject
	public StatisticsViewModel() {}

	public void refreshStatisticResults() {
		_curveMeasurementStatisticResults.setValue(statisticResults);
	}
}
