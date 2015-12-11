package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by baek_uncheon on 2015. 7. 17..
 */
public class AlloMyDialog extends Dialog {
    Context context;

    Animation slideUp;
    Allo allo;
    Friend friend;
    String st_type = "";

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
    TextView tv_content;


    LinearLayout ll_check_basic;
    ImageView iv_check_basic;


    Button btn_purchase;
    Button btn_gift;

    Button btn_select;
    Button btn_delete;
    Button btn_cancel;
    MyAlloFragment myAlloFragment;

    MainFragment mainFragment;


    boolean is_allo_prepared = false;

    String st_url;

    ProgressDialog pd = null;

//    AlloCacheThread alloCacheThread = null;
//
//    ProgressDialog dialog;


    PlayAllo playAllo = PlayAllo.getInstance();
    AlloUtils alloUtils = AlloUtils.getInstance();


    public AlloMyDialog(Context context) {
        super(context, R.style.full_screen_dialog);
        this.context = context;
    }

    protected AlloMyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public AlloMyDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setMyAlloFragment(MyAlloFragment myAlloFragment) {
        this.myAlloFragment = myAlloFragment;
    }

    public void setAllo(Allo allo) {
        this.allo = allo;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
        this.allo = friend;
    }

    public void setType(String st_type) {
        this.st_type = st_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);

        setContentView(R.layout.dialog_allo_delete);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("AlloMyDialog");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.BOTTOM);

        playAllo.setMyDialog(AlloMyDialog.this);

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

        btn_delete = (Button) findViewById(R.id.btn_delete);

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

        if (st_type.equals("MY")) {


            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SingleToneData.getInstance().deleteMyAllo(allo);

                    String st_allo_list = SingleToneData.getInstance().getMyAlloListString();


                    AsyncHttpClient myClient;

                    myClient = new AsyncHttpClient();
                    myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
                    myClient.setCookieStore(myCookieStore);

                    String url = context.getString(R.string.url_my_allo);
                    RequestParams params = new RequestParams();

                    LoginUtils loginUtils = new LoginUtils(context);

                    params.put("id", loginUtils.getId());
                    params.put("pw", loginUtils.getPw());
                    params.put("allo_list", st_allo_list);

                    Log.i("allo_list string ", st_allo_list);

                    pd = ProgressDialog.show(context, "", context.getString(R.string.wait_delete), true);
                    myClient.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            pd.dismiss();
                            Log.i("HTTP RESPONSE......", new String(responseBody));
                            deleteMyAlloSuccess(new String(responseBody));
                            dismiss();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            pd.dismiss();
                            Toast.makeText(context, context.getText(R.string.on_failure), Toast.LENGTH_SHORT).show();
                            SingleToneData.getInstance().addMyAllo(allo);
                            dismiss();
                        }
                    });
                }
            });
        } else if (st_type.equals("MY_FRIEND")) {

            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    AsyncHttpClient myClient;

                    myClient = new AsyncHttpClient();
                    myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
                    myClient.setCookieStore(myCookieStore);

                    String url = context.getString(R.string.url_remove_friend_allo);
                    RequestParams params = new RequestParams();

                    LoginUtils loginUtils = new LoginUtils(context);

                    params.put("id", loginUtils.getId());
                    params.put("pw", loginUtils.getPw());
                    params.put("friend_phone_number", friend.getPhoneNumber());

                    pd = ProgressDialog.show(context, "", context.getString(R.string.wait_delete), true);
                    myClient.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("HTTP RESPONSE......", new String(responseBody));
                            pd.dismiss();
                            deleteFriendAlloSuccess(new String(responseBody));
                            dismiss();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            pd.dismiss();
                            dismiss();
                            Toast.makeText(context, context.getText(R.string.on_failure), Toast.LENGTH_SHORT).show();
                            SingleToneData.getInstance().addMyAllo(allo);
                        }
                    });
                }
            });
        }
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



    private void deleteMyAlloSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                String st_start_point = AlloUtils.getInstance().millisecondToTimeString(allo.getStartPoint());
                String st_end_point = AlloUtils.getInstance().millisecondToTimeString(allo.getEndPoint());


                String msg = "'" + allo.getTitle() + "' " + st_start_point + "~" + st_end_point + " 구간이 삭제되었습니다.";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("알로 삭제").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMyAlloDone();

                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            } else {
                SingleToneData.getInstance().addMyAllo(allo);
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void deleteMyAlloDone() {
        myAlloFragment.onReload();
        dismiss();
    }




    private void deleteFriendAlloSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");
                JSONArray ja_friend_allo_list = jo_response.getJSONArray("friend_allo_list");

                ArrayList<Friend> al_friend_list = new ArrayList<>();
                for (int i = 0; i < ja_friend_allo_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_allo_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));
                    if (jo_friend.has("title"))
                        friend.setTitle(jo_friend.getString("title"));
                    if (jo_friend.has("artist"))
                        friend.setArtist(jo_friend.getString("artist"));
                    if (jo_friend.has("image"))
                        friend.setImage(jo_friend.getString("image"));
                    if (jo_friend.has("thumbs"))
                        friend.setThumbs(jo_friend.getString("thumbs"));
                    if (jo_friend.has("uid"))
                        friend.setId(jo_friend.getString("uid"));
                    if (jo_friend.has("url"))
                        friend.setURL(jo_friend.getString("url"));
                    if (jo_friend.has("duration"))
                        friend.setDuration(jo_friend.getInt("duration"));
                    if (jo_friend.has("start_point"))
                        friend.setStartPoint(jo_friend.getInt("start_point"));
                    if (jo_friend.has("end_point"))
                        friend.setEndPoint(jo_friend.getInt("end_point"));

                    al_friend_list.add(friend);

                }

                SingleToneData.getInstance().setFriendAlloList(al_friend_list);

                String msg = "\n친구별 알로가 삭제되었습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("친구별 알로 삭제").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                                myAlloFragment.onReloadListView();
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


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
