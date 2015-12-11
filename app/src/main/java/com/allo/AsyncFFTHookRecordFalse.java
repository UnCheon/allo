package com.allo;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by baek_uncheon on 2015. 3. 2..
 */
public abstract class AsyncFFTHookRecordFalse extends AsyncTask<String, String, String> {
    final String TAG = getClass().getSimpleName();

    public abstract void stopAllo();


    Context context;

    AudioManager audioManager = null;

    public AsyncFFTHookRecordFalse(Context context) {
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

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        SingleToneData.getInstance().setPhoneState("RINGING");
        Toast.makeText(context, "수동종료만을 지원하는 스마트폰입니다. 상대방이 전화를 받으면 정지버튼을 누르세요", Toast.LENGTH_LONG);


        while (!SingleToneData.getInstance().getPhoneState().equals("IDLE")) {


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


