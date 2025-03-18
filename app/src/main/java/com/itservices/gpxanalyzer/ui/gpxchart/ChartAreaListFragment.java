package com.itservices.gpxanalyzer.ui.gpxchart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentChartAreaListBinding;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemAdapter;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemFactory;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.ViewMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChartAreaListFragment extends Fragment {
    ChartAreaListViewModel viewModel;
    FragmentChartAreaListBinding binding;

    ChartAreaItemAdapter adapter;

    @Inject
    ChartAreaItemFactory chartAreaItemFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this.requireActivity()).get(ChartAreaListViewModel.class);
        viewModel.bind(requireContext(), R.raw.skiing20250121t091423);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {

        binding = FragmentChartAreaListBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        binding.gpxChartsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        if (viewModel.getChartAreaItemListLiveData().getValue()==null || viewModel.getChartAreaItemListLiveData().getValue().isEmpty() ) {
            List<ChartAreaItem> immutableList = Arrays.asList(
                    chartAreaItemFactory.create(ViewMode.ASL_T_1, false, false),
                    chartAreaItemFactory.create(ViewMode.V_T_1, true, false)
            );
            List<ChartAreaItem> itemList = new ArrayList<>( immutableList );
            viewModel.setChartAreaItemList(itemList);
            viewModel.setDefaultChartAreaItemList(immutableList);
        } else {
            adapter = new ChartAreaItemAdapter(viewModel.getChartAreaItemListLiveData().getValue(), viewModel, getViewLifecycleOwner());
            binding.gpxChartsRecyclerView.setAdapter(adapter);
        }

        viewModel.setOrientation(getResources().getConfiguration().orientation);

        binding.loadButton.setOnClickListener(view ->
                viewModel.postEventLoadData()
        );

        binding.switchSeverityModeButton.setOnClickListener(view -> viewModel.switchSeverityMode());

        viewModel.getChartAreaItemListLiveData().observe(getViewLifecycleOwner(), items -> {
                    adapter = new ChartAreaItemAdapter(items, viewModel, getViewLifecycleOwner());
                    binding.gpxChartsRecyclerView.swapAdapter(adapter, false);
                }
        );

        viewModel.getOnSwitchViewModeChangedLiveData().observe(getViewLifecycleOwner(),
                (item -> {
                    viewModel.switchViewMode(adapter, item);
                }));

        viewModel.getOnOnOffColorizedCirclesCheckBoxChangedLiveData().observe(getViewLifecycleOwner(),
                (pair -> {
                    viewModel.changeOnOffColorizedCircles(adapter, pair, requireActivity());
                }));

        viewModel.getOnZoomInClickedLiveData().observe(getViewLifecycleOwner(),
                (item -> {
                    viewModel.zoomIn(adapter, item, requireActivity());
                }));

        viewModel.getOnZoomOutLiveData().observe(getViewLifecycleOwner(),
                (item -> {
                    viewModel.zoomOut(adapter, item, requireActivity());
                }));

        viewModel.getOnAutoScalingLiveData().observe(getViewLifecycleOwner(),
                (item -> {
                    viewModel.autoScaling(adapter, item, requireActivity());
                }));

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.onPause();
    }
}
