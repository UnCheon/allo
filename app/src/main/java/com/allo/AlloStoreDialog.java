package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by baek_uncheon on 2015. 7. 17..
 */
public class AlloStoreDialog extends Dialog {
    Context context;

    Animation slideUp;
    Allo allo;
    Friend friend;
    String st_type = "";

    SeekBar sb_seekBar;
    Timer timer = null;
    boolean is_progress_moving = false;


    ImageView iv_allo;
    ImageView iv_allo_main;

    ImageView iv_play_pause;
    TextView tv_play_time;
    TextView tv_time;
    TextView tv_title;
    TextView tv_artist;
    TextView tv_content;
    TextView tv_uploader;

    Button btn_purchase;
    Button btn_gift;

    MainFragment mainFragment;


    boolean is_allo_prepared = false;

    String st_url;

    ProgressDialog pd = null;

    Tracker mTracker;


    PlayAllo playAllo = PlayAllo.getInstance();


    public AlloStoreDialog(Context context) {
        super(context, R.style.full_screen_dialog);
        this.context = context;
    }

    protected AlloStoreDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public AlloStoreDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setMainFragment(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    public void setAllo(Allo allo) {
        this.allo = allo;
    }


    public void setType(String st_type) {
        this.st_type = st_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("AlloStoreDialog");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        switch (st_type) {
            case "STORE":
                setContentView(R.layout.dialog_allo_store);
                break;
            case "UCC":
                setContentView(R.layout.dialog_allo_click);
                break;
            default:
                setContentView(R.layout.dialog_allo_store);
                break;
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.BOTTOM);

        playAllo.setStoreDialog(AlloStoreDialog.this);

        setLayout();
        setListener();
        setContent();
        setTimer();

    }
    private void setLayout(){
        sb_seekBar = (SeekBar) findViewById(R.id.sb_seekBar);
        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_play_time = (TextView) findViewById(R.id.tv_play_time);
        tv_time = (TextView) findViewById(R.id.tv_time);
        iv_allo = (ImageView) findViewById(R.id.iv_allo);
        tv_artist = (TextView) findViewById(R.id.tv_artist);

        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);
        btn_purchase = (Button) findViewById(R.id.btn_purchase);
        btn_gift = (Button) findViewById(R.id.btn_gift);

        if(st_type.equals("UCC")){
            tv_content = (TextView) findViewById(R.id.tv_content);
            tv_uploader = (TextView) findViewById(R.id.tv_uploader);
        }

    }

    private void setListener(){
        sb_seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        iv_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("on clikc", "click");
                mTracker.send(new HitBuilders.EventBuilder().setCategory("StoreDialog").setAction("play_btn click").build());
                playAllo();
            }

        });

        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "";
                if (allo.getIsUcc()) {
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("purchase").setAction("purchase_btn click (ucc)").build());
                    msg = "\n'" + allo.getTitle() + "'" + " 을(를) 구매하시겠습니까?\n\n이용권이 소모되지 않습니다.\n";
                }else {
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("purchase").setAction("purchase_btn click (not ucc)").build());
                    msg = "\n'" + allo.getTitle() + "'" + " 을(를) 구매하시겠습니까?\n\n이용권 1매가 사용됩니다.\n";
                }



                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("알로 구매하기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTracker.send(new HitBuilders.EventBuilder().setCategory("purchase").setAction("ok").build());
                                purchase();

                                // 'YES'
                            }
                        }).setNegativeButton("취소",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTracker.send(new HitBuilders.EventBuilder().setCategory("purchase").setAction("no").build());
                                // 'No'
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });

        btn_gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("gift").setAction("gift_btn click").build());
                gift();
            }
        });
    }


    private void setContent(){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.allo)
                .showImageForEmptyUri(R.drawable.allo)
                .showImageOnFail(R.drawable.allo)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(0)).build();

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));

        imageLoader.displayImage(allo.getImage(), iv_allo_main, options);
        imageLoader.displayImage(allo.getThumbs(), iv_allo, options);

        tv_artist.setText(allo.getArtist());
        tv_title.setText(allo.getTitle());
        if (st_type.equals("UCC")){
            String st_uploader = "created by " + allo.getUploader();

            tv_content.setText(allo.getContent());
            tv_uploader.setText(st_uploader);

        }

        int i_milsec = 0;
        if (st_type.equals("STORE")) {
            i_milsec = 60 * 1000;
        } else {
            i_milsec = playAllo.getDuration();
        }

        tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(i_milsec));

        sb_seekBar.setMax(i_milsec);
        sb_seekBar.setProgress(0);

    }


    private void setTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (playAllo.isPlaying()) {
                    if (!is_progress_moving) {
                        if (st_type.equals("STORE")) {
                            if (playAllo.getCurrentPosition() >= 60 * 1000) {
                                playAllo.pauseAllo();
                                playAllo.seekToAllo(0);
                                sb_seekBar.setProgress(0);
                                ((MainActivity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_play_pause.setImageResource(R.drawable.selector_play);
                                    }
                                });
                            }

                        }
                    }

                    sb_seekBar.setProgress(playAllo.getCurrentPosition());

                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 50, 50);
    }



//    SeekBar Listener

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tv_play_time.setText(AlloUtils.getInstance().millisecondToTimeString(progress));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            is_progress_moving = true;
            mTracker.send(new HitBuilders.EventBuilder().setCategory("seekBar").setAction("seekBar touch").build());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            is_progress_moving = false;
            playAllo.seekToAllo(seekBar.getProgress());
            if (!playAllo.isPlaying())
                playAllo();
        }
    };


    private void playAllo() {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_NORMAL);

        if (playAllo.isPlaying()) {
            playAllo.pauseAllo();
            iv_play_pause.setImageResource(R.drawable.selector_play);
        } else {
            Log.i("play allo", "allo play start in dialog ");
            playAllo.playAllo();
            iv_play_pause.setImageResource(R.drawable.selector_pause);
        }
    }

    //    Call back
    public void onCompleteAllo() {
        Log.i("AlloStoreDialog", "onCompleteAllo");
        playAllo.seekToAllo(0);
        sb_seekBar.setProgress(0);
        iv_play_pause.setImageResource(R.drawable.selector_play);

    }


    private void purchase() {


        String url = context.getString(R.string.url_purchase);

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());

        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();

        LoginUtils loginUtils = new LoginUtils(context);
        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("allo_id", allo.getId());


        pd = ProgressDialog.show(context, "", context.getString(R.string.wait_pruchase), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();

                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject jo_response_body = new JSONObject(new String(responseBody));
                    String st_status = jo_response_body.getString("status");
                    if (st_status.equals("success")) {

                        JSONObject jo_response = jo_response_body.getJSONObject("response");
                        if (jo_response.has("cash")) {
                            int i_cash = jo_response.getInt("cash");
                            SingleToneData.getInstance().setCash(String.valueOf(i_cash));
                        }
                        ((MainActivity) context).onReloadPurchaseMainFragment();

                        dismiss();
//                        Toast.makeText(context, context.getResources().getString(R.string.success_purchase), Toast.LENGTH_SHORT).show();

                    } else if (st_status.equals("fail")) {
                        ErrorHandler errorHandler = new ErrorHandler(context);
                        errorHandler.handleErrorCode(jo_response_body);
                    }

                } catch (JSONException e) {
                    Toast.makeText(context, "Json Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(context, context.getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void gift() {

        Intent intent = new Intent(context, FriendListActivity.class);
        intent.putExtra("allo", allo);
        context.startActivity(intent);
        dismiss();

//        select friend activity
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }

    @Override
    public void onStop() {
        super.onStop();

        playAllo.stopAllo();
        if (timer != null)
            timer.cancel();

        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }
}
