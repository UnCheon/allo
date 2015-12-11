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
public abstract class AsyncFFTHookMute extends AsyncTask<String, String, String> {
    final String TAG = getClass().getSimpleName();

    private static final int ALLO_START = 0;
    private static final int ALLO_STOP = 1;

    public abstract void playAllo();

    public abstract void stopAllo();



    Context context;

    AudioManager audioManager = null;

    Boolean connected = false;
    int fftCounter = 0;
    int phoneState = 1;
    String phone_state = "OUTGOING";
    long prev2Time = 0;
    long next2Time = 0;
    long prevStopTime = 0;
    long nextStopTime = 0;

    boolean is_ringing_check = false;
    long i_2s = 0;
    long l_silent_time = 0;
    int i_ring_count = 0;

    int currVol;


    private MediaRecorder recorder = null;
    AudioRecord audioRecord = null;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer = null;
    int blockSize = 256;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLE_RATE = 44100;


    public AsyncFFTHookMute(Context context) {
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


        SingleToneData.getInstance().setPhoneState("RINGING");
        publishProgress("ALLO_START");

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(true);

        phoneState = 1;
        fftCounter = 0;

        transformer = new RealDoubleFFT(blockSize);


        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(AUDIO_SOURCE, frequency, channelConfiguration, audioEncoding, bufferSize);
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];


        audioRecord.startRecording();

        long l_start_time = System.currentTimeMillis();
        while (!SingleToneData.getInstance().getPhoneState().equals("IDLE")) {
            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);
            fftUpdate(toTransform);

            if (isCancelled()){
                Log.i("hook mute", "isCancelled break"); break;
            }

        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
        Log.i(TAG, "onProgressUpdate");

