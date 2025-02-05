package com.itservices.gpxanalyzer.logbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentLogbookBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LogbookFragment extends Fragment {
    LogbookViewModel logbookViewModel;
    FragmentLogbookBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logbookViewModel = new ViewModelProvider(this).get(LogbookViewModel.class);
        logbookViewModel.setOrientation(getResources().getConfiguration().orientation);
    }

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
	) {

		binding = FragmentLogbookBinding.inflate(inflater, container, false);
		binding.setViewModel(logbookViewModel);
		binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.button.setOnClickListener(view ->
                logbookViewModel.loadData(requireContext(), R.raw.skiing20250121t091423)
        );

        logbookViewModel.bindHeightTimeChart(binding.heightTimeLineChart, (MainActivity) requireActivity());

        logbookViewModel.bindVelocityTimeChart(binding.velocityTimeLineChart, (MainActivity) requireActivity());

        logbookViewModel.getRequestStatusLiveData().observe(getViewLifecycleOwner(), requestStatus -> {
            binding.button.setEnabled( logbookViewModel.getButtonEnabled(requestStatus) );
        });

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        logbookViewModel.onPause();
    }
}
