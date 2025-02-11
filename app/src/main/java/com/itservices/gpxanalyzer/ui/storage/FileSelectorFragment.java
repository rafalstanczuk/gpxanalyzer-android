package com.itservices.gpxanalyzer.ui.storage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.databinding.FragmentFileSelectorBinding;

import java.util.Objects;

public class FileSelectorFragment extends Fragment {

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private static final String GPX = ".gpx";

    private FileSelectorViewModel viewModel;
    private FileAdapter fileAdapter;
    private FragmentFileSelectorBinding binding;

    // Register the Activity Result API for file selection
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    Log.d("FileSelector", "File selected: " + uri.toString());
                    // Handle file (for example, copy to internal storage or display)
                    if (Objects.requireNonNull(uri.getPath()).endsWith(GPX)) {
                        viewModel.addFile(requireContext(), uri);
                    } else {
                        Toast.makeText(requireContext(), "Wrong file format. Select .gpx only ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("FileSelector", "No file selected");
                }
            });

    // Register the Activity Result API to request "MANAGE_EXTERNAL_STORAGE" permission
    private final ActivityResultLauncher<Intent> manageExternalStorageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission granted, set up file picker
                        setupFilePicker();
                    } else {
                        Toast.makeText(requireContext(), "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileSelectorBinding.inflate(inflater, container, false);

        // Initialize RecyclerView with FileAdapter
        fileAdapter = new FileAdapter(file -> {
            Toast.makeText(requireContext(), "Selected: " + file.getName(), Toast.LENGTH_SHORT).show();
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(fileAdapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check and request storage permissions if needed
        if (isStoragePermissionGranted()) {
            setupFilePicker();
        } else {
            requestStoragePermission();
        }
    }

    /**
     * Checks if storage permissions are granted.
     */
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // For SDK 24 to 28, standard permission check for READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // For SDK 29 (Android 10), Scoped Storage permission
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // For SDK 30+ (Android 11 and above), check for MANAGE_EXTERNAL_STORAGE permission
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests storage permissions.
     */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above, request MANAGE_EXTERNAL_STORAGE permission using ActivityResult API
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            manageExternalStorageLauncher.launch(intent);  // Launch the request for file access permission
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API 29), request standard read permission with scoped storage
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            // For SDK 24 to 28, request READ_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFilePicker(); // Permission granted, set up the file picker
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot access files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets up the file picker and initializes ViewModel.
     */
    private void setupFilePicker() {
        viewModel = new ViewModelProvider(this).get(FileSelectorViewModel.class);

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
