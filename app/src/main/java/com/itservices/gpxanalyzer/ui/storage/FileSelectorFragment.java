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
import com.itservices.gpxanalyzer.ui.mapper.FileInfoItemMapper;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

/**
 * A {@link DialogFragment} that provides a user interface for selecting a GPX file from storage.
 * It displays a list of found GPX files (with details and miniatures) using a {@link RecyclerView}
 * and the {@link FileAdapter}. It handles permission requests for file access and initiates
 * a background search for GPX files, including miniature generation, displaying progress.
 * Interacts with {@link FileSelectorViewModel} for state management and business logic.
 */
@AndroidEntryPoint
public class FileSelectorFragment extends DialogFragment {

    /**
     * Manages RxJava subscriptions to prevent memory leaks.
     */
    private final CompositeDisposable disposables = new CompositeDisposable();
    /**
     * ViewModel associated with this fragment.
     */
    private FileSelectorViewModel viewModel;
    /**
     * Adapter for the RecyclerView displaying the list of files.
     */
    private FileAdapter fileAdapter;
    /**
     * View binding instance for this fragment's layout (fragment_file_selector.xml).
     */
    private FragmentFileSelectorBinding binding;

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

        fileAdapter = new FileAdapter(viewModel, getViewLifecycleOwner());

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

        viewModel.getSelectedFileInfoItemLiveData().observe(getViewLifecycleOwner(), fileInfoItem -> {
            if (fileInfoItem != null) {
                dismiss();
            }
        });

        viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());

        binding.searchFilesButton.setOnClickListener(v -> {
            if (viewModel.getPermissionsGranted()) {
                startRecursiveSearchAndGenerateMiniatures();
            } else {
                viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());
            }
        });
    }

    /**
     * Starts the process of recursively searching for GPX files and generating miniatures.
     * Disables the search button, updates its text to show progress, observes progress events
     * from the ViewModel, and calls the ViewModel's method to begin the search.
     */
    private void startRecursiveSearchAndGenerateMiniatures() {
        binding.searchFilesButton.setEnabled(false);
        binding.searchFilesButton.setText(R.string.searching_files);

        viewModel.getPercentageEventProgress().observe(getViewLifecycleOwner(), eventProgress -> {
            if (eventProgress == null) {
                return;
            }

            switch (eventProgress.percentageUpdateEventSourceType()) {
                case GPX_FILE_DATA_ENTITY_PROVIDER, UNKNOWN_SOURCE -> {
                }
                case STORAGE_SEARCH_PROGRESS -> {
                    binding.searchFilesButton.setText(getString(R.string.searching_files_progress, eventProgress.percentage()));
                }
                case MINIATURE_GENERATION_PROGRESS -> {
                    binding.searchFilesButton.setText(getString(R.string.generating_miniatures_progress, eventProgress.percentage()));
                }
                case GEOCODING_PROCESSING -> {
                    binding.searchFilesButton.setText(getString(R.string.gathering_geocoded_addresses_progress, eventProgress.percentage()));
                }
                case UPDATING_RESOURCES_PROCESSING -> {
                    binding.searchFilesButton.setText(getString(R.string.updating_database_progress, eventProgress.percentage()));
                }
            }
        });

        viewModel.searchGpxFilesAndGenerateMiniatures(requireContext(), binding.sequentialMiniatureRenderer);
    }

    /**
     * Initializes observers for the ViewModel's LiveData, specifically the list of found GPX files.
     * Updates the {@link FileAdapter} when the list changes and re-enables the search button.
     * Shows appropriate Toast messages based on whether files were found.
     */
    private void initViewModelObservers() {
        viewModel.getFoundFileListLiveData().observe(getViewLifecycleOwner(), gpxFileInfoList -> {
            binding.searchFilesButton.setEnabled(true);
            binding.searchFilesButton.setText(R.string.search_for_file);

            if (gpxFileInfoList == null) {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
                }
            } else if (gpxFileInfoList.isEmpty()) {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
                }
                fileAdapter.setFiles(FileInfoItemMapper.mapFrom(gpxFileInfoList));
            } else {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), getString(R.string.found_gpx_files, gpxFileInfoList.size()), Toast.LENGTH_SHORT).show();
                }
                fileAdapter.setFiles(FileInfoItemMapper.mapFrom(gpxFileInfoList));
            }
        });
    }

    /**
     * Handles the case where necessary file access permissions were denied.
     * Dismisses the dialog and shows a warning Toast message.
     *
     * @param context The application context.
     */
    private void warningNeedsPermissions(Context context) {
        dismiss();
        Toast.makeText(context, R.string.permission_denied_cannot_access_files, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up resources when the view is destroyed.
     * Clears the ViewBinding reference and disposes of any active RxJava subscriptions.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.clear();
    }
}
