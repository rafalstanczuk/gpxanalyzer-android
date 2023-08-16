package com.itservices.gpxanalyzer.logbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentLogbookBinding;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.Disposable;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Extensions;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

import com.itservices.gpxanalyzer.logbook.chart.ChartViewModel;
import com.itservices.gpxanalyzer.logbook.chart.data.Measurement;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

@AndroidEntryPoint
public class LogbookFragment  extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {

	static final String TAG = LogbookFragment.class.getSimpleName();

	public StatisticsViewModel statisticsViewModel;
	public LogbookViewModel logbookViewModel;
	public ChartViewModel chartViewModel;


	@Inject
	public GPXParser mParser;

	@Inject
    StatisticResults statisticResults;

	private MainActivity activity;
	private FragmentLogbookBinding binding;

	@Nullable
	private Disposable disposableFoodTable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
		chartViewModel = new ViewModelProvider(this).get(ChartViewModel.class);
		logbookViewModel = new ViewModelProvider(this).get(LogbookViewModel.class);
		logbookViewModel.setOrientation(getResources().getConfiguration().orientation);
	}

	@Override
	public void onViewCreated(
		@NonNull View view, @Nullable Bundle savedInstanceState
	) {
		super.onViewCreated(view, savedInstanceState);

		setupObservers();


		Gpx parsedGpx = null;
		try {
			//InputStream in = getAssets().open("test20230719.gpx");
			InputStream in = requireContext().getResources().openRawResource(R.raw.test20230719);
			parsedGpx = mParser.parse(in); // consider doing this on a background thread
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		}

		if (parsedGpx != null) {
			// log stuff
			List<Track> tracks = parsedGpx.getTracks();

			statisticResults.clear();

			for (int i = 0; i < tracks.size(); i++) {

				Track track = tracks.get(i);
				//Log.d(TAG, "track " + i + ":");
				List<TrackSegment> segments = track.getTrackSegments();
				for (int j = 0; j < segments.size(); j++) {
					TrackSegment segment = segments.get(j);
					//Log.d(TAG, "  segment " + j + ":");
					for (TrackPoint trackPoint : segment.getTrackPoints()) {
						/*String msg = "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude()
								+ ", elev " + trackPoint.getElevation()
								+ ", time " + trackPoint.getTime();*/
						Extensions ext = trackPoint.getExtensions();
						Double speed;
						if (ext != null) {
							speed = ext.getSpeed();
							//msg = msg.concat(", speed " + speed);
						}
						//Log.d(TAG, msg);




						Measurement measurement = new Measurement();

						measurement.measurement = trackPoint.getElevation();

						Calendar calendar = Calendar.getInstance();
						calendar.setTime(trackPoint.getTime().toDate());

						measurement.timestamp = calendar;

						statisticResults.addMeasurements(measurement);
					}
				}
			}

			statisticResults.compute();
		} else {
			Log.e(TAG, "Error parsing gpx track!");
		}


		statisticsViewModel.refreshStatisticResults();


	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	private void resetMeasurementCurveMarkerAndClearSelection() {
		if (logbookViewModel.isTrendCurveMode()) {
			chartViewModel.resetMarkerAndClearSelection(binding.lineChart);
		}
	}

	private void resetMeasurementCurveMarkerAndSaveSelection() {
		if (logbookViewModel.isTrendCurveMode()) {
			chartViewModel.resetMarkerAndSaveSelection(binding.lineChart);
		}
	}


	private void switchToCGMCurveView() {
		activity.runOnUiThread(() -> {
			initChart();

			binding.lineChart.setOnChartValueSelectedListener(this);
			binding.lineChart.setOnChartGestureListener(this);

			binding.chartLayout.setVisibility(View.VISIBLE);

			binding.invalidateAll();
		});
	}

	private void setupObservers() {


		chartViewModel.getLineDataSetListToAddLive()
			.observe(getViewLifecycleOwner(), lineDataSetList -> {
					activity.runOnUiThread(() ->
						chartViewModel.tryToUpdateDataChart(binding.lineChart, lineDataSetList));
				}
			);

		chartViewModel.getHighlightedEntry()
			.observe(getViewLifecycleOwner(), selectedEntry -> {

				binding.lineChart.setHighlightedEntry(activity, selectedEntry);
			});

		chartViewModel.getEntryToHighlightTimeInt()
			.observe(getViewLifecycleOwner(), selectedColumnTimeInt -> {
				chartViewModel.selectMarker(binding.lineChart, selectedColumnTimeInt);
			});

		statisticsViewModel.getCurveMeasurementsStatisticResults()
			.observe(getViewLifecycleOwner(), curveMeasurementStatisticResults ->
				chartViewModel.updateCurveMeasurementLineDataSetFrom(
					requireContext(), curveMeasurementStatisticResults)
			);

		statisticsViewModel.getMeasurementStatisticResults()
			.observe(getViewLifecycleOwner(), measurementStatisticResults ->
				chartViewModel.updateSingleMeasurementDataSetFrom(
					requireContext(), measurementStatisticResults)
			);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {


		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(
		@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
	) {
		activity = (MainActivity) requireActivity();
		binding = FragmentLogbookBinding.inflate(inflater);
		binding.setViewModel(logbookViewModel);


		//statisticResults.initStat();

		logbookViewModel.getViewMode().observe(getViewLifecycleOwner(), viewMode -> {
			switchViewMode(viewMode);
		});

		return binding.getRoot();
	}

	private void tryToDispose(Disposable disposable) {
		if (disposable != null) {
			disposable.dispose();
		}
	}

	private void switchViewMode(ViewMode viewMode) {

		switch (viewMode) {
			case TREND_CURVE:
				switchToCGMCurveView();
				break;
			case INFO_ONLY_VIEW:

				break;
		}
	}

	@Override
	public void onChartGestureStart(
		MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture
	) {

	}

	@Override
	public void onChartGestureEnd(
		MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture
	) {

	}

	@Override
	public void onChartLongPressed(MotionEvent me) {

	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {

	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {

	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
		binding.lineChart.highlightCenterValueInTranslation();
	}

	@Override
	public void onValueSelected(Entry e, Highlight h) {
		chartViewModel.setSelectionEntry(e);
		chartViewModel.setSelectionHighlight(h);
	}

	@Override
	public void onNothingSelected() {
		resetMeasurementCurveMarkerAndClearSelection();
	}

	public void initChart() {
		activity.runOnUiThread(() -> {
			chartViewModel.init(binding.lineChart);
		});
	}
}
