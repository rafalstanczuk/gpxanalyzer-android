package com.itservices.gpxanalyzer.ui.gpxchart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentChartAreaListBinding;
import com.itservices.gpxanalyzer.databinding.TopBarLayoutBinding;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemAdapter;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItemFactory;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.storage.FileSelectorFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChartAreaListFragment extends Fragment {
    private static final String TAG = ChartAreaListFragment.class.getSimpleName();

    ChartAreaListViewModel viewModel;
    FragmentChartAreaListBinding binding;

    ChartAreaItemAdapter adapter;

    @Inject
    ChartAreaItemFactory chartAreaItemFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

        viewModel = new ViewModelProvider(this.requireActivity()).get(ChartAreaListViewModel.class);


        binding = FragmentChartAreaListBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        TopBarLayoutBinding topBarLayoutBinding = binding.topBarLayout;
        topBarLayoutBinding.setViewModel(viewModel);
        topBarLayoutBinding.setLifecycleOwner(getViewLifecycleOwner());

        binding.gpxChartsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (viewModel.getChartAreaItemListLiveData().getValue()==null || viewModel.getChartAreaItemListLiveData().getValue().isEmpty() ) {
            List<ChartAreaItem> immutableList = Arrays.asList(
                    chartAreaItemFactory.create(GpxViewMode.ASL_T_1, false, false),
                    chartAreaItemFactory.create(GpxViewMode.V_T_1, true, false)
            );
            List<ChartAreaItem> itemList = new ArrayList<>( immutableList );
            viewModel.setChartAreaItemList(itemList);
            viewModel.setDefaultChartAreaItemList(immutableList);
        } else {
            adapter = new ChartAreaItemAdapter(viewModel.getChartAreaItemListLiveData().getValue(), viewModel, getViewLifecycleOwner());
            binding.gpxChartsRecyclerView.setAdapter(adapter);
        }

        viewModel.setOrientation(getResources().getConfiguration().orientation);

        topBarLayoutBinding.loadButton.setOnClickListener(view ->
                viewModel.postEventLoadData()
        );

        topBarLayoutBinding.selectFileButton.setOnClickListener(view -> {
            FileSelectorFragment fileSelectorFragment = new FileSelectorFragment();
            fileSelectorFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialog);
            fileSelectorFragment.show(getChildFragmentManager(), FileSelectorFragment.class.getSimpleName());
        });

        topBarLayoutBinding.switchSeverityModeButton.setOnClickListener(view -> viewModel.switchSeverityMode());

        viewModel.getChartAreaItemListLiveData().observe(getViewLifecycleOwner(), items -> {
                    adapter = new ChartAreaItemAdapter(items, viewModel, getViewLifecycleOwner());
                    binding.gpxChartsRecyclerView.swapAdapter(adapter, false);
                }
        );

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called");
        super.onPause();

        binding.mapView.onPause();
        viewModel.dispose();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume() called");

        super.onResume();
        viewModel.bind();
        viewModel.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");

        super.onDetach();
        binding.mapView.onDetachedFromWindow();
        binding = null; // Prevent memory leaks
    }
}
