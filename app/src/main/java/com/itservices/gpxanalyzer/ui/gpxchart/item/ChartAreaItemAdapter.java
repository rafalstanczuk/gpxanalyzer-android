package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.ChartAreaItemBinding;
import com.itservices.gpxanalyzer.ui.gpxchart.ChartAreaListViewModel;

import java.util.List;

/**
 * RecyclerView adapter for displaying ChartAreaItems.
 * This adapter manages the presentation of chart areas in a RecyclerView,
 * handling the creation and binding of view holders for each chart item.
 * It uses data binding to connect the UI components with their corresponding
 * data models and view models.
 */
public class ChartAreaItemAdapter extends RecyclerView.Adapter<ChartAreaItemAdapter.ChartAreaItemViewHolder> {

    private final List<ChartAreaItem> chartAreaItems;
    private final ChartAreaListViewModel viewModel;
    private final LifecycleOwner viewLifecycleOwner;

    /**
     * Creates a new ChartAreaItemAdapter.
     * 
     * @param chartAreaItems The list of chart items to display
     * @param viewModel The view model providing business logic for the chart list
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
     * @param parent The parent ViewGroup
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
     * @param holder The view holder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ChartAreaItemViewHolder holder, int position) {
        ChartAreaItem item = chartAreaItems.get(position);
        item.setPositionSlot(position);
        holder.bind(item, viewModel, viewLifecycleOwner);
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
     * View holder for chart area items.
     * This inner class manages the view for a single chart area,
     * handling the binding of data to the view elements.
     */
    public static class ChartAreaItemViewHolder extends RecyclerView.ViewHolder {
        private final ChartAreaItemBinding binding;

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
         * @param item The chart area item to bind
         * @param viewModel The view model providing business logic
         * @param viewLifecycleOwner The lifecycle owner for observing LiveData
         */
        void bind(ChartAreaItem item, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
            binding.lineChart.setPositionSlot(item.getPositionSlot());
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