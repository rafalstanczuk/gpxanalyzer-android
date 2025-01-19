package com.itservices.gpxanalyzer.fftspectrum;

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
import com.itservices.gpxanalyzer.audio.AudioViewModel;
import com.itservices.gpxanalyzer.databinding.FragmentFftspectrumBinding;
import com.itservices.gpxanalyzer.logbook.LogbookViewModel;
import com.itservices.gpxanalyzer.logbook.RequestType;
import com.itservices.gpxanalyzer.logbook.StatisticsViewModel;
import com.itservices.gpxanalyzer.logbook.ViewMode;
import com.itservices.gpxanalyzer.logbook.chart.ChartViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class FFTSpectrumFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {

    static final String TAG = FFTSpectrumFragment.class.getSimpleName();

    public StatisticsViewModel statisticsViewModel;
    public LogbookViewModel logbookViewModel;
    public ChartViewModel chartViewModel;

    public AudioViewModel audioViewModel;

    private MainActivity activity;
    private FragmentFftspectrumBinding binding;

    Disposable disposable = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        chartViewModel = new ViewModelProvider(this).get(ChartViewModel.class);
        logbookViewModel = new ViewModelProvider(this).get(LogbookViewModel.class);
        logbookViewModel.setOrientation(getResources().getConfiguration().orientation);
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setupObservers();
    }

    private void loadData() {
        disposable = logbookViewModel
                .loadData(requireContext(), R.raw.test20230729)
                .doOnError(e -> {
                })
                .subscribe(
                        statisticResults -> statisticsViewModel.refreshStatisticResults(statisticResults),
                        onError -> Log.e(TAG, "loadData: ", onError));
    }

    private void resetMeasurementCurveMarkerAndClearSelection() {
        if (logbookViewModel.isTrendCurveMode()) {
          //  chartViewModel.resetMarkerAndClearSelection(binding.lineChart);
        }
    }

    private void resetMeasurementCurveMarkerAndSaveSelection() {
        if (logbookViewModel.isTrendCurveMode()) {
           // chartViewModel.resetMarkerAndSaveSelection(binding.lineChart);
        }
    }


    private void switchToCurveView() {
        activity.runOnUiThread(() -> {
            initChart();

           // binding.lineChart.setOnChartValueSelectedListener(this);
           // binding.lineChart.setOnChartGestureListener(this);

            //binding.chartLayout.setVisibility(View.VISIBLE);

            binding.invalidateAll();
        });
    }

    private void setupObservers() {

        audioViewModel.getSpectrumPairListLiveData()
                .observe(getViewLifecycleOwner(), audioSpectrum -> {
                            Log.d(TAG, "audioSpectrum() :" + audioSpectrum);

                        }
                );

        audioViewModel.getAudioCaptureState()
                .observe(getViewLifecycleOwner(), audioCaptureState -> {
                            if (audioCaptureState != null) {
                                activity.runOnUiThread(() ->
                                        binding.button.setText(audioCaptureState.getNextCyclic().name())
                                );

                                switch (audioCaptureState) {

                                    case OFF:
                                        audioViewModel.stopRecording();
                                        break;
                                    case ON:
                                        audioViewModel.startRecording();
                                        break;
                                }

                            }
                        }
                );

        binding.button.setOnClickListener(view -> {
            audioViewModel.switchOnOff();
        });

        chartViewModel.getLineDataSetListToAddLive()
                .observe(getViewLifecycleOwner(), lineDataSetList -> {
                            activity.runOnUiThread(() -> {}
                                   // chartViewModel.tryToUpdateDataChart(binding.lineChart, lineDataSetList)
                            );
                        }
                );

        chartViewModel.getHighlightedEntry()
                .observe(getViewLifecycleOwner(), selectedEntry -> {

                   // binding.lineChart.setHighlightedEntry(activity, selectedEntry);
                });

        chartViewModel.getEntryToHighlightTimeInt()
                .observe(getViewLifecycleOwner(), selectedColumnTimeInt -> {
                   // chartViewModel.selectMarker(binding.lineChart, selectedColumnTimeInt);
                });

        statisticsViewModel.getCurveMeasurementsStatisticResults()
                .observe(getViewLifecycleOwner(), curveMeasurementStatisticResults ->
                        chartViewModel.updateCurveMeasurementLineDataSetFrom(curveMeasurementStatisticResults)
                );

        statisticsViewModel.getMeasurementStatisticResults()
                .observe(getViewLifecycleOwner(), measurementStatisticResults ->
                        chartViewModel.updateSingleMeasurementDataSetFrom(measurementStatisticResults)
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
        binding = FragmentFftspectrumBinding.inflate(inflater);
        binding.setViewModel(logbookViewModel);

        logbookViewModel.getViewMode().observe(getViewLifecycleOwner(), this::switchViewMode);

        logbookViewModel.getRequestType().observe(getViewLifecycleOwner(), this::switchRequestType);

        return binding.getRoot();
    }

    private void switchRequestType(RequestType requestType) {
        activity.runOnUiThread(() -> {
            switch (requestType) {
                case DEFAULT:
                case DONE:
                    binding.button.setEnabled(true);
                    break;
                case LOADING:
                case PROCESSING:
                    binding.button.setEnabled(false);
                    break;
            }
        });
    }

    private void tryToDispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void switchViewMode(ViewMode viewMode) {

        switch (viewMode) {
            case TREND_CURVE:
                switchToCurveView();
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
       // binding.lineChart.highlightCenterValueInTranslation();
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
            //chartViewModel.init(binding.lineChart);
        });
    }
}

/*

public class MainActivity extends AppCompatActivity {
    private AudioCapture audioCapture;
    private SpectrumView spectrumView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spectrumView = findViewById(R.id.spectrumView);
        audioCapture = new AudioCapture();
    }

    public void start(View view) {
        audioCapture.startRecording();
    }

    public void stop(View view) {
        audioCapture.stopRecording();
    }
}
 */