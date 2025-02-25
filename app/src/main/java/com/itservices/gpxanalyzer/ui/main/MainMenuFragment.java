package com.itservices.gpxanalyzer.ui.main;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentMainMenuBinding;
import com.itservices.gpxanalyzer.ui.main.item.MenuItem;
import com.itservices.gpxanalyzer.ui.main.item.MenuItemAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainMenuFragment extends Fragment {

    private FragmentMainMenuBinding binding;
    private MainMenuViewModel viewModel;
    private MenuItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        binding = FragmentMainMenuBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(MainMenuViewModel.class);
        viewModel.setMenuItems(Arrays.asList(
                new MenuItem(getDrawable(requireContext(), R.drawable.ic_files_fill0),getString(R.string.search_for_file), R.id.fileSelectorFragment),
                new MenuItem(getDrawable(requireContext(), R.drawable.ic_query_stats_fill0),getString(R.string.gpx_charts), R.id.chartAreaListFragment)
        ));

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());


        binding.menuRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MenuItemAdapter(new ArrayList<>(), viewModel /* as the handler */);
        binding.menuRecyclerView.setAdapter(adapter);

        viewModel.getMenuItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                // Update adapter data
                adapter = new MenuItemAdapter(items, viewModel);
                binding.menuRecyclerView.setAdapter(adapter);
            }
        });

        viewModel.getMenuItemClickedEvent().observe(getViewLifecycleOwner(), clickedItem -> {
            if (clickedItem != null) {
                Navigation.findNavController(requireView()).navigate(
                        clickedItem.getDestinationFragment()
                );
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}
