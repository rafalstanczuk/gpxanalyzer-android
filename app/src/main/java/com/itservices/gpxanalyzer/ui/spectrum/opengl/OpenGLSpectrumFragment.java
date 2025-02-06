package com.itservices.gpxanalyzer.ui.spectrum.opengl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.itservices.gpxanalyzer.ui.spectrum.AudioViewModel;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioCaptureState;
import com.itservices.gpxanalyzer.databinding.FragmentOpenglSpectrumBinding;


public class OpenGLSpectrumFragment extends Fragment {

    private static final String TAG = "OpenGLSpectrumFragment";

    private AudioViewModel audioViewModel;

    private FragmentOpenglSpectrumBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        binding = com.itservices.gpxanalyzer.databinding.FragmentOpenglSpectrumBinding.inflate(inflater);
        binding.setViewModel(audioViewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the AudioViewModel via Hilt or normal ViewModelProvider
        audioViewModel = new ViewModelProvider(requireActivity()).get(AudioViewModel.class);

        // Observe the spectrum pair list
        audioViewModel.getSpectrumPairListLiveData().observe(getViewLifecycleOwner(), list -> {
            // Update the renderer’s data
            //Log.d(TAG, "Update the renderer’s data");
            binding.spectrumView.getRenderer().setSpectrumData(list);
            // Request a redraw (because we set RENDERMODE_WHEN_DIRTY)
            binding.spectrumView.requestRender();
        });

        audioViewModel.getAudioCaptureState().observe(getViewLifecycleOwner(), state -> {
            requireActivity().runOnUiThread(() -> {
                if (state != null) {
                    binding.button.setText(state.getNextCyclic().toString());
                } else {
                    binding.button.setText(AudioCaptureState.ON.toString());
                }
            });
        });

        binding.button.setOnClickListener(v -> {
            audioViewModel.switchOnOff(requireActivity());
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause GLSurfaceView
        binding.spectrumView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume GLSurfaceView
        binding.spectrumView.onResume();
    }
}