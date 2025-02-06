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

        gpxChartsViewModel.bindHeightTimeChart(binding.heightTimeLineChart, (MainActivity) requireActivity());
        binding.heightTimeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.heightTimeLineChart)
        );

        gpxChartsViewModel.bindVelocityTimeChart(binding.velocityTimeLineChart, (MainActivity) requireActivity());
        binding.velocityTimeAutoscalingButton.setOnClickListener(view ->
                gpxChartsViewModel.resetTimeScale(binding.velocityTimeLineChart)
        );

        gpxChartsViewModel.getRequestStatusLiveData().observe(getViewLifecycleOwner(), requestStatus -> {
            binding.loadButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.heightTimeAutoscalingButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
            binding.velocityTimeAutoscalingButton.setEnabled( gpxChartsViewModel.getButtonEnabled(requestStatus) );
        });

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        gpxChartsViewModel.onPause();
    }
}
