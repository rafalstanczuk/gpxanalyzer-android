package com.itservices.gpxanalyzer.fftspectrum;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.Disposable;


@HiltViewModel
public class AudioViewModel extends ViewModel {

    @Inject
    public AudioCapture audioCapture;

    @Inject
    public FFT fft;
    private MutableLiveData<AudioSpectrum> spectrumLiveData = new MutableLiveData<>();

    private MutableLiveData<AudioCaptureState> audioCaptureState = new MutableLiveData<>();
    private Disposable disposables;

    @Inject
    public AudioViewModel() {
    }

    public LiveData<AudioSpectrum> getSpectrumLiveData() {
        return spectrumLiveData;
    }

    public LiveData<AudioCaptureState> getAudioCaptureState() {
        return audioCaptureState;
    }

    public void startRecording() {
        disposables = audioCapture.startRecording()
                .map(audioBufferObj -> {

                    if (audioBufferObj instanceof AudioBuffer) {

                        short[] audioBuffer = ( (AudioBuffer)audioBufferObj ).getAudioBuffer();

                        double[] real = new double[audioBuffer.length];
                        double[] imag = new double[audioBuffer.length];
                        for (int i = 0; i < audioBuffer.length; i++) {
                            real[i] = audioBuffer[i];
                            imag[i] = 0;
                        }

                        fft.fft(real, imag);

                        double[] magnitude = new double[audioBuffer.length];
                        for (int i = 0; i < audioBuffer.length; i++) {
                            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
                        }

                        return new AudioSpectrum(magnitude);
                    } else {
                        return new AudioSpectrum(new double[1]);
                    }
                })
                .subscribe(spectrum -> spectrumLiveData.postValue(spectrum));
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
