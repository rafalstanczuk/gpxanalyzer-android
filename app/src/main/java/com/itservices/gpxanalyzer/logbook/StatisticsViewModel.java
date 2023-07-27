package com.itservices.gpxanalyzer.logbook;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;



import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

	private final MutableLiveData<StatisticResults> _bgStatisticResults = new MutableLiveData<>();

	private final MutableLiveData<StatisticResults> _cgmsStatisticResults = new MutableLiveData<>();

	public LiveData<StatisticResults> getGlucoseStatisticResults() {
		return _bgStatisticResults;
	}

	public LiveData<StatisticResults> getCGMSStatisticResults() {
		return _cgmsStatisticResults;
	}

	@Inject
	StatisticResults statisticResults;

	@Inject
	public StatisticsViewModel() {}

	public void refreshStatisticResults() {



		_bgStatisticResults.setValue(statisticResults);


		/*_bgStatisticResults.setValue(results);
		_weightStatisticResults.setValue(weightResults);
		_bloodPressureStatisticResults.setValue(bloodPressureResults);
		_cgmsStatisticResults.setValue(cgmsResults);*/
	}
}
