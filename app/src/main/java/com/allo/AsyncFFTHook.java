package com.allo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import ca.uol.aig.fftpack.RealDoubleFFT;

/**
 * Created by baek_uncheon on 2015. 3. 2..
 */
public abstract class AsyncFFTHook extends AsyncTask<String, String, String> {

    public abstract void offHooked();

    Context context;

    AudioManager audioManager = null;

    Boolean connected = false;
    int fftCounter = 0;
    int phoneState = 1;
    String phone_state = "standing";
    long prev2Time = 0;
    long next2Time = 0;
    long prevStopTime = 0;
    long nextStopTime = 0;

    long i_2s = 0;

    int currVol;


    private MediaRecorder recorder = null;
    AudioRecord audioRecord = null;
    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer;
    int blockSize = 256;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLE_RATE = 44100;


    public AsyncFFTHook(Context context) {
        this.context = context;
    }


    @Override
    protected void onPreExecute() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i("Async", "onPreExecute");
    }


    @Override
    protected String doInBackground(String... params) {
        Log.i("Async", "onInBackground");
        _record();
        Log.i("Async", "onInBackground after record");

        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
        Log.i("Async", "onProgressUpdate");

    }


    @Override
    protected void onCancelled() {
        Log.i("Async", "onCancaelled");
    }

    private void _record() {
        phoneState = 1;
        fftCounter = 0;
        connected = false;

        recorder = new MediaRecorder();
        transformer = new RealDoubleFFT(blockSize);

        recorder.setAudioSource(AUDIO_SOURCE);


        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(AUDIO_SOURCE, frequency, channelConfiguration, audioEncoding, bufferSize);
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];

        audioRecord.startRecording();
        System.out.println("start while");

        audioManager.setMicrophoneMute(true);

        while (!connected && !isCancelled()) {
            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);
            fftUpdate(toTransform);
        }
    }

    protected void fftUpdate(double[]... toTransform) {

        if (audioManager.isMicrophoneMute()) {
//            Log.i("Mic", "true");
        } else {
            Log.i("Mic", "false");
            audioManager.setMode(AudioManager.MODE_CURRENT);
            audioManager.setMicrophoneMute(true);

        }

        long current_time = System.currentTimeMillis();

        if (phoneState < 1 && i_2s < 10 && current_time - i_2s > 2000) {
            Log.i("i 2s ", String.valueOf(current_time - i_2s));
            stopFFT();
        }

        int max_downy = 1000;
        int max_x = 1000;
        for (int i = 0; i < toTransform[0].length; i++) {
            int x = i;
            int downy = (int) (100 - (toTransform[0][i] * 10));

            if (downy < 98) {
                if (max_downy > downy) {
                    max_downy = downy;
                    max_x = x;
                }
            }
        }

        if (max_downy != 1000) {
            Log.i("Frequency", "x : " + max_x + "    downy : " + max_downy + "    phoneState : " + phoneState);
        }


        if (max_x < 100) {
            if (fftCounter < 6) {
                ++fftCounter;
                if (max_x > 38 && max_x < 45) { // skt tone start

                    i_2s = System.currentTimeMillis();
                    Log.i("i_2s skt", String.valueOf(i_2s));
                    phoneState = 2;
                    prev2Time = System.currentTimeMillis();
                } else if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
                    if (fftCounter > 4) {
                        stopFFT();
                    }
                }
            } else {
                if (phoneState == 1) {
                    // ring back tone
                    if (!(max_x > 24 && max_x < 35)) {
                        stopFFT();
                    } else {
                        i_2s = System.currentTimeMillis();
                        Log.i("i_2s ps1", String.valueOf(i_2s));
                    }
                } else if (phoneState == 2) {
                    next2Time = System.currentTimeMillis();
                    if (next2Time - prev2Time > 1900) {
                        phoneState = 1;
                    }
                    if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
                        stopFFT();
                    } else {
                        i_2s = System.currentTimeMillis();
                        Log.i("i_2s ps2", String.valueOf(i_2s));
                    }
                }
            }
        }
    }

    private void stopFFT() {
        Log.i("stop audio", "stop audio was called");

        nextStopTime = System.currentTimeMillis();
        if ((nextStopTime - prevStopTime) < 500) {
            Log.i("stop fft", "connected!!");
            connected = true;
            offHooked();

            audioManager.setMode(AudioManager.MODE_CURRENT);
            audioManager.setMicrophoneMute(false);

            if (recorder != null)
                recorder.release();

            if (audioRecord != null)
                audioRecord.release();


        } else {
            prevStopTime = System.currentTimeMillis();
        }
    }
}



