package com.allo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by baek_uncheon on 2015. 7. 17..
 */
public class AlloFriendDialog extends Dialog {
    Context context;

    Allo allo;

    SeekBar sb_seekBar;
    Timer timer = null;
    boolean is_progress_moveing = false;

    ImageView iv_allo;
    ImageView iv_allo_main;

    ImageView iv_play_pause;
    TextView tv_play_time;
    TextView tv_time;
    TextView tv_title;
    TextView tv_artist;

    ProgressDialog pd = null;


    PlayAllo playAllo = PlayAllo.getInstance();


    public AlloFriendDialog(Context context) {
        super(context, R.style.full_screen_dialog);
        this.context = context;
    }

    public void setAllo(Allo allo) {
        this.allo = allo;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("AlloFriendDialog");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        this.setCancelable(true);

        setContentView(R.layout.dialog_allo_friend);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.BOTTOM);

        playAllo.setFriendDialog(AlloFriendDialog.this);

        setLayout();
        setListener();
        setContent();
        setTimer();
    }

    private void setLayout() {
        sb_seekBar = (SeekBar) findViewById(R.id.sb_seekBar);
        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_play_time = (TextView) findViewById(R.id.tv_play_time);
        tv_time = (TextView) findViewById(R.id.tv_time);
        iv_allo = (ImageView) findViewById(R.id.iv_allo);
        tv_artist = (TextView) findViewById(R.id.tv_artist);

        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);

    }

    private void setListener() {
        sb_seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        iv_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("on clikc", "click");
                playAllo();
            }

        });

    }

    private void setContent() {
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

        int i_milsec = playAllo.getDuration();

        tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(i_milsec));

        sb_seekBar.setMax(i_milsec);

        playAllo.seekToAllo(allo.getStartPoint());
        sb_seekBar.setProgress(allo.getStartPoint());
    }

    private void setTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (playAllo.isPlaying()) {
                    if (!is_progress_moveing) {
                        sb_seekBar.setProgress(playAllo.getCurrentPosition());
                    }
                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 50, 50);

    }



//    Listener

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tv_play_time.setText(AlloUtils.getInstance().millisecondToTimeString(progress));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            is_progress_moveing = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            is_progress_moveing = false;
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

    public void onCompleteAllo() {
        playAllo.seekToAllo(allo.getStartPoint());
        sb_seekBar.setProgress(allo.getStartPoint());
        iv_play_pause.setImageResource(R.drawable.selector_play);
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
