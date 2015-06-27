package com.allo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class BackgroundService extends Service{


	AudioManager audioManager = null;
	int currVol;

    final String TAG = getClass().getSimpleName();

    AsyncFFTHook asyncFFTHook;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        Log.i("before volume ", String.valueOf(currVol));
//        audioManager.setMode(AudioManager.STREAM_VOICE_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, AudioManager.FLAG_PLAY_SOUND);
//        audioManager.setMode(AudioManager.STREAM_VOICE_CALL);
        audioManager.setSpeakerphoneOn(false);
        Log.i("after volume ", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)));
		Log.i("service", "onCreate");



	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("service", "onStart");
        String phone_number = intent.getStringExtra("phone_number");

        Log.i("phone _ number in background", phone_number);

        playAllo(phone_number);
        asyncFFTHook = new AsyncFFTHook(getApplicationContext());
        asyncFFTHook.execute();


        return START_STICKY;
	}


    public void playAllo(String phone_number){

        String fileName = "free.mp3";
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getAssets().openFd(fileName);
            RingbackTone mRingbackTone = RingbackTone.getInstance();
            mRingbackTone.playRingbackTone(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

        } catch (IOException e) {
            e.printStackTrace();
        }



        /*
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);

        String url = getApplicationContext().getString(R.string.base_url) + "ringback/tone/";

        PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        params.put("phone_number", phone_number);



        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject response_object = new JSONObject(new String(responseBody));

                    String status = response_object.getString("status");
                    JSONObject result_object = response_object.getJSONObject("response");
                    Log.i("status", status);
                    if (status.equals("success")) {

                        String url = result_object.getString("url");
                        String start_point = result_object.getString("start_point");



                        String http_string = url.substring(0,4);

                        Log.i("http_string", http_string);
                        Log.i("before url", url);
                        if (!(http_string.contentEquals("http"))){
                            Log.i("in url", url);
                            url = getApplicationContext().getString(R.string.base_media_url)+url;
                        }


                        RingbackTone mRingbackTone = RingbackTone.getInstance();
                        Log.i("url", url);
                        mRingbackTone.playRingbackTone(url);
                    } else {

                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(new String(responseBody));
            }
        });
        */
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
        Log.i("Allo", "onDestroy");

        RingbackTone mRingbackTone = RingbackTone.getInstance();
        mRingbackTone.stopRingbackTone();

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVol, AudioManager.FLAG_PLAY_SOUND);
//        audioManager.setMode(AudioManager.STREAM_MUSIC);
        audioManager.setMicrophoneMute(false);
        audioManager.setSpeakerphoneOn(true);

        asyncFFTHook.cancel(true);
    }
}
