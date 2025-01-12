package com.itservices.gpxanalyzer.fftspectrum;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;


@HiltViewModel
public class AudioViewModel extends ViewModel {

    @Inject
    public AudioCapture audioCapture;

    @Inject
    public FFTProcessor fftProcessor;


    private MutableLiveData<AudioSpectrum> spectrumLiveData = new MutableLiveData<>();
    private MutableLiveData< List<Pair<Float, Double>> > spectrumPairListLiveData = new MutableLiveData<>();

    private MutableLiveData<AudioCaptureState> audioCaptureState = new MutableLiveData<>();
    private Disposable disposables;

    @Inject
    public AudioViewModel() {
    }

    public LiveData<AudioSpectrum> getSpectrumLiveData() {
        return spectrumLiveData;
    }

    public LiveData<List<Pair<Float, Double>>> getSpectrumPairListLiveData() {
        return spectrumPairListLiveData;
    }

    public LiveData<AudioCaptureState> getAudioCaptureState() {
        return audioCaptureState;
    }

    public void startRecording() {
        fftProcessor.init(audioCapture);
        disposables = audioCapture.startRecording()
                .map(fftProcessor::process)
                .map(AudioSpectrum::getPositiveFrequencyAmplitudePairList)
                .subscribe(spectrumPairList -> spectrumPairListLiveData.postValue(spectrumPairList));
    }

    public void stopRecording() {
        audioCapture.stopRecording();
        disposables.dispose();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }

    public void switchOnOff() {
        AudioCaptureState state = audioCaptureState.getValue();
        if (state != null) {
            audioCaptureState.postValue(
                    state.getNextCyclic()
            );
        } else {
            audioCaptureState.postValue(
                    AudioCaptureState.ON
            );
        }
    }
}
