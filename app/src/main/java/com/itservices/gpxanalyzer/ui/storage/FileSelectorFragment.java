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

    /** ViewModel associated with this fragment. */
    private FileSelectorViewModel viewModel;
    /** Adapter for the RecyclerView displaying the list of files. */
    private FileAdapter fileAdapter;
    /** View binding instance for this fragment's layout (fragment_file_selector.xml). */
    private FragmentFileSelectorBinding binding;

    /** Manages RxJava subscriptions to prevent memory leaks. */
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Initializes the ViewModel.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FileSelectorViewModel.class);
        viewModel.init();
    }

    /**
     * Inflates the layout, initializes the RecyclerView adapter, and sets up the basic view structure.
     * The adapter is configured with a listener that calls {@link FileSelectorViewModel#selectFile(File)}
     * and dismisses the dialog upon selection.
     */
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

    /**
     * Called after the view has been created. Initiates permission checks and sets up observers
     * for ViewModel LiveData (permissions, found files, progress). Configures the button listener
     * to start the file search or re-request permissions.
     */
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
        binding.btnSelectFile.setEnabled(false);
        binding.btnSelectFile.setText(R.string.searching_files);

        viewModel.getPercentageEventProgress().observe(getViewLifecycleOwner(), eventProgress -> {
            if (eventProgress == null) {
                return;
            }

            switch (eventProgress.percentageUpdateEventSourceType()){
                case GPX_FILE_DATA_ENTITY_PROVIDER, UNKNOWN_SOURCE -> {
                }
                case STORAGE_SEARCH_PROGRESS -> {
                    binding.btnSelectFile.setText(getString(R.string.searching_files_progress, eventProgress.percentage()));
                }
                case MINIATURE_GENERATION_PROGRESS -> {
                    binding.btnSelectFile.setText(getString(R.string.generating_miniatures_progress, eventProgress.percentage()));
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
            binding.btnSelectFile.setEnabled(true);
            binding.btnSelectFile.setText(R.string.search_for_file);

            if (gpxFileInfoList == null) {
                 Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
            } else if (gpxFileInfoList.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
                 fileAdapter.setFiles(FileInfoItemMapper.mapFrom(gpxFileInfoList));
            } else {
                Toast.makeText(requireContext(), getString(R.string.found_gpx_files, gpxFileInfoList.size()), Toast.LENGTH_SHORT).show();
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
