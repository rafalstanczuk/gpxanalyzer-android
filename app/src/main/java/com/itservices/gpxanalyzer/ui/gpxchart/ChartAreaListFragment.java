package com.itservices.gpxanalyzer.ui.gpxchart;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentChartAreaListBinding;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChartAreaListFragment extends Fragment {
    ChartAreaListViewModel viewModel;
    FragmentChartAreaListBinding binding;

    @Inject
    ChartAreaItemFactory chartAreaItemFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChartAreaListViewModel.class);
        viewModel.setOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {

        binding = FragmentChartAreaListBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.gpxChartsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ChartAreaItemAdapter adapter = new ChartAreaItemAdapter(Arrays.asList(
                chartAreaItemFactory.create(ViewMode.ASL_T_1,  false, false),
                chartAreaItemFactory.create(ViewMode.V_T_1,  true, false)
        ), viewModel);
        binding.gpxChartsRecyclerView.setAdapter(adapter);

        viewModel.setChartAreaItemList(adapter.getItems());

        binding.loadButton.setOnClickListener(view ->
                viewModel.loadData(requireActivity(), R.raw.skiing20250121t091423)
        );

        binding.switchSeverityModeButton.setOnClickListener(view -> viewModel.switchSeverityMode());



        viewModel.getOnSwitchViewModeChangedLiveData().observe(getViewLifecycleOwner(),
                (item -> {viewModel.switchViewMode(adapter, item, requireActivity());} ));

        viewModel.getOnOnOffColorizedCirclesCheckBoxChangedLiveData().observe(getViewLifecycleOwner(),
                (pair -> {viewModel.changeOnOffColorizedCircles(adapter, pair, requireActivity());} ));

        viewModel.getOnZoomInClickedLiveData().observe(getViewLifecycleOwner(),
                (item -> {viewModel.zoomIn(adapter, item, requireActivity());} ));

        viewModel.getOnZoomOutLiveData().observe(getViewLifecycleOwner(),
                (item -> {viewModel.zoomOut(adapter, item, requireActivity());} ));

        viewModel.getOnAutoScalingLiveData().observe(getViewLifecycleOwner(),
                (item -> {viewModel.autoScaling(adapter, item, requireActivity());} ));

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.onPause();
    }
}
