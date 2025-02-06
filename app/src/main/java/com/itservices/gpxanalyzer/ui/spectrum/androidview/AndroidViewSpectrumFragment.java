package com.itservices.gpxanalyzer.ui.spectrum.androidview;

import static com.itservices.gpxanalyzer.utils.common.ConcurrentUtil.tryToDispose;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.itservices.gpxanalyzer.MainActivity;
import com.itservices.gpxanalyzer.ui.spectrum.AudioViewModel;
import com.itservices.gpxanalyzer.databinding.FragmentAndroidViewSpectrumBinding;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class AndroidViewSpectrumFragment extends Fragment {

    static final String TAG = AndroidViewSpectrumFragment.class.getSimpleName();
    public AudioViewModel audioViewModel;

    private MainActivity activity;
    private FragmentAndroidViewSpectrumBinding binding;

    Disposable disposable = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioViewModel = new ViewModelProvider(this).get(AudioViewModel.class);
    }

    @Override
    public void onViewCreated(
            @NonNull View view, @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();

        tryToDispose(disposable);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupObservers();
    }

    private void setupObservers() {

        audioViewModel.getSpectrumPairListLiveData()
                .observe(getViewLifecycleOwner(), audioSpectrum -> {
                            //   Log.d(TAG, "audioSpectrum() :" + audioSpectrum);

                            double[] array = audioSpectrum.stream().map(
                                    pair -> pair.second
                            ).mapToDouble(d -> d).toArray();

                            requireActivity().runOnUiThread(() -> {
                                binding.spectrumView.updateSpectrum(array);
                            });
                        }
                );

        audioViewModel.getAudioCaptureState()
                .observe(getViewLifecycleOwner(), audioCaptureState -> {
                            if (audioCaptureState != null) {
                                activity.runOnUiThread(() ->
                                        binding.button.setText(audioCaptureState.getNextCyclic().name())
                                );

                                switch (audioCaptureState) {

                                    case OFF:
                                        audioViewModel.stopRecording();
                                        break;
                                    case ON:
                                        audioViewModel.startRecording(requireActivity());
                                        break;
                                }

                            }
                        }
                );

        binding.button.setOnClickListener(view -> {
            audioViewModel.switchOnOff(requireActivity());
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        activity = (MainActivity) requireActivity();
        binding = FragmentAndroidViewSpectrumBinding.inflate(inflater);

        return binding.getRoot();
    }
}