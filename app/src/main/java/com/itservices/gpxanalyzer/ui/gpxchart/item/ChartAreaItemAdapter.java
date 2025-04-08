package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemBinding;
import com.itservices.gpxanalyzer.ui.gpxchart.ChartAreaListViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter for displaying ChartAreaItems.
 * This adapter manages the presentation of chart areas in a RecyclerView,
 * handling the creation and binding of view holders for each chart item.
 * It uses data binding to connect the UI components with their corresponding
 * data models and view models.
 */
public class ChartAreaItemAdapter extends RecyclerView.Adapter<ChartAreaItemAdapter.ChartAreaItemViewHolder> {
    private static final String TAG = ChartAreaItemAdapter.class.getSimpleName();

    private final List<ChartAreaItem> chartAreaItems;
    private final ChartAreaListViewModel viewModel;
    private final LifecycleOwner viewLifecycleOwner;
    private final Map<Integer, SpeedDialFabHelperZoom> speedDialZoomMap = new HashMap<>();
    private final Map<Integer, SpeedDialFabHelperChartSettings> speedDialSettingsMap = new HashMap<>();

    /**
     * Creates a new ChartAreaItemAdapter.
     *
     * @param chartAreaItems     The list of chart items to display
     * @param viewModel          The view model providing business logic for the chart list
     * @param viewLifecycleOwner The lifecycle owner for observing LiveData
     */
    public ChartAreaItemAdapter(List<ChartAreaItem> chartAreaItems, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
        this.chartAreaItems = chartAreaItems;
        this.viewModel = viewModel;
        this.viewLifecycleOwner = viewLifecycleOwner;

        AdapterDataObserverImpl adapterDataObserver = new AdapterDataObserverImpl(chartAreaItems);
        registerAdapterDataObserver(adapterDataObserver);

        //viewModel.setItemsChangedPublisher(adapterDataObserver.getChangedItems());
    }

