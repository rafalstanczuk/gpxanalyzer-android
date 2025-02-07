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

        bindHeightTimeChartUI();

        bindVelocityTimeChartUI();

        observeRequestStatus();

        return binding.getRoot();
    }

    private void observeRequestStatus() {
        gpxChartsViewModel.getRequestStatusLiveData().observe(getViewLifecycleOwner(), requestStatus -> {
            binding.loadButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.heightTimeAutoscalingButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.heightTimeZoomInButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.heightTimeZoomOutButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );

            binding.velocityTimeAutoscalingButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.velocityTimeZoomInButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.velocityTimeZoomOutButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
        });
    }

    private void bindVelocityTimeChartUI() {
        gpxChartsViewModel.bindVelocityTimeChart(binding.velocityTimeLineChart, (MainActivity) requireActivity());
        binding.velocityTimeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.velocityTimeLineChart)
        );
        binding.velocityTimeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.velocityTimeLineChart)
        );
        binding.velocityTimeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.velocityTimeLineChart)
        );
    }

    private void bindHeightTimeChartUI() {
        gpxChartsViewModel.bindHeightTimeChart(binding.heightTimeLineChart, (MainActivity) requireActivity());
        binding.heightTimeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.heightTimeLineChart)
        );
        binding.heightTimeZoomInButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomIn(binding.heightTimeLineChart)
        );
        binding.heightTimeZoomOutButton.setOnClickListener(view ->
                gpxChartsViewModel.zoomOut(binding.heightTimeLineChart)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        gpxChartsViewModel.onPause();
    }
}
