package com.itservices.gpxanalyzer.audio.audiocapture;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Observable;

@Singleton
public class AudioCapture {
    private static final int SAMPLE_RATE = 44100;
    private AudioRecord audioRecord;

    private int minBufferSize;
    private int bufferSize;
    private boolean isRecording = false;

    @Inject
    public AudioCapture(@ApplicationContext Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        minBufferSize = AudioRecord.getMinBufferSize(getMaxValidSampleRate(),
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        minBufferSize = 1024*4;
        Log.d(AudioCapture.class.getSimpleName(), "minBufferSize = [" + minBufferSize + "]");

        // Round up to the next power of 2
        bufferSize = 1;
        while (bufferSize < minBufferSize) {
            bufferSize <<= 1; // equivalent to bufferSize *= 2
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    private int getMaxValidSampleRate() {
        int[] sampleRatesToTry = new int[]{192000, 96000, 48000, 44100, 22050, 16000, 11025};

        for (int rate : sampleRatesToTry) {
            int bufferSize = AudioRecord.getMinBufferSize(
                    rate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            if (bufferSize != AudioRecord.ERROR && bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                // Found a valid sample rate
                Log.d(AudioCapture.class.getSimpleName(), "getMaxValidSampleRate = " + rate + "[Hz]");
                return rate;
            }
        }

        // Fallback if nothing works (unlikely)
        return 44100;
    }

    public @NonNull Observable<Object> startRecording() {
        return Observable.create(emitter -> {
            isRecording = true;
            audioRecord.startRecording();
            short[] audioBuffer = new short[bufferSize];
            while (isRecording && !emitter.isDisposed()) {
                int readSize = audioRecord.read(audioBuffer, 0, bufferSize);
                if (readSize > 0) {
                    emitter.onNext(new AudioBuffer(audioBuffer, SAMPLE_RATE));
                }
            }
            audioRecord.stop();
            emitter.onComplete();
        });
    }

    public void stopRecording() {
        isRecording = false;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}