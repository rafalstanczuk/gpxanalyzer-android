package com.itservices.gpxanalyzer.audio;

import static com.itservices.gpxanalyzer.utils.common.ConcurrentUtil.tryToDispose;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.audio.audiocapture.AudioBuffer;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioCapture;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioCaptureState;
import com.itservices.gpxanalyzer.audio.audiocapture.AudioSpectrum;
import com.itservices.gpxanalyzer.data.dsp.FFTProcessor;

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

    public void startRecording(Context context) {
        if (audioCapture.init(context)) {
            fftProcessor.init(audioCapture);
            disposables = audioCapture.startRecording()
                    .map(a -> (AudioBuffer) a)
                    //.map(NoiseFilter::filter)
                    .map(fftProcessor::process)
                    .map(AudioSpectrum::getPositiveFrequencyAmplitudePairList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .doOnError(throwable -> Log.e("audioCapture", "startRecording: ", throwable))
                    .doOnNext(spectrumPairList -> spectrumPairListLiveData.postValue(spectrumPairList))
                    .subscribe();
        }
    }

    public void stopRecording() {
        audioCapture.stopRecording();
        tryToDispose(disposables);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        tryToDispose(disposables);
    }

    public void switchOnOff(Context context) {
        // Button logic: toggle ON/OFF

        AudioCaptureState state =
                audioCaptureState.getValue();

        if (state != null) {
            if (state == AudioCaptureState.ON) {
                stopRecording();
            } else {
                startRecording(context);
            }

            audioCaptureState.postValue(
                    state.getNextCyclic()
            );
        }
    }
}
