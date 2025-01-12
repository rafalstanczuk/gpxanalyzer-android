package com.itservices.gpxanalyzer.opengl;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.itservices.gpxanalyzer.databinding.FragmentSpectrumBinding;
import com.itservices.gpxanalyzer.fftspectrum.AudioCaptureState;
import com.itservices.gpxanalyzer.fftspectrum.AudioViewModel;


public class SpectrumFragment extends Fragment {

    private static final String TAG = "SpectrumFragment";

    private AudioViewModel audioViewModel;

    private FragmentSpectrumBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        binding = FragmentSpectrumBinding.inflate(inflater);

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
            Log.d(TAG, "Got new spectrum data: " + list.size() + " points");
            binding.spectrumView.getRenderer().setSpectrumData(list);
            // Request a redraw (because we set RENDERMODE_WHEN_DIRTY)
            binding.spectrumView.requestRender();
        });

        // Also observe the AudioCaptureState if you want to update button text
        audioViewModel.getAudioCaptureState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) {
                binding.button.setText(state.toString()); // e.g. "ON", "OFF", etc.
            }
        });

        // Button logic: toggle ON/OFF
        binding.button.setOnClickListener(v -> {
            // If current state is ON, call stop. Otherwise start.
            if (audioViewModel.getAudioCaptureState().getValue() == AudioCaptureState.ON) {
                audioViewModel.stopRecording();
                // Optionally post new state (OFF) or rely on switchOnOff in the VM
                audioViewModel.switchOnOff(); // or do a manual postValue
            } else {
                audioViewModel.startRecording();
                audioViewModel.switchOnOff(); // or manual postValue
            }
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