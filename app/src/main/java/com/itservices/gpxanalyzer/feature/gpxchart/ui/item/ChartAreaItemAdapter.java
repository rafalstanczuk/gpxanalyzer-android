package com.itservices.gpxanalyzer.feature.gpxchart.ui.item;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.itservices.gpxanalyzer.core.data.cache.processed.chart.ChartSlot;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemBinding;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.ChartAreaListViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} for displaying {@link ChartAreaItem} objects.
 * This adapter manages the creation and binding of ViewHolders for each chart area,
 * using Data Binding (`ChartAreaItemBinding`) to connect UI components with the
 * {@link ChartAreaItem} data model and the {@link ChartAreaListViewModel}.
 * It also handles the setup and cleanup of associated helper classes for Speed Dial FABs.
 */
public class ChartAreaItemAdapter extends RecyclerView.Adapter<ChartAreaItemAdapter.ChartAreaItemViewHolder> {
    private static final String TAG = ChartAreaItemAdapter.class.getSimpleName();

    /** The list of chart items currently displayed by the adapter. */
    private final List<ChartAreaItem> chartAreaItems;
    /** The ViewModel associated with the fragment containing the RecyclerView. */
    private final ChartAreaListViewModel viewModel;
    /** The LifecycleOwner of the fragment, used for observing LiveData within item bindings. */
    private final LifecycleOwner viewLifecycleOwner;
    /** Map to store SpeedDialFab helpers for zoom controls, keyed by adapter position. */
    private final Map<Integer, SpeedDialFabHelperZoom> speedDialZoomMap = new HashMap<>();
    /** Map to store SpeedDialFab helpers for chart settings controls, keyed by adapter position. */
    private final Map<Integer, SpeedDialFabHelperChartSettings> speedDialSettingsMap = new HashMap<>();

    /**
     * Creates a new ChartAreaItemAdapter.
     * Registers a custom {@link AdapterDataObserverImpl} to react to data changes.
     *
     * @param chartAreaItems     The list of {@link ChartAreaItem}s to display.
     * @param viewModel          The {@link ChartAreaListViewModel} providing interaction logic.
     * @param viewLifecycleOwner The {@link LifecycleOwner} for data binding and LiveData observation.
     */
    public ChartAreaItemAdapter(List<ChartAreaItem> chartAreaItems, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
        this.chartAreaItems = chartAreaItems;
        this.viewModel = viewModel;
        this.viewLifecycleOwner = viewLifecycleOwner;

        AdapterDataObserverImpl adapterDataObserver = new AdapterDataObserverImpl(chartAreaItems);
        registerAdapterDataObserver(adapterDataObserver);
    }

    /**
     * Creates a new {@link ChartAreaItemViewHolder} by inflating the item layout
     * using {@link ChartAreaItemBinding}.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ChartAreaItemViewHolder that holds the View for each item.
     */
    @NonNull
    @Override
    public ChartAreaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChartAreaItemBinding binding = ChartAreaItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChartAreaItemViewHolder(binding);
    }

    /**
     * Binds data from a {@link ChartAreaItem} at a specific position to the given {@link ChartAreaItemViewHolder}.
     * Sets the {@link ChartSlot}, binds the item and ViewModel to the ViewHolder's binding,
     * and configures the Speed Dial FAB helpers for that item.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
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

    /**
     * Configures the Speed Dial FAB helper for zoom controls for a specific item.
     *
     * @param binding  The binding instance for the item's layout.
     * @param position The adapter position of the item.
     * @param item     The {@link ChartAreaItem} data.
     */
    private void configureSpeedDialFabZoom(@NonNull ChartAreaItemBinding binding, int position, ChartAreaItem item) {

        // Create and configure the SpeedDialFabView helper
        SpeedDialFabHelperZoom helper = new SpeedDialFabHelperZoom();

        helper.configureSpeedDialFab(binding.chartAreaItemScaleControlLayout, item);

        // Store the helper for later cleanup
        speedDialZoomMap.put(position, helper);

        Log.d(TAG, "Configured SpeedDialFabView for position " + position);
    }

    /**
     * Configures the Speed Dial FAB helper for chart settings controls for a specific item.
     *
     * @param binding  The binding instance for the item's layout.
     * @param position The adapter position of the item.
     * @param item     The {@link ChartAreaItem} data.
     */
    private void configureSpeedDialFabSettings(@NonNull ChartAreaItemBinding binding, int position, ChartAreaItem item) {

        // Create and configure the SpeedDialFabView helper
        SpeedDialFabHelperChartSettings helperSettings = new SpeedDialFabHelperChartSettings();

        helperSettings.configureSpeedDialFab(binding.chartAreaItemPropertiesControlLayout, item);

        // Store the helper for later cleanup
        speedDialSettingsMap.put(position, helperSettings);

        Log.d(TAG, "Configured SpeedDialFabView for position " + position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return chartAreaItems.size();
    }

    /**
     * Gets the underlying list of chart items managed by this adapter.
     *
     * @return The list of {@link ChartAreaItem}s.
     */
    public List<ChartAreaItem> getItems() {
        return chartAreaItems;
    }

    /**
     * Called when a ViewHolder created by this adapter has been recycled.
     * Ensures associated Speed Dial FABs are closed and cleans up helper resources
     * stored in the maps ({@link #speedDialZoomMap}, {@link #speedDialSettingsMap}).
     *
     * @param holder The ViewHolder for the item being recycled.
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

    /**
     * Removes the Speed Dial FAB helper for zoom controls associated with a given position.
     *
     * @param position The adapter position.
     */
    private void recycleSpeedDialFabZoom(int position) {
        SpeedDialFabHelperZoom helper = speedDialZoomMap.get(position);
        if (helper != null) {
            speedDialZoomMap.remove(position);
        }
    }

    /**
     * Removes the Speed Dial FAB helper for chart settings controls associated with a given position.
     *
     * @param position The adapter position.
     */
    private void recycleSpeedDialFabSettings(int position) {
        SpeedDialFabHelperChartSettings helper = speedDialSettingsMap.get(position);
        if (helper != null) {
            speedDialSettingsMap.remove(position);
        }
    }

    /**
     * Called when the adapter is detached from the RecyclerView.
     * Clears all stored Speed Dial FAB helper resources.
     *
     * @param recyclerView The RecyclerView from which the adapter is being detached.
     */
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        speedDialZoomMap.clear();
        speedDialSettingsMap.clear();
    }

    /**
     * ViewHolder class for {@link ChartAreaItem}s.
     * Holds the {@link ChartAreaItemBinding} instance for the item layout
     * and provides the {@link #bind(ChartAreaItem, ChartAreaListViewModel, LifecycleOwner)} method
     * to connect the data and ViewModel to the layout.
     */
    public static class ChartAreaItemViewHolder extends RecyclerView.ViewHolder {
        /** Data binding instance for the item layout (chart_area_item.xml). */
        final ChartAreaItemBinding binding;

        /**
         * Creates a new ChartAreaItemViewHolder.
         *
         * @param binding The data binding instance for the item layout.
         */
        ChartAreaItemViewHolder(@NonNull ChartAreaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds a {@link ChartAreaItem} and the {@link ChartAreaListViewModel} to this ViewHolder's layout.
         * Sets the {@link ChartSlot} on the chart view, binds the {@link ChartController},
         * and sets the necessary variables (item, viewModel, lifecycleOwner) on the binding instance.
         *
         * @param item               The {@link ChartAreaItem} data to bind.
         * @param viewModel          The {@link ChartAreaListViewModel} for interaction logic.
         * @param viewLifecycleOwner The {@link LifecycleOwner} for observing LiveData.
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