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
public abstract class AsyncFFTHookMuteTrue extends AsyncTask<String, String, String> {
    final String TAG = getClass().getSimpleName();

    public abstract void stopAllo();


    Context context;

    AudioManager audioManager = null;

    int fftCounter = 0;

    int i_ring_state = 0;
    long l_ring_time = 0;
    long l_total_time = 0;


    long i_2s = 0;
    long i_1s = 0;
    long l_silent_time = 0;
    int i_mute_count = 0;
    int i_ring_count = 0;
    int i_skt_count = 0;
    long l_ring_start_time = 0;
    long l_skt_start_time = 0;

    boolean is_ringing_check = false;
    boolean is_ttottotto = false;

    int i_ttotto_count = 0;
    int i_silient_count = 0;

    long l_ttotto_start = 0;
    long l_silent = 0;

    boolean i_silent = false;
    long l_silent_time_2 = 0;
    long l_ttotto_time = 0;



    int i_count = 0;

    AudioRecord audioRecord = null;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer = null;
    int blockSize = 256;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLE_RATE = 44100;


    public AsyncFFTHookMuteTrue(Context context) {
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


        if (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime() > 2000) {
            Log.i("doInbackground time out : ", "" + (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime()));
            SingleToneData.getInstance().setPhoneState("IDLE");
        }



        transformer = new RealDoubleFFT(blockSize);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(AUDIO_SOURCE, frequency, channelConfiguration, audioEncoding, bufferSize);
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(true);

        audioRecord.startRecording();

        while (!SingleToneData.getInstance().getPhoneState().equals("IDLE")) {


            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);

            if (is_ringing_check == false) {
                is_ringing_check = true;
                startAllo();
            }

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
        if (params[0].equals("STOP")) {
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

       Log.i("always Frequency", "x : " + max_x + "   max_downy : " + max_downy + "   count : " + i_count);

        /*
        silentTime(max_downy);



        if (max_downy != 1000) {

            i_count++;

            if (i_count < 54) {
                allChecker(max_x);
            } else {
                ringChecker(max_x);
            }
        }
        */

    }


    private void sktChecker(int max_x) {
        if (!(max_x > 8 && max_x < 19) || (max_x == 28) || (max_x > 33 && max_x < 50) || (max_x > 54 && max_x < 59) || (max_x > 82 && max_x < 85)) {
            Log.i("always Frequency", "x : " + max_x + "    downy : ");
            if (i_skt_count == 0) {
                l_skt_start_time = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - l_skt_start_time < 700)
                i_skt_count++;
            else {
                i_skt_count = 0;
            }

            if (i_skt_count > 2) {
                Log.i("STOP SKT Frequency", "x : " + max_x);
                stopFFT();
            }
        }
    }

    private void allChecker(int max_x) {
        if (!((max_x > 8 && max_x < 19) || (max_x > 22 && max_x < 50) || (max_x > 54 && max_x < 59) || (max_x > 82 && max_x < 85))) {
//            Log.i("always ALL f", "x : " + max_x + "    downy : " + "   count : " + i_count);
            if (i_skt_count == 0) {
                l_ring_start_time = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - l_ring_start_time < 1000)
                i_skt_count++;
            else {
                i_skt_count = 0;
            }

            if (i_skt_count > 2) {
                Log.i("STOP ALL f", "x : " + max_x + "    downy : " + "    count : " + i_count);
                stopFFT();
            }
        }
    }


    private void ringChecker(int max_x) {
        if (!(max_x > 22 && max_x < 37)) {
//            Log.i("always ring f", "x : " + max_x + "    downy : " + "     count : " + i_count);
            if (i_ring_count == 0) {
                l_ring_start_time = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - l_ring_start_time < 500)
                i_ring_count++;
            else {
                i_ring_count = 0;
            }

            if (i_ring_count > 1) {

                Log.i("STOP RING f", "x : " + max_x + "    downy : " + "    count : " + i_count);
                stopFFT();
            }
        }
    }

    private void silentChecker(int max_x) {
        if (i_count < 54) {
            if ((max_x > 8 && max_x < 19) || (max_x > 22 && max_x < 50) || (max_x > 54 && max_x < 59) || (max_x > 82 && max_x < 85)) {
                i_mute_count++;
                if (i_mute_count > 3) {
                    i_2s = System.currentTimeMillis();
                }

            } else {
                if ((System.currentTimeMillis() - l_silent_time) > 500) {
                    l_silent_time = System.currentTimeMillis();
                    i_mute_count = 0;
                }
            }
        } else {
            if (max_x > 22 && max_x < 37) {
                i_mute_count++;
                if (i_mute_count > 3) {
                    i_2s = System.currentTimeMillis();
//                    Log.i("i_2s record : ", i_2s+"");
                    if (is_ttottotto == false) {
//                        뚜르르 시작 시간
                        i_1s = i_2s;
                        is_ttottotto = true;
//                        Log.i("i_1s recorded : ", i_1s+"");
                    }
                }

            } else {
                if (System.currentTimeMillis() - i_2s > 200) {
                    l_silent = System.currentTimeMillis();
//                    Log.i("l_silent ", System.currentTimeMillis() - i_2s+"");
                }
                if ((System.currentTimeMillis() - l_silent_time) > 200) {
                    l_silent_time = System.currentTimeMillis();
                    i_mute_count = 0;
                }
            }
        }
    }




    private void silentTime(int max_downy) {
        if (max_downy == 1000) {
            if (i_silent == false) {
                i_silent = true;
                l_silent_time_2 = System.currentTimeMillis();
                Log.i("ttotto Time : ", System.currentTimeMillis() - l_ttotto_time + "");


                long l_time = System.currentTimeMillis() - l_ring_time;
                if (i_ring_state == 1) {
                    if (l_time > 900 && l_time < 1200) {
                        i_ring_state = 4;
                        l_ring_time = System.currentTimeMillis();
                    }
                }else if (i_ring_state == 4){
                    if (l_time < 940 || l_time > 1180){
                        Log.i("STOP FFT", "silent state 4, l_time : " + l_time);
                        stopFFT();
                    }else{
                        l_ring_time = System.currentTimeMillis();
                    }
                }

            }
            if ((System.currentTimeMillis() - l_silent_time_2 > 2100) && (i_ring_state ==4)){
                Log.i("STOP FFT", "silent time over, silent_time: " + (System.currentTimeMillis()-l_silent_time_2));
                stopFFT();
            }
        } else {
            if (i_silent == true) {
                i_silent = false;
                l_ttotto_time = System.currentTimeMillis();
                Log.i("Silent Time : ", System.currentTimeMillis() - l_silent_time_2 + "");


                long l_time = System.currentTimeMillis() - l_ring_time;
                if (i_ring_state == 0) {
                    i_ring_state = 1;
                    l_ring_time = System.currentTimeMillis();
                } else if (i_ring_state == 1) {
                    Log.i("BUG BUG", "ttotto state 1, l_time : " + l_time);
                    l_ring_time = System.currentTimeMillis();
                } else if (i_ring_state == 4){
                    if (l_time < 1900 || l_time > 2100){
                        Log.i("STOP FFT", "ttotto state 4, l_time : " + l_time);
                        stopFFT();
                    }else{
                        l_ring_time = System.currentTimeMillis();
                        // ring checker !!
                    }
                }
            }
        }
    }

/*
    private void silentTime(int max_downy) {
        if (max_downy == 1000) {
            if (i_silent == false) {
                i_silent = true;
                l_silent_time_2 = System.currentTimeMillis();
                Log.i("ttotto Time : ", System.currentTimeMillis() - l_ttotto_time + "");


                long l_time = System.currentTimeMillis() - l_ring_time;
                if (i_ring_state == 1) {
                    if (l_time > 900 && l_time < 1200) {
                        i_ring_state = 4;
                        l_ring_time = System.currentTimeMillis();
                    } else if (l_time > 1300 && l_time < 1570) {
                        i_ring_state = 2;
                        l_total_time = l_total_time + l_time;
                        l_ring_time = System.currentTimeMillis();
                    } else if (l_time > 1590 && l_time < 1650) {
                        i_ring_state = 3;
                        l_ring_time = System.currentTimeMillis();
                    } else {
                        Log.i("STOP FFT", "silent state 1, l_time : " +l_time);
                        stopFFT();
                    }
                } else if (i_ring_state == 2){
                    l_total_time = l_total_time + l_time;
                    if (l_total_time > 1560 && l_total_time < 1650){
                        i_ring_state = 3;
                        l_ring_time = System.currentTimeMillis();
                    } else {
                        Log.i("STOP FFT", "silent state 2, l_time : " +l_time + ", l_total_time : "+l_total_time);
                        stopFFT();
                    }
                }else if (i_ring_state == 4){
                    if (l_time < 940 || l_time > 1180){
                        Log.i("STOP FFT", "silent state 4, l_time : " + l_time);
                        stopFFT();
                    }else{
                        l_ring_time = System.currentTimeMillis();
                   }
                }

            }
            if ((System.currentTimeMillis() - l_silent_time_2 > 2100) && (i_ring_state ==4)){
                Log.i("STOP FFT", "silent time over, silent_time: " + (System.currentTimeMillis()-l_silent_time_2));
                stopFFT();
            }
        } else {
            if (i_silent == true) {
                i_silent = false;
                l_ttotto_time = System.currentTimeMillis();
                Log.i("Silent Time : ", System.currentTimeMillis() - l_silent_time_2 + "");


                long l_time = System.currentTimeMillis() - l_ring_time;
                if (i_ring_state == 0) {
                    i_ring_state = 1;
                    l_ring_time = System.currentTimeMillis();
                } else if (i_ring_state == 1) {
                    Log.i("BUG BUG", "ttotto state 1, l_time : " + l_time);
                    l_ring_time = System.currentTimeMillis();
                } else if(i_ring_state == 2){
                    if (l_time < 100 && l_time > 15){
                        l_total_time = l_total_time + l_time;
                        l_ring_time = System.currentTimeMillis();
                    }else{
                        Log.i("STOP FFT", "ttotto state 2, l_time : " + l_time);
                        stopFFT();
                    }
                } else if (i_ring_state == 3) {
                    if (l_time > 700 && l_time < 890){
                        i_ring_state = 4;
                        l_ring_time = System.currentTimeMillis();
                     // ring checker !!
                    }else{
                        Log.i("STOP FFT", "ttotto state 3, l_time : " + l_time);
                        stopFFT();
                    }
                } else if (i_ring_state == 4){
                    if (l_time < 1900 || l_time > 2100){
                        Log.i("STOP FFT", "ttotto state 4, l_time : " + l_time);
                        stopFFT();
                    }else{
                     l_ring_time = System.currentTimeMillis();
                     // ring checker !!
                    }
                }
            }
        }
    }
*/

    private void startAllo() {
        Log.i("mute true", "startAllo");
        audioManager.setMicrophoneMute(true);
        SingleToneData.getInstance().setPhoneState("RINGING");
        publishProgress("ALLO_START");
    }




    private void stopFFT() {
        Log.i("mute true", "stopFFT  stop, idle, mute, volume");
        PlayAllo.getInstance().backgroundStopAllo();
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager.setMicrophoneMute(false);
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


