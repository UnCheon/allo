package com.allo;

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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
public class AlloDialog extends Dialog {
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
    TextView tv_uploader;

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


    public AlloDialog(Context context) {
        super(context, R.style.full_screen_dialog);
        this.context = context;
    }

    protected AlloDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public AlloDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setMyAlloFragment(MyAlloFragment myAlloFragment) {
        this.myAlloFragment = myAlloFragment;
    }

    public void setMainFragment(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    public void setAllo(Allo allo) {
        this.allo = allo;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
        this.allo = friend;
    }

    public void setDeleteUrl(String st_url) {
        this.st_url = st_url;
    }

    public void setType(String st_type) {
        this.st_type = st_type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);


        switch (st_type) {
            case "ucc":
                setContentView(R.layout.dialog_allo_click);
                setStore();
                setUcc();
                break;
            case "store":
                setContentView(R.layout.dialog_allo_store);
                setStore();
                break;
            case "upload":
                setContentView(R.layout.dialog_allo_upload);
                setUpload();
                break;
            case "delete":
                setContentView(R.layout.dialog_allo_delete);
                setDelete();
                break;
            case "delete_friend":
                setContentView(R.layout.dialog_allo_delete);
                setDeleteFriend();
                break;
            case "friend":
                setContentView(R.layout.dialog_allo_friend);
                setFriend();
                break;


        }


        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);


        getWindow().setGravity(Gravity.BOTTOM);


        setPlayBar();


    }


    private void setPlayBar() {

        sb_seekBar = (SeekBar) findViewById(R.id.sb_seekBar);
        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);


        tv_title = (TextView) findViewById(R.id.tv_title);

        tv_play_time = (TextView) findViewById(R.id.tv_play_time);
        tv_time = (TextView) findViewById(R.id.tv_time);

        if (!st_type.equals("upload")) {
            iv_allo = (ImageView) findViewById(R.id.iv_allo);
            tv_artist = (TextView) findViewById(R.id.tv_artist);

            DisplayImageOptions options = new DisplayImageOptions.Builder()
//                    .showImageOnLoading(R.drawable.allo)
                    .showImageForEmptyUri(R.drawable.allo)
                    .showImageOnFail(R.drawable.allo)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(0)).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            if (!imageLoader.isInited())
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            imageLoader.displayImage(allo.getThumbs(), iv_allo, options);

            tv_artist.setText(allo.getArtist());
        }

        tv_title.setText(allo.getTitle());

//        playAllo.setAlloDialog(this);
        if (st_type.equals("delete") || st_type.equals("delete_friend")) {
            playAllo.setDeleteAlloPrepare(st_url);
        } else if (st_type.equals("upload")) {
            playAllo.setDeleteAlloPrepare(allo.getURL());
        } else if (st_type.equals("friend")) {
            playAllo.setDeleteAlloPrepare(allo.getURL());
        } else {

            is_allo_prepared = true;
            int i_milsec = 0;

            if (st_type.equals("store")) {
                i_milsec = 60 * 1000;
            } else {
                i_milsec = playAllo.getDuration();
            }

            tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(i_milsec));

            sb_seekBar.setMax(i_milsec);
            sb_seekBar.setProgress(0);

            sb_seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            iv_play_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("on clikc", "click");
                    playAllo();
                }

            });

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (playAllo.isPlaying()) {
                        if (!is_progress_moveing) {
                            if (st_type.equals("store")) {
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

            if (st_type.equals("delete") || st_type.equals("delete_friend") || st_type.equals("friend")) {
                playAllo.seekToAllo(allo.getStartPoint());
            }
        }


    }


    // call back from PlayAllo
    public void onPreparedAllo() {
        Log.i("tag", "allo prepared");

        is_allo_prepared = true;
        int i_milsec = playAllo.getDuration();
        tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(i_milsec));

        sb_seekBar.setMax(playAllo.getDuration());
        sb_seekBar.setProgress(0);
        sb_seekBar.setProgress(allo.getStartPoint());


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

        if (st_type.equals("delete") || st_type.equals("delete_friend") || st_type.equals("friend")) {
            playAllo.seekToAllo(allo.getStartPoint());
        }


    }

    public void onCompleteAllo() {

        playAllo.seekToAllo(0);
        sb_seekBar.setProgress(0);
        iv_play_pause.setImageResource(R.drawable.selector_play);

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



    private void setStore() {

        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);
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



        btn_purchase = (Button) findViewById(R.id.btn_purchase);
        btn_gift = (Button) findViewById(R.id.btn_gift);



        btn_purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "\n'" + allo.getTitle() + "'" + " 을(를) 구매하시겠습니까?\n\n이용권이 소모되지 않습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("알로 구매하기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                purchase();

                                // 'YES'
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                gift();
            }
        });



    }

    private void setUcc() {
        String st_uploader = "created by " + allo.getUploader();
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv_uploader = (TextView) findViewById(R.id.tv_uploader);

        tv_content.setText(allo.getContent());
        tv_uploader.setText(st_uploader);
    }


    private void setUpload() {

        btn_select = (Button) findViewById(R.id.btn_select);

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecordActivity) context).onSelectAlloFinish(allo);
                dismiss();
            }
        });

    }

    private void setDelete() {
        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);
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

        btn_delete = (Button) findViewById(R.id.btn_delete);

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

                Log.i("allo _list string ", st_allo_list);

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

    }


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
                        new DialogInterface.OnClickListener() {
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

    private void setFriend() {
        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);
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
    }


    private void setDeleteFriend() {
        iv_allo_main = (ImageView) findViewById(R.id.iv_allo_main);
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

        btn_delete = (Button) findViewById(R.id.btn_delete);

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


    private void deleteFriendAlloSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
//                SingleToneData.getInstance().deleteFriendAllo(friend);
//                friend.setTitle(null);
//                friend.setArtist(null);
//                friend.setURL(null);
//                friend.setThumbs(null);
//                friend.setImage(null);
//                friend.setId(null);
//                friend.setStartPoint(0);
//                friend.setEndPoint(0);
//                friend.setDuration(0);
//                SingleToneData.getInstance().addFriendAllo(friend);
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
                        new DialogInterface.OnClickListener() {
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


    private void setCancel() {

        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    private void playAllo() {

        Log.i("play allo", "play allo");

        if (!is_allo_prepared)
            return;

        Log.i("play allo", "is allo prepared ");

        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
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
                        Toast.makeText(context, context.getResources().getString(R.string.success_purchase), Toast.LENGTH_SHORT).show();

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

    private void select() {

    }


    @Override
    public void onStop() {
        super.onStop();
        playAllo.stopAllo();
        if (timer != null)
            timer.cancel();

//        if (alloCacheThread != null && alloCacheThread.isAlive()){
//            try{
//                alloCacheThread.join();
//            }catch(InterruptedException e){
//
//            }
//        }
    }
}
