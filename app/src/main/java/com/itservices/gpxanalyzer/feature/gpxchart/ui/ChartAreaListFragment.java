package com.itservices.gpxanalyzer.feature.gpxchart.ui;

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
import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItem;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItemAdapter;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItemFactory;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.feature.gpxlist.ui.FileSelectorFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link Fragment} that displays a list of GPX chart areas ({@link ChartAreaItem}) in a RecyclerView.
 * It interacts with {@link ChartAreaListViewModel} to manage the chart data, state, and user actions.
 * The layout includes a top bar with controls for data loading, file selection, and other chart-related actions.
 * It also manages the lifecycle of an embedded MapView used for miniatures or previews.
 */
@AndroidEntryPoint
public class ChartAreaListFragment extends Fragment {
    private static final String TAG = ChartAreaListFragment.class.getSimpleName();

    /** The ViewModel associated with this Fragment, shared with the Activity. */
    ChartAreaListViewModel viewModel;
    /** View binding instance for this fragment's layout (fragment_chart_area_list.xml). */
    FragmentChartAreaListBinding binding;

    /** Adapter for the RecyclerView displaying the chart area items. */
    ChartAreaItemAdapter adapter;

    /** Factory for creating instances of {@link ChartAreaItem}. Injected by Hilt. */
    @Inject
    ChartAreaItemFactory chartAreaItemFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this.requireActivity()).get(ChartAreaListViewModel.class);
    }

    /**
     * Inflates the layout, initializes ViewModel, binds data, sets up RecyclerView, and configures UI listeners.
     * If the ViewModel does not already contain a list of chart items, it initializes a default list
     * (e.g., Altitude vs. Time, Velocity vs. Time).
     * Sets up observers for LiveData from the ViewModel to update the RecyclerView adapter.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

        viewModel.setOrientation(getResources().getConfiguration().orientation);

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
            viewModel.setDefaultChartAreaItemList(immutableList);
            viewModel.setChartAreaItemList(itemList);
        } else {
            adapter = new ChartAreaItemAdapter(viewModel.getChartAreaItemListLiveData().getValue(), viewModel, getViewLifecycleOwner());
            binding.gpxChartsRecyclerView.setAdapter(adapter);
        }

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

    /**
     * Pauses the embedded MapView and instructs the ViewModel to dispose of its resources (e.g., RxJava subscriptions).
     */
    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called");
        super.onPause();

        binding.mapView.onPause();
        viewModel.dispose();
    }

    /**
     * Resumes the embedded MapView and instructs the ViewModel to re-bind and handle resume logic.
     */
    @Override
    public void onResume() {
        Log.d(TAG, "onResume() called");

        super.onResume();
        viewModel.bind();
        viewModel.onResume();
        binding.mapView.onResume();
    }

    /**
     * Handles detachment, ensuring the embedded MapView is properly cleaned up and the ViewBinding reference is cleared
     * to prevent memory leaks.
     */
    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");

        super.onDetach();
        binding.mapView.onDetachedFromWindow();
        binding = null; // Prevent memory leaks
    }
}