        if (params[0].equals("ALLO_START")) {
            Log.i(TAG, "onProgressUpdate : ALLO_START");
            playAllo();
        } else if (params[0].equals("STOP")) {
            Log.i(TAG, "onProgressUpdate : STOP");
            stopAllo();
        }



    }

    @Override
    protected void onPostExecute(String params) {
        Log.i(TAG, "onPostExecute");
        destroy();

    }

    @Override
    protected void onCancelled() {
        Log.i(TAG, "onCancelled()");
        destroyOnCancel();
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String params) {
        Log.i(TAG, "onCancelled(String parmas)");
        destroyOnCancel();
        super.onCancelled(params);
    }


    protected void fftUpdate(double[]... toTransform) {

        long current_time = System.currentTimeMillis();
        if (i_2s > 0 && current_time - i_2s > 2500) {
            Log.i("i 2s after", String.valueOf(current_time - i_2s));
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
//            Log.i("always Frequency", "x : " + max_x + "    downy : " + max_downy + "    phoneState : " + phoneState);
        }

        if (max_x > 25 && max_x <32 && max_downy > 30 && max_downy < 99 ){
            Log.i("Frequency", "x : " + max_x + "    downy : " + max_downy + "    phoneState : " + phoneState);
            i_ring_count ++;
            if (i_ring_count > 3){
                i_2s = System.currentTimeMillis();
                Log.i("@@@@@@@ Frequency", "x : " + max_x + "    downy : " + max_downy + "    i_2s : " + i_2s);
            }
//            Log.i("@@@@@@@ Frequency", "x : " + max_x + "    downy : " + max_downy + "    i_2s : " + i_2s);

        }else{
            if ((System.currentTimeMillis() - l_silent_time) > 500){
              l_silent_time = System.currentTimeMillis();
                i_ring_count = 0;
            }

//            silent time

        }

//        Log.i("Frequency", "x : " + max_x + "    downy : " + max_downy + "    i_2s : " + i_2s);




        if (max_x < 100) {
            if (fftCounter < 7) {
                ++fftCounter;
                if (max_x > 38 && max_x < 45) { // skt tone start
                    phoneState = 2;
                    prev2Time = System.currentTimeMillis();
                } else if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
                    if (fftCounter > 5) {
                        Log.i("hook mute", "fft counter > 4"); stopFFTCheck();
                    }else{
                    }
                }
            } else {
                if (phoneState == 1) {
                    // ring back tone
                    if (!(max_x > 24 && max_x < 35)) {
                        Log.i("hook mute", "not ringbacktone 1"); stopFFTCheck();
                    } else {
                    }
                } else if (phoneState == 2) {
                    next2Time = System.currentTimeMillis();
                    if (next2Time - prev2Time > 1900) {
                        phoneState = 1;
                    }
                    if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
                        Log.i("hook mute", "not ringbacktone 2");stopFFTCheck();
                    } else {
                    }
                }
            }

        }

    }

    private void startAllo() {
        Log.i(TAG, "startAllo");
        if (!SingleToneData.getInstance().getPhoneState().equals("RINGING")) {
            SingleToneData.getInstance().setPhoneState("RINGING");
            publishProgress("ALLO_START");
        }
    }


    private void stopFFTCheck() {
        Log.i("stop audio", "stop audio was called");

        nextStopTime = System.currentTimeMillis();
        if ((nextStopTime - prevStopTime) < 500) {
            Log.i("stop fft", "connected!!");
                stopFFT();

        } else {
            prevStopTime = System.currentTimeMillis();
        }
    }

    private void stopFFT() {
        Log.i(TAG, "stopFFT");
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager.setMicrophoneMute(false);
        publishProgress("STOP");
    }

    private void destroy(){
        Log.i(TAG, "destroy");
        SingleToneData.getInstance().setPhoneState("IDLE");


        audioManager.setMicrophoneMute(false);

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }


        stopAllo();
    }

    private void destroyOnCancel(){
        Log.i(TAG, "destroyOnCancel");
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(false);

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

    }
}




    /*


//    @Override
//    public void run() {
//        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        _record();
//    }


    private void _record() {
        phoneState = 1;
        fftCounter = 0;
        connected = false;

        recorder = null;
        transformer = null;
        audioRecord = null;

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

        while (!connected) {
            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);
            fftUpdate(toTransform);
        }
    }

    protected void fftUpdate(double[]... toTransform) {

//        if(audioManager.isMicrophoneMute()){
////            Log.i("Mic", "true");
//        }else{
//            Log.i("Mic", "false");
//            audioManager.setMode(AudioManager.MODE_CURRENT);
//            audioManager.setMicrophoneMute(true);
//
//        }

        if (SingleToneData.getInstance().getPhoneState().equals("IDLE")) {
            Log.i("async hook", "IDLE so stop fft");
            connected = true;
            stopFFT();
        }


        if (is_ringing == true && (is_ringing_check == false)) {
            Log.i("async hook", "start allo");
            SingleToneData.getInstance().setPhoneState("RINGING");
            is_ringing_check = true;
            handler.sendEmptyMessage(ALLO_START);
        }


        long current_time = System.currentTimeMillis();
        if (phoneState > 0 && i_2s > 10 && current_time - i_2s > 2500) {
            Log.i("i 2s after", String.valueOf(current_time - i_2s));
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
                    is_ringing = true;
                    i_2s = System.currentTimeMillis();
                    Log.i("i_2s skt", String.valueOf(i_2s));
                    phoneState = 2;
                    prev2Time = System.currentTimeMillis();
                } else if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
                    if (fftCounter > 4) {
//                        stopFFT();
                    }
                }
            } else {
                if (phoneState == 1) {
                    // ring back tone
                    if (!(max_x > 24 && max_x < 35)) {
//                        stopFFT();
                    } else {
                        i_2s = System.currentTimeMillis();
                        Log.i("i_2s ps1", String.valueOf(i_2s));
                        is_ringing = true;
                    }
                } else if (phoneState == 2) {
                    next2Time = System.currentTimeMillis();
                    if (next2Time - prev2Time > 1900) {
                        phoneState = 1;
                    }
                    if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
//                        stopFFT();
                    } else {
                        i_2s = System.currentTimeMillis();
                        Log.i("i_2s ps2", String.valueOf(i_2s));
                        is_ringing = true;
                    }
                }
            }
        }
    }


    private void stopFFT() {
        Log.i("stop audio", "stop audio was called");
        if (SingleToneData.getInstance().getPhoneState().equals("IDLE")) {
            onFinish();
        } else {
            nextStopTime = System.currentTimeMillis();
            if ((nextStopTime - prevStopTime) < 500) {
                onFinish();

            } else {
                Log.i("stop audio", "nextstop time - preve stop time < 500 ");
                prevStopTime = System.currentTimeMillis();
            }
        }
    }

    private void onFinish() {
        SingleToneData.getInstance().setPhoneState("IDLE");
        connected = true;

        audioManager.setMode(AudioManager.MODE_CURRENT);
        audioManager.setMicrophoneMute(false);

        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        handler.sendEmptyMessage(ALLO_STOP);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ALLO_START)
                playAllo();
            else
                stopAllo();
        }
    };
}
*/


