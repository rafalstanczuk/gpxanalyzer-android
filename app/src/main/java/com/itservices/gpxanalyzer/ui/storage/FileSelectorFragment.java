package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentFileSelectorBinding;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@AndroidEntryPoint
public class FileSelectorFragment extends DialogFragment {

    private FileSelectorViewModel viewModel;
    private FileAdapter fileAdapter;
    private FragmentFileSelectorBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FileSelectorViewModel.class);
        viewModel.init();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileSelectorBinding.inflate(inflater, container, false);

        fileAdapter = new FileAdapter(file -> {
            viewModel.selectFile(file);
            Toast.makeText(requireContext(), getString(R.string.selected) + file.getName(), Toast.LENGTH_SHORT).show();
            dismiss();
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(fileAdapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getPermissionsGrantedLiveData().observe(getViewLifecycleOwner(), granted -> {
            if (!granted) {
                warningNeedsPermissions(FileSelectorFragment.this.requireActivity());
            } else {
                initViewModelObservers();
                viewModel.receiveRecentFoundFileList();
            }
        });

        viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());

        binding.btnSelectFile.setOnClickListener(v -> {
            if (viewModel.getPermissionsGranted()) {
                // Start recursive search when button is clicked
                startRecursiveSearch();
            } else {
                viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());
            }
        });
    }

    private void startRecursiveSearch() {
        // Show loading state
        binding.btnSelectFile.setEnabled(false);
        binding.btnSelectFile.setText(R.string.searching_files);

        // Observe search progress
        viewModel.getSearchFilesProgress().observe(getViewLifecycleOwner(), progress -> {
            binding.btnSelectFile.setText(getString(R.string.searching_files_progress, progress));
        });

        // Start the recursive search
        viewModel.searchGpxFilesRecursively(requireContext());
    }

    private void initViewModelObservers() {
        viewModel.getFoundFileListLiveData().observe(getViewLifecycleOwner(), gpxFileInfoList -> {
            // Reset button state
            binding.btnSelectFile.setEnabled(true);
            binding.btnSelectFile.setText(R.string.search_for_file);

            if (gpxFileInfoList == null) {
                Toast.makeText(requireContext(),
                        R.string.no_gpx_files_found,
                    Toast.LENGTH_SHORT).show();
            } else if (gpxFileInfoList.isEmpty()) {
                Toast.makeText(requireContext(), 
                    R.string.no_gpx_files_found, 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), 
                    getString(R.string.found_gpx_files, gpxFileInfoList.size()),
                    Toast.LENGTH_SHORT).show();

                fileAdapter.setFiles(gpxFileInfoList);
            }
        });
    }

    private void warningNeedsPermissions(Context context) {
        dismiss();
        Toast.makeText(context, R.string.permission_denied_cannot_access_files, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.clear(); // Clean up subscriptions
    }
}
