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

        binding.secondChartPropertiesControlLayout.setViewModel(gpxChartsViewModel);
        binding.secondChartScaleControlLayout.setViewModel(gpxChartsViewModel);

        binding.firstChartPropertiesControlLayout.setViewModel(gpxChartsViewModel);
        binding.firstChartScaleControlLayout.setViewModel(gpxChartsViewModel);

        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.loadButton.setOnClickListener(view ->
                gpxChartsViewModel.loadData(requireContext(), R.raw.skiing20250121t091423)
        );

        binding.switchSeverityModeButton.setOnClickListener(view -> gpxChartsViewModel.switchSeverityMode() );

        bindFirstChartUI();

        bindSecondChartUI();

        return binding.getRoot();
    }

    private void bindSecondChartUI() {
        gpxChartsViewModel.bindSecondChart(binding.secondChartPropertiesControlLayout, binding.secondLineChart, (MainActivity) requireActivity());
        binding.secondChartScaleControlLayout.timeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.secondLineChart)
        );
        binding.secondChartScaleControlLayout.timeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.secondLineChart)
        );
        binding.secondChartScaleControlLayout.timeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.secondLineChart)
        );

        binding.secondChartPropertiesControlLayout.onOffColorizedCirclesCheckBox
                .setOnCheckedChangeListener((buttonView, isChecked) -> gpxChartsViewModel.setSecondChartDrawIconEnabled(requireActivity(), isChecked)
                );
    }

    private void bindFirstChartUI() {
        gpxChartsViewModel.bindFirstChart(binding.firstChartPropertiesControlLayout, binding.firstLineChart, (MainActivity) requireActivity());
        binding.firstChartScaleControlLayout.timeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.firstLineChart)
        );
        binding.firstChartScaleControlLayout.timeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.firstLineChart)
        );
        binding.firstChartScaleControlLayout.timeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.firstLineChart)
        );

        binding.firstChartPropertiesControlLayout.onOffColorizedCirclesCheckBox
                .setOnCheckedChangeListener((buttonView, isChecked) -> gpxChartsViewModel.setFirstChartDrawIconEnabled(requireActivity(), isChecked)
                );
    }

    @Override
    public void onPause() {
        super.onPause();
        gpxChartsViewModel.onPause();
    }
}
