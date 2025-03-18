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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChartAreaItemAdapter extends RecyclerView.Adapter<ChartAreaItemAdapter.ChartAreaItemViewHolder> {

    private final List<ChartAreaItem> chartAreaItems;
    private final ChartAreaListViewModel viewModel;
    private final LifecycleOwner viewLifecycleOwner;


    public ChartAreaItemAdapter(List<ChartAreaItem> chartAreaItems, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
        this.chartAreaItems = chartAreaItems;
        this.viewModel = viewModel;

        this.viewLifecycleOwner = viewLifecycleOwner;

        AdapterDataObserverImpl adapterDataObserver = new AdapterDataObserverImpl(chartAreaItems);
        registerAdapterDataObserver(adapterDataObserver);

        //viewModel.setItemsChangedPublisher(adapterDataObserver.getChangedItems());
    }

    @NonNull
    @Override
    public ChartAreaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ChartAreaItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.chart_area_item, parent, false);
        return new ChartAreaItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartAreaItemViewHolder holder, int position) {
        ChartAreaItem item = chartAreaItems.get(position);
        holder.bind(item, viewModel, viewLifecycleOwner);
    }

    @Override
    public int getItemCount() {
        return chartAreaItems.size();
    }

    public List<ChartAreaItem> getItems() {
        return chartAreaItems;
    }

    public static class ChartAreaItemViewHolder extends RecyclerView.ViewHolder {
        private final ChartAreaItemBinding binding;

        ChartAreaItemViewHolder(@NonNull ChartAreaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChartAreaItem item, ChartAreaListViewModel viewModel, LifecycleOwner viewLifecycleOwner) {
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