package com.itservices.gpxanalyzer.audio;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.audio.audiocapture.AudioCapture;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioCaptureState;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioSpectrum;
import com.itservices.gpxanalyzer.dsp.FFTProcessor;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


@HiltViewModel
public class AudioViewModel extends ViewModel {

    @Inject
    public AudioCapture audioCapture;

    @Inject
    public FFTProcessor fftProcessor;


    private MutableLiveData<AudioSpectrum> spectrumLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Pair<Float, Double>>> spectrumPairListLiveData = new MutableLiveData<>();

    private MutableLiveData<AudioCaptureState> audioCaptureState = new MutableLiveData<>(AudioCaptureState.OFF);
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
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
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
        // Button logic: toggle ON/OFF

        AudioCaptureState state =
                audioCaptureState.getValue();

        if (state != null) {
            if (state == AudioCaptureState.ON) {
                stopRecording();
            } else {
                startRecording();
            }

            audioCaptureState.postValue(
                    state.getNextCyclic()
            );
        }
    }
}
