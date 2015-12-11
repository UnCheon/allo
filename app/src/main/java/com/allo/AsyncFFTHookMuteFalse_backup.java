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
public abstract class AsyncFFTHookMuteFalse_backup extends AsyncTask<String, String, String> {
    final String TAG = getClass().getSimpleName();

    public abstract void playAllo();

    public abstract void stopAllo();


    Context context;

    AudioManager audioManager = null;

    int fftCounter = 0;
    int phoneState = 1;

    long i_2s = 0;
    long i_1s = 0;
    long l_silent_time = 0;
    long l_start_time;
    int i_mute_count = 0;

    int i_ring_count = 0;
    int i_skt_count = 0;
    long l_ring_start_time = 0;
    long l_skt_start_time = 0;

    boolean is_ringing_check = false;
    boolean is_ttottotto = false;

    long l_silent = 0;


    int i_count = 0;

    AudioRecord audioRecord = null;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer = null;
    int blockSize = 256;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLE_RATE = 44100;


    public AsyncFFTHookMuteFalse_backup(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        Log.i(TAG, "doInBackground start hooking");

        phoneState = 1;
        fftCounter = 0;

        transformer = new RealDoubleFFT(blockSize);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(AUDIO_SOURCE, frequency, channelConfiguration, audioEncoding, bufferSize);
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        l_start_time = System.currentTimeMillis();
        audioRecord.startRecording();

        while (!SingleToneData.getInstance().getPhoneState().equals("IDLE")) {

            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);

            fftUpdate(toTransform);


            if (isCancelled()) {
                Log.i("mute true stop ", "is canceled true");
                break;
            }

        }
        Log.i("STOP fft", " after while");
        stopFFT();
        destroy();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
//        Log.i(TAG, "onProgressUpdate");
        if (params[0].equals("ALLO_START")) {
            Log.i("mute true", "onProgressUpdate : ALLO_START");

        } else if (params[0].equals("STOP")) {
            Log.i("mute true", "onProgressUpdate : STOP");
            stopAllo();
        }
    }

    @Override
    protected void onPostExecute(String params) {
        Log.i("mute true", "onPostExecute");
        stopAllo();
    }


    protected void fftUpdate(double[]... toTransform) {
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
        silentChecker(max_x);

        if (is_ringing_check == false){
            if (System.currentTimeMillis() - l_start_time > 3000){
                stopFFT();
            }else if (i_2s > 0){
                is_ringing_check = true;
                startAllo();
            }
        }


        if (is_ringing_check == true) {

            if (System.currentTimeMillis() - i_2s > 2300) {
                Log.i("STOP SLIENT f", "i_2s is bigger than 2000, " + String.valueOf(System.currentTimeMillis() - i_2s));
                stopFFT();
            }

            if (System.currentTimeMillis() - i_1s < 1000) {
                if (l_silent - i_2s > 200 && (l_silent != 0)) {
                    Log.i("ttottotto", "stop stop stop");
                    stopFFT();
                }
            } else {
                if (l_silent - i_2s > 200 && (l_silent != 0)) {
                    Log.i("ttottotto", "true - > false");
                    is_ttottotto = false;
                }
            }
        }

        if (max_downy != 1000) {
            Log.i("always Frequency", "x : " + max_x);
        }
    }


    private void silentChecker(int max_x) {

        if (max_x > 22 && max_x < 37) {
            i_mute_count++;
            if (i_mute_count > 3) {
                i_2s = System.currentTimeMillis();
                Log.i("i_2s record : ", i_2s + "");
                if (is_ttottotto == false) {
//                        뚜르르 시작 시간
                    i_1s = i_2s;
                    is_ttottotto = true;
                    Log.i("i_1s recorded : ", i_1s + "");
                }
            }

        } else {
            if (System.currentTimeMillis() - i_2s > 200) {
                l_silent = System.currentTimeMillis();
                Log.i("l_silent ", System.currentTimeMillis() - i_2s + "");
            }
            if ((System.currentTimeMillis() - l_silent_time) > 200) {
                l_silent_time = System.currentTimeMillis();
                i_mute_count = 0;
            }
        }

    }

    private void startAllo() {
        Log.i("mute true", "startAllo");
        SingleToneData.getInstance().setPhoneState("RINGING");
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.FLAG_PLAY_SOUND);
        PlayAllo.getInstance().backgroundPlayAllo();

//        publishProgress("ALLO_START");
    }


    private void stopFFT() {
        Log.i("mute true", "stopFFT  stop, idle, mute, volume");
        PlayAllo.getInstance().backgroundStopAllo();
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, SingleToneData.getInstance().getVolume(), AudioManager.FLAG_PLAY_SOUND);
        publishProgress("STOP");
    }

    private void destroy() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }


    @Override
    protected void onCancelled() {
        Log.i("mute true", "onCancelled()");
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String params) {
        Log.i("mute true", "onCancelled(String parmas)");
        super.onCancelled(params);
    }

}