    /**
     * Creates a new view holder when needed by the RecyclerView.
     *
     * @param parent   The parent ViewGroup
     * @param viewType The type of view
     * @return A new ChartAreaItemViewHolder
     */
    @NonNull
    @Override
    public ChartAreaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ChartAreaItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.chart_area_item, parent, false);
        return new ChartAreaItemViewHolder(binding);
    }

    /**
     * Binds data to an existing view holder.
     *
     * @param holder   The view holder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ChartAreaItemViewHolder holder, int position) {
        try {
            if (holder.binding != null) {
                holder.binding.chartAreaItemScaleControlLayout.settingsSpeedDialFabZoom.closeMenu();
                holder.binding.chartAreaItemPropertiesControlLayout.settingsSpeedDialFabChartSettings.closeMenu();

                ChartSlot chartSlot = ChartSlot.fromPosition(position);

                ChartAreaItem item = chartAreaItems.get(position);
                item.setChartSlot(chartSlot);
                holder.bind(item, viewModel, viewLifecycleOwner);

                configureSpeedDialFabZoom(holder.binding, position, item);
                configureSpeedDialFabSettings(holder.binding, position, item);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
    }

    private void configureSpeedDialFabZoom(@NonNull ChartAreaItemBinding binding, int position, ChartAreaItem item) {

        // Create and configure the SpeedDialFabView helper
        SpeedDialFabHelperZoom helper = new SpeedDialFabHelperZoom();

        helper.configureSpeedDialFab(binding.chartAreaItemScaleControlLayout, item);

        // Store the helper for later cleanup
        speedDialZoomMap.put(position, helper);

        Log.d(TAG, "Configured SpeedDialFabView for position " + position);
    }

    private void configureSpeedDialFabSettings(@NonNull ChartAreaItemBinding binding, int position, ChartAreaItem item) {

        // Create and configure the SpeedDialFabView helper
        SpeedDialFabHelperChartSettings helperSettings = new SpeedDialFabHelperChartSettings();

        helperSettings.configureSpeedDialFab(binding.chartAreaItemPropertiesControlLayout, item);

        // Store the helper for later cleanup
        speedDialSettingsMap.put(position, helperSettings);

        Log.d(TAG, "Configured SpeedDialFabView for position " + position);
    }

    /**
     * Gets the total number of items in the data set.
     *
     * @return The number of chart items
     */
    @Override
    public int getItemCount() {
        return chartAreaItems.size();
    }

    /**
     * Gets the list of chart items managed by this adapter.
     *
     * @return The list of ChartAreaItems
     */
    public List<ChartAreaItem> getItems() {
        return chartAreaItems;
    }

    /**
     * Called when a view holder is recycled.
     * Cleans up resources associated with the view holder.
     *
     * @param holder The view holder being recycled
     */
    @Override
    public void onViewRecycled(@NonNull ChartAreaItemViewHolder holder) {
        super.onViewRecycled(holder);

        // First ensure the SpeedDialFabView is closed
        if (holder.binding != null) {
            // Force the SpeedDialFabView to close menu when recycled
            holder.binding.chartAreaItemScaleControlLayout.settingsSpeedDialFabZoom.closeMenu();
            holder.binding.chartAreaItemPropertiesControlLayout.settingsSpeedDialFabChartSettings.closeMenu();
        }

        // Then dispose of helper resources
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            Log.d(TAG, "Recycling view at position: " + position);
            recycleSpeedDialFabZoom(position);
            recycleSpeedDialFabSettings(position);
        }
    }

    private void recycleSpeedDialFabZoom(int position) {
        SpeedDialFabHelperZoom helper = speedDialZoomMap.get(position);
        if (helper != null) {
            speedDialZoomMap.remove(position);
        }
    }
    private void recycleSpeedDialFabSettings(int position) {
        SpeedDialFabHelperChartSettings helper = speedDialSettingsMap.get(position);
        if (helper != null) {
            speedDialSettingsMap.remove(position);
        }
    }

    /**
     * Called when the adapter is detached from the RecyclerView.
     * Cleans up all resources.
     *
     * @param recyclerView The RecyclerView the adapter was attached to
     */
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        speedDialZoomMap.clear();
        speedDialSettingsMap.clear();
    }

    /**
     * View holder for chart area items.
     * This inner class manages the view for a single chart area,
     * handling the binding of data to the view elements.
     */
    public static class ChartAreaItemViewHolder extends RecyclerView.ViewHolder {
        final ChartAreaItemBinding binding;

        /**
         * Creates a new ChartAreaItemViewHolder.
         *
         * @param binding The data binding for the chart area item view
         */
        ChartAreaItemViewHolder(@NonNull ChartAreaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a chart area item to this view holder.
         * Connects the chart controller to the chart view and sets up data binding
         * for all components in the chart area layout.
         *
         * @param item               The chart area item to bind
         * @param viewModel          The view model providing business logic
         * @param viewLifecycleOwner The lifecycle owner for observing LiveData
         */
        void bind(ChartAreaItem item, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
            binding.lineChart.setChartSlot(item.getChartSlot());
            item.getChartController().bindChart(binding.lineChart);

            binding.chartAreaItemPropertiesControlLayout.setLifecycleOwner(viewLifecycleOwner);
            binding.chartAreaItemPropertiesControlLayout.setViewModel(viewModel);
            binding.chartAreaItemPropertiesControlLayout.setChartAreaItem(item);

            binding.chartAreaItemScaleControlLayout.setLifecycleOwner(viewLifecycleOwner);
            binding.chartAreaItemScaleControlLayout.setViewModel(viewModel);
            binding.chartAreaItemScaleControlLayout.setChartAreaItem(item);

            binding.setLifecycleOwner(viewLifecycleOwner);
            binding.setViewModel(viewModel);
            binding.setChartAreaItem(item);

            binding.executePendingBindings();
        }
    }
}