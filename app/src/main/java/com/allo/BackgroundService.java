package com.allo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class BackgroundService extends Service {

    private static BackgroundService instance = null;

    final String TAG = getClass().getSimpleName();

    ImageView iv_stop;
    WindowManager.LayoutParams params = null;
    WindowManager windowManager = null;

    WindowManager.LayoutParams params1 = null;
    WindowManager windowManager1 = null;

    LinearLayout ll_info = null;
    RelativeLayout ll_loading = null;

    TextView tv_allo_info;

    float START_X, START_Y;
    int PREV_X, PREV_Y;
    int MAX_X = -1, MAX_Y = -1;

    AudioManager audioManager = null;
    Allo allo = new Allo();

    AsyncFFTHookMuteFalse asyncFFTHookMuteFalse = null;
    AsyncFFTHookMuteTrue asyncFFTHookMuteTrue = null;
    AsyncFFTHookRecordFalse asyncFFTHookRecordFalse = null;

    AlloCacheAsyncTask alloCacheThread = null;
    AlloCacheLocalAsycTask alloCacheLocalAsycTask = null;

    long l_service_start = 0;

    boolean is_prepare = false;
    Tracker mTracker;


    public static boolean isInstanceCreated() {
        return instance != null;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("BackgroundService");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        instance = this;

        setAlloInfoWindow();
        setAlloLoding();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        SingleToneData.getInstance().setVolume(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.FLAG_PLAY_SOUND);


        asyncFFTHookMuteTrue = new AsyncFFTHookMuteTrue(BackgroundService.this) {
            @Override
            public void stopAllo() {
                finishService();
            }
        };

        asyncFFTHookMuteFalse = new AsyncFFTHookMuteFalse(BackgroundService.this) {
            @Override
            public void stopAllo() {
                finishService();
            }
        };

        asyncFFTHookRecordFalse = new AsyncFFTHookRecordFalse(BackgroundService.this) {
            @Override
            public void stopAllo() {
                finishService();
            }
        };

        Log.i("service", "onCreate");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("service", "onStart");


        AlloUtils alloUtils = AlloUtils.getInstance();
        String st_type = alloUtils.getDeviceType();
        Log.i("st_type", st_type);
        asyncFFTHookMuteTrue.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        /*
        if (st_type.equals("mute_false")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                asyncFFTHookMuteFalse.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                asyncFFTHookMuteFalse.execute("a", "a", "a");
            }
        }else if (st_type.equals("mute_true")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                asyncFFTHookMuteTrue.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                asyncFFTHookMuteTrue.execute("a", "a", "a");
            }
        }else if (st_type.equals("impossible")){
            SingleToneData.getInstance().setPhoneState("RINGING");
            Toast.makeText(BackgroundService.this, "수동종료만을 지원하는 스마트폰입니다. 상대방이 전화를 받으면 정지버튼을 누르세요", Toast.LENGTH_LONG);
        }

        */

        String phone_number = "";
        try {
            phone_number = intent.getStringExtra("phone_number");
            Log.i("phone_number", phone_number);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ConnectivityManager cm = (ConnectivityManager) BackgroundService.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        l_service_start = System.currentTimeMillis();

        if ((mobile.isConnected() || wifi.isConnected()) && !phone_number.equals(""))
            getAllo(phone_number);
        else
            getNoInternetAllo();

        return START_STICKY;
    }

    private void getNoInternetAllo() {
        SharedPreferences pref = getSharedPreferences("my_allo", MODE_PRIVATE);
        int i_count = pref.getInt("count", 0);

        if (i_count != 0) {
            Random r = new Random();
            int i_index = r.nextInt(i_count);

            String st_url_key = i_index + "url";
            String st_start_key = i_index + "key";
            String st_title_key = i_index + "title";
            String st_artist_key = i_index + "artist";
            String st_thumb_key = i_index + "thumb";
            String st_image_key = i_index + "image";


            allo.setURL(pref.getString(st_url_key, ""));
            allo.setTitle(pref.getString(st_title_key, ""));
            allo.setArtist(pref.getString(st_artist_key, ""));
            allo.setThumbs(pref.getString(st_thumb_key, ""));
            allo.setImage(pref.getString(st_image_key, ""));
            allo.setStartPoint(pref.getInt(st_start_key, 0));

            if (allo.getURL().equals("")) {
                Log.i("background no internet", "url== null so stop allo");
                finishService();
            } else {
                getCacheFileOnlyLocal();
            }
        } else {
            Log.i("background no internet", "count == 0 so stop allo");
            finishService();
        }
    }


    private void getAllo(String st_phone_number) {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(1000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = this.getString(R.string.url_allo);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("phone_number", st_phone_number);

        Log.i("get allo start", "time : " + (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime()));
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("get allo end", "time : " + (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime()));
                getAlloFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(BackgroundService.this, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "AsyncHttp onFailure");
                getNoInternetAllo();
            }
        });
    }

    private void getAlloFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                boolean is_array = false;
                JSONObject jo_response = jo_result.getJSONObject("response");
                try {
                    JSONObject jo_allo = null;
                    try {
                        JSONArray ja_allo = jo_response.getJSONArray("allo");
                        int length = ja_allo.length();
                        if (length > 0) {
                            Random r = new Random();
                            int i_r = r.nextInt(length);
                            jo_allo = ja_allo.getJSONObject(i_r);
                        }
                    } catch (JSONException e) {
                        jo_allo = jo_response.getJSONObject("allo");
                    }

                    if (jo_allo != null) {
                        if (jo_allo.has("title"))
                            allo.setTitle(jo_allo.getString("title"));
                        if (jo_allo.has("artist"))
                            allo.setArtist(jo_allo.getString("artist"));
                        if (jo_allo.has("image"))
                            allo.setImage(jo_allo.getString("image"));
                        if (jo_allo.has("thumbs"))
                            allo.setThumbs(jo_allo.getString("thumbs"));
                        if (jo_allo.has("uid"))
                            allo.setId(jo_allo.getString("uid"));
                        if (jo_allo.has("url"))
                            allo.setURL(jo_allo.getString("url"));
                        if (jo_allo.has("duration"))
                            allo.setDuration(jo_allo.getInt("duration"));
                        if (jo_allo.has("start_point"))
                            allo.setStartPoint(jo_allo.getInt("start_point"));
                        if (jo_allo.has("end_point"))
                            allo.setEndPoint(jo_allo.getInt("end_point"));

                        getCacheFile();
                    } else {
                        Log.i(TAG, "getAlloFinish jo_allo is null");
                        finishService();
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "getAlloFinish JSONException 1");
                    finishService();
                }
            } else {
                ErrorHandler errorHandler = new ErrorHandler(BackgroundService.this);
                errorHandler.handleErrorCode(jo_result);
                Log.i(TAG, "getAlloFinish Error handler");
                finishService();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "getAlloFinish JsonException 2");
            finishService();
        }
    }

    private void getCacheFile() {

        Log.i("get cache file start", "time : " + (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime()));

        alloCacheThread = new AlloCacheAsyncTask(BackgroundService.this, allo.getURL()) {

            @Override
            public void onFinish(String st_cache_path, long l_time) {
                allo.setURL(st_cache_path);

                    getFileFinish();

            }


            @Override
            public void onDownLoading() {
                tv_allo_info.setText("downloading..");
                Toast.makeText(BackgroundService.this, "다운로드는 곡당 최초 1회만 진행됩니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Log.i(TAG, "getCacheFileOnlyLocal onFailed IDLE");
                finishService();
            }
        };

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            alloCacheThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            alloCacheThread.execute("a", "a", "a");
        }

    }


    private void getCacheFileOnlyLocal() {
        alloCacheLocalAsycTask = new AlloCacheLocalAsycTask(BackgroundService.this, allo.getURL()) {
            @Override
            public void onFinish(String st_cache_path, long l_time) {
                allo.setURL(st_cache_path);

                    getFileFinish();

            }

            @Override
            public void onFailed() {
                Log.i(TAG, "getCacheFileOnlyLocal onFailed IDLE");
                finishService();
            }
        };

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            alloCacheLocalAsycTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            alloCacheLocalAsycTask.execute("a", "a", "a");
        }
    }


    private void getFileFinish(){
        long sleep_time = System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime();
        if (sleep_time < 4500){
            SystemClock.sleep(4500-sleep_time);

        }

        if (SingleToneData.getInstance().getPhoneState().equals("RINGING")){
            PlayAllo.getInstance().setBackgroundPrepare(allo);
            setAlloInfo();

//           when device is galaxy s3
            PlayAllo.getInstance().backgroundPlayAllo();

        } else {
            Log.i("IDLE getFileFinish", "not RINGING");
            finishService();
        }
    }




    private void setBack() {

    }

    private void finishService() {
        Log.i("finish", "finishService was called");
        SingleToneData.getInstance().setPhoneState("IDLE");
        if (BackgroundService.isInstanceCreated())
            BackgroundService.this.stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        PlayAllo.getInstance().backgroundStopAllo();
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager.setMicrophoneMute(false);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, SingleToneData.getInstance().getVolume(), AudioManager.FLAG_PLAY_SOUND);


        instance = null;


        if (ll_info != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(ll_info);
            ll_info = null;
        }

        if (ll_loading != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(ll_loading);
            ll_loading = null;
        }


        if (alloCacheLocalAsycTask != null) {
            if (!alloCacheLocalAsycTask.isCancelled())
                alloCacheLocalAsycTask.cancel(true);
        }

        if (alloCacheThread != null) {
            if (!alloCacheThread.isCancelled())
                alloCacheThread.cancel(true);
        }

        if (asyncFFTHookMuteFalse != null) {
            if (!asyncFFTHookMuteFalse.isCancelled())
                asyncFFTHookMuteFalse.cancel(true);
        }

        if (asyncFFTHookMuteTrue != null) {
            if (!asyncFFTHookMuteTrue.isCancelled())
                asyncFFTHookMuteTrue.cancel(true);
        }

        if (asyncFFTHookRecordFalse != null) {
            if (!asyncFFTHookRecordFalse.isCancelled())
                asyncFFTHookRecordFalse.cancel(true);
        }




    }




    private void setAlloInfoWindow() {

        windowManager1 = (WindowManager) getSystemService(WINDOW_SERVICE);

        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics());
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());

        params1 = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params1.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

        DisplayMetrics matrix = new DisplayMetrics();
        windowManager1.getDefaultDisplay().getMetrics(matrix);

        float height_ = matrix.heightPixels * 0.46f * 0.5f;

        params1.y = -(int) height_;

    }

    private void setAlloLoding() {
        LayoutInflater inflater = (LayoutInflater) BackgroundService.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.allo_loading, null, false);
        ll_loading = (RelativeLayout) view.findViewById(R.id.ll_loading);
        tv_allo_info = (TextView) view.findViewById(R.id.tv_allo_info);
        windowManager1.addView(ll_loading, params1);
    }

    private void setAlloInfo() {

        Allo allo = PlayAllo.getInstance().getAllo();
        LayoutInflater inflater = (LayoutInflater) BackgroundService.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.allo_info, null, false);

        ll_info = (LinearLayout) view.findViewById(R.id.ll_info);

//        ll_info.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mTracker.send(new HitBuilders.EventBuilder().setCategory("Background").setAction("ll_info click").build());
//                Log.i("BackgourndService", "ll_info IDLE");
//                Log.i(TAG, "StopButton click");
//                finishService();
//            }
//        });

        ImageView iv_allo = (ImageView) view.findViewById(R.id.iv_allo);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        LinearLayout ll_stop = (LinearLayout) view.findViewById(R.id.ll_stop);
        ll_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Background").setAction("ll_stop click").build());
                Log.i("BackgourndService", "ll_stop IDLE");
                Log.i(TAG, "StopButton click");
                finishService();
            }
        });


        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(100)).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(BackgroundService.this));

        imageLoader.displayImage(allo.getThumbs(), iv_allo, options);
        tv_title.setText(allo.getTitle());
        tv_artist.setText(allo.getArtist());

        if (ll_loading != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(ll_loading);
            ll_loading = null;
        }
        windowManager1.addView(ll_info, params1);
    }


}
