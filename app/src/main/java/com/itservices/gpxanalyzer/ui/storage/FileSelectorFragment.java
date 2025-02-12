package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentFileSelectorBinding;
import com.itservices.gpxanalyzer.utils.PermissionUtils;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileSelectorFragment extends Fragment {

    public static final String GPX_FILE_EXTENSION = ".gpx";

    private FileSelectorViewModel viewModel;
    private FileAdapter fileAdapter;
    private FragmentFileSelectorBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FileSelectorViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileSelectorBinding.inflate(inflater, container, false);

        // Initialize RecyclerView with FileAdapter
        fileAdapter = new FileAdapter(file -> {
            viewModel.selectFile(file);
            Toast.makeText(requireContext(), "Selected: " + file.getName(), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(fileAdapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check and request storage permissions if needed
        if (PermissionUtils.hasFileAccessPermissions(FileSelectorFragment.this.requireActivity())) {
            setupFilePicker();
        } else {
            PermissionUtils.requestFileAccessPermissions(permissionLauncher);
        }
    }

    /**
     * Sets up the file picker and initializes ViewModel.
     */
    private void setupFilePicker() {
        Log.d(FileSelectorFragment.class.getSimpleName(), "setupFilePicker() called");

        // Observe file list
        viewModel.getFiles().observe(getViewLifecycleOwner(), fileAdapter::setFiles);

        // Load existing GPX files
        viewModel.loadFiles(requireContext(), GPX_FILE_EXTENSION);

        // Set file picker button click listener
        binding.btnSelectFile.setOnClickListener(v -> {
            if (PermissionUtils.hasFileAccessPermissions(FileSelectorFragment.this.requireActivity())) {
                openFilePicker();
            } else {
                PermissionUtils.requestFileAccessPermissions(permissionLauncher);
            }
        });
    }

    private void openFilePicker() {
        String[] mimeTypes = new String[]{
                "*/*"
        };
        // Launch the file picker with MIME type filters
        filePickerLauncher.launch(mimeTypes);
    }

    // Register the Activity Result API for file selection
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    File file = viewModel.addFile(FileSelectorFragment.this.requireActivity(), uri, GPX_FILE_EXTENSION);

                    if (file == null) {
                        Toast.makeText(FileSelectorFragment.this.requireActivity(), "Wrong file format. Select .gpx only ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("FileSelector", "No file selected");
                }
            });

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = result.values().stream().allMatch(granted -> granted);
                if (allGranted) {
                    // Permission granted, set up file picker
                    setupFilePicker();
                } else {
                    // Handle denied permission case
                    warningNeedsPermissions(requireContext());
                }
            });

    private void warningNeedsPermissions(Context context) {
        Navigation.findNavController(requireView()).navigateUp();
        Toast.makeText(context, R.string.permission_denied_cannot_access_files, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
