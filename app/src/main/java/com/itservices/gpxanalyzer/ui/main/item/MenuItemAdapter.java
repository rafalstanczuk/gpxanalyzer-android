package com.itservices.gpxanalyzer.ui.main.item;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.MenuItemBinding;

import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private final List<MenuItem> menuItems;
    private final MenuItemHandler handler;

    public MenuItemAdapter(List<MenuItem> menuItems, MenuItemHandler handler) {
        this.menuItems = menuItems;
        this.handler = handler;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MenuItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.menu_item, parent, false);
        return new MenuItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.bind(item, handler);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private final MenuItemBinding binding;

        MenuItemViewHolder(@NonNull MenuItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MenuItem item, MenuItemHandler handler) {
            binding.setMenuItem(item);
            binding.setHandler(handler);
            binding.executePendingBindings();
        }
    }
}