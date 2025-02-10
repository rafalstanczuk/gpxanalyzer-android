package com.itservices.gpxanalyzer.ui.gpxchart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentGpxChartsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GpxChartsFragment extends Fragment {
    GpxChartsViewModel gpxChartsViewModel;
    FragmentGpxChartsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gpxChartsViewModel = new ViewModelProvider(this).get(GpxChartsViewModel.class);
        gpxChartsViewModel.setOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {

        binding = FragmentGpxChartsBinding.inflate(inflater, container, false);
        binding.setViewModel(gpxChartsViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.loadButton.setOnClickListener(view ->
                gpxChartsViewModel.loadData(requireContext(), R.raw.skiing20250121t091423)
        );

        bindAltitudeTimeChartUI();

        bindSpeedTimeChartUI();

        observeRequestStatus();

        return binding.getRoot();
    }

    private void observeRequestStatus() {
        gpxChartsViewModel.getRequestStatusLiveData().observe(getViewLifecycleOwner(), requestStatus -> {
            binding.loadButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
            binding.altitudeScaleControlLayout.timeAutoscalingButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
            binding.altitudeScaleControlLayout.timeZoomInButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
            binding.altitudeScaleControlLayout.timeZoomOutButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));

            binding.speedScaleControlLayout.timeAutoscalingButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
            binding.speedScaleControlLayout.timeZoomInButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
            binding.speedScaleControlLayout.timeZoomOutButton.setEnabled(gpxChartsViewModel.getButtonEnabled(requestStatus));
        });
    }

    private void bindSpeedTimeChartUI() {
        gpxChartsViewModel.bindSpeedTimeChart(binding.speedPropertiesControlLayout, binding.speedTimeLineChart, (MainActivity) requireActivity());
        binding.speedScaleControlLayout.timeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.speedTimeLineChart)
        );
        binding.speedScaleControlLayout.timeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.speedTimeLineChart)
        );
        binding.speedScaleControlLayout.timeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.speedTimeLineChart)
        );

        binding.speedPropertiesControlLayout.onOffColorizedCirclesCheckBox
                .setOnCheckedChangeListener((buttonView, isChecked) -> gpxChartsViewModel.setSpeedDrawIconEnabled(isChecked)
                );
    }

    private void bindAltitudeTimeChartUI() {
        gpxChartsViewModel.bindAltitudeTimeChart(binding.altitudePropertiesControlLayout, binding.altitudeTimeLineChart, (MainActivity) requireActivity());
        binding.altitudeScaleControlLayout.timeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.altitudeTimeLineChart)
        );
        binding.altitudeScaleControlLayout.timeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.altitudeTimeLineChart)
        );
        binding.altitudeScaleControlLayout.timeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.altitudeTimeLineChart)
        );

        binding.altitudePropertiesControlLayout.onOffColorizedCirclesCheckBox
                .setOnCheckedChangeListener((buttonView, isChecked) -> gpxChartsViewModel.setAltitudeDrawIconEnabled(isChecked)
                );
    }

    @Override
    public void onPause() {
        super.onPause();
        gpxChartsViewModel.onPause();
    }
}
