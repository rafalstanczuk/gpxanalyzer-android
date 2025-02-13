package com.itservices.gpxanalyzer.ui.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentFileSelectorBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FileSelectorFragment extends Fragment {

    private FileSelectorViewModel viewModel;
    private FileAdapter fileAdapter;
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

        fileAdapter = new FileAdapter(file -> {
            viewModel.selectFile(file);
            Toast.makeText(requireContext(), getString(R.string.selected) + file.getName(), Toast.LENGTH_SHORT).show();

            Navigation.findNavController(requireView()).navigateUp();
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
                    }
                }
        );

        viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());

        binding.btnSelectFile.setOnClickListener(
                v -> {
                    if (viewModel.getPermissionsGranted()) {
                        viewModel.openFilePicker();
                    } else {
                        viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());
                    }
                }
        );
    }

    private void initViewModelObservers() {
        viewModel.getFileFound().observe(getViewLifecycleOwner(), found -> {
            if (!found) {
                Toast.makeText(FileSelectorFragment.this.requireActivity(), R.string.wrong_file_format_select_gpx_only, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFoundFileList().observe(getViewLifecycleOwner(), files -> {
            if (!files.isEmpty()) {
                Toast.makeText(FileSelectorFragment.this.requireActivity(), R.string.file_found_now_select_one_from_the_list, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FileSelectorFragment.this.requireActivity(), R.string.the_list_of_gpx_file_to_use_is_empty_click_search_for_file_first, Toast.LENGTH_SHORT).show();
            }
            fileAdapter.setFiles(files);
        });

        viewModel.loadLocalFiles(requireContext());
    }

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
