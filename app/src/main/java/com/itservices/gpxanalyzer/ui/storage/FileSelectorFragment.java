package com.itservices.gpxanalyzer.ui.storage;

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

    public static final String GPX = ".gpx";

    private FileSelectorViewModel viewModel;
    private FileAdapter fileAdapter;
    private FragmentFileSelectorBinding binding;

    // Register the Activity Result API for file selection
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    Log.d("FileSelector", "File selected: " + uri.toString());
                    // Handle file (for example, copy to internal storage or display)

                    File file = viewModel.addFile(FileSelectorFragment.this.requireActivity(), uri);

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
                    Navigation.findNavController(requireView()).navigate(
                            R.id.mainMenuFragment
                    );
                    Toast.makeText(requireContext(), "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
                }
            });

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
            Navigation.findNavController(requireView()).navigate(
                    R.id.mainMenuFragment
            );
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
            PermissionUtils.requestFileAccessPermissions(FileSelectorFragment.this.requireActivity(), permissionLauncher);
        }
    }

    /**
     * Sets up the file picker and initializes ViewModel.
     */
    private void setupFilePicker() {
        // Observe file list
        viewModel.getFiles().observe(getViewLifecycleOwner(), fileAdapter::setFiles);

        // Load existing GPX files
        viewModel.loadFiles(requireContext(), GPX);

        // Set file picker button click listener
        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());
    }

    /**
     * Opens system file picker to select GPX files using Activity Result API.
     */
    private void openFilePicker() {
        // For Android 9 (API 28) and below, MIME types may need broader filtering.
/*        String[] mimeTypes = new String[]{
                "application/gpx+xml",  // GPX format
                "application/xml",      // General XML format
                "text/xml"              // XML text format
        };*/

        String[] mimeTypes = new String[]{
                "*/*"
        };
        // Launch the file picker with MIME type filters
        filePickerLauncher.launch(mimeTypes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}
