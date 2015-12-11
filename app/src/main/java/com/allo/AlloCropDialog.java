package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import ringdroid.MarkerView;
import ringdroid.SamplePlayer;
import ringdroid.WaveformView;
import ringdroid.soundfile.SoundFile;

/**
 * Created by baek_uncheon on 2015. 7. 17..
 */
public class AlloCropDialog extends Dialog implements MarkerView.MarkerListener,
        WaveformView.WaveformListener {
    Context context;

    Animation slideUp;
    Allo allo;
    Friend friend;

    ImageView iv_allo;
    LinearLayout ll_play_pause;
    ImageView iv_play_pause;

    TextView tv_title;
    TextView tv_artist;

    Button btn_crop;

    ProgressDialog pd = null;

    ZoomControls zoomCtr;
    boolean is_marker_moving = false;

    String st_start = "";
    String st_end = "";


    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private boolean mFinishActivity;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private SoundFile mSoundFile;
    private File mFile;
    private String mFilename;


    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private TextView mStartText;
    private TextView mEndText;

    private ImageView mPlayButton;
    private boolean mKeyDown;
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    private SamplePlayer mPlayer;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;

    private Thread mLoadSoundFileThread;

    String st_type = "allo";

    Tracker mTracker;

    public AlloCropDialog(Context context) {
        super(context, R.style.full_screen_dialog);
        this.context = context;
    }

    protected AlloCropDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public AlloCropDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setAllo(Allo allo) {
        this.allo = allo;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
        st_type = "friend";
    }

    public void setSoundFile(SoundFile soundFile) {
        this.mSoundFile = soundFile;
    }

    public void setPlyaer(SamplePlayer samplePlayer) {
        this.mPlayer = samplePlayer;
    }

    public void setDensity(float density) {
        this.mDensity = density;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_allo_crop);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("AlloCropDialog");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);


        getWindow().setGravity(Gravity.BOTTOM);

        setLayoutListener();


        mIsPlaying = false;

        mAlertDialog = null;
        mProgressDialog = null;

        mLoadSoundFileThread = null;
//        mSaveSoundFileThread = null;

        mFilename = "/storage/emulated/0/Samsung/Music/Over_the_horizon.mp3";

//        mSoundFile = null;
        mKeyDown = false;

        mHandler = new Handler();


        loadGui();


        mHandler.postDelayed(mTimerRunnable, 100);

//        loadFromFile();
        mPlayer = new SamplePlayer(mSoundFile);
        finishOpeningSoundFile();

    }

    private void setLayoutListener() {


        ll_play_pause = (LinearLayout) findViewById(R.id.ll_play_pause);
        iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);


        tv_title = (TextView) findViewById(R.id.tv_title);


        iv_allo = (ImageView) findViewById(R.id.iv_allo);
        tv_artist = (TextView) findViewById(R.id.tv_artist);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.allo)
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

        zoomCtr = (ZoomControls) findViewById(R.id.zoomInOut);
        zoomCtr.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveformZoomIn();
                mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("zoom in").build());
            }
        });

        zoomCtr.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveformZoomOut();
                mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("zoom out").build());
            }
        });


        tv_title.setText(allo.getTitle());


        btn_crop = (Button) findViewById(R.id.btn_crop);

        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String st_point_start = String.valueOf(formatTime(mStartPos));
                String st_start_point = st_point_start.replace(".", "");

                String st_point_end = String.valueOf(formatTime(mEndPos));
                String st_end_point = st_point_end.replace(".", "");


                int i_start_point = Integer.parseInt(st_start_point) * 10;
                int i_end_point = Integer.parseInt(st_end_point) * 10;

                allo.setStartPoint(i_start_point);
                allo.setEndPoint(i_end_point);

                if (st_type.equals("friend")) {


                    AsyncHttpClient myClient;

                    myClient = new AsyncHttpClient();
                    myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
                    PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
                    myClient.setCookieStore(myCookieStore);

                    String url = context.getString(R.string.url_friend_allo);
                    RequestParams params = new RequestParams();

                    LoginUtils loginUtils = new LoginUtils(context);

                    params.put("id", loginUtils.getId());
                    params.put("pw", loginUtils.getPw());
                    params.put("start_point", i_start_point);
                    params.put("end_point", i_end_point);
                    params.put("allo_uid", allo.getId());

                    params.put("friend_phone_number", friend.getPhoneNumber());

                    Log.i("start point", String.valueOf(allo.getStartPoint()));
                    Log.i("end point", String.valueOf(allo.getEndPoint()));

                    pd = ProgressDialog.show(context, "", context.getString(R.string.wait_set), true);
                    myClient.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i("HTTP RESPONSE......Crop", new String(responseBody));
                            pd.dismiss();
                            setFriendAlloSuccess(new String(responseBody));

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            pd.dismiss();
                            Toast.makeText(context, context.getText(R.string.on_failure), Toast.LENGTH_SHORT).show();
                            SingleToneData.getInstance().deleteMyAllo(allo);
                        }
                    });
                } else {

                    SingleToneData.getInstance().addMyAllo(allo);
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

                    pd = ProgressDialog.show(context, "", context.getString(R.string.wait_set), true);
                    myClient.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            pd.dismiss();
                            Log.i("HTTP RESPONSE..Crop", new String(responseBody));
                            setMyAlloSuccess(new String(responseBody));

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            pd.dismiss();
                            Toast.makeText(context, context.getText(R.string.on_failure), Toast.LENGTH_SHORT).show();
                            SingleToneData.getInstance().deleteMyAllo(allo);
                        }
                    });
                }

                mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("crop_btn clicked").build());
            }
        });


//        ll_play_pause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }


    private void setFriendAlloSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                handlePause();
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

                String st_start_point = AlloUtils.getInstance().millisecondToTimeString(allo.getStartPoint());
                String st_end_point = AlloUtils.getInstance().millisecondToTimeString(allo.getEndPoint());


                String msg = "\n'" + allo.getTitle() + "' " + st_start_point + "~" + st_end_point + " 구간이 친구별 알로로 설정되었습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("구간 선택").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                                ((SelectMyAlloActivity) context).finish();
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


    private void setMyAlloSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                handlePause();
                String st_start_point = AlloUtils.getInstance().millisecondToTimeString(allo.getStartPoint());
                String st_end_point = AlloUtils.getInstance().millisecondToTimeString(allo.getEndPoint());

                SharedPreferences pref = context.getSharedPreferences("my_allo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();


                int i_count = pref.getInt("count", 0);

                String st_url_key = i_count + "url";
                String st_start_key = i_count + "key";
                String st_title_key = i_count + "title";
                String st_artist_key = i_count + "artist";
                String st_image_key = i_count + "image";
                String st_thumb_key = i_count +"thumb";

                editor.putString(st_title_key, allo.getTitle());
                editor.putString(st_artist_key, allo.getArtist());
                editor.putString(st_url_key, allo.getURL());
                editor.putInt(st_start_key, allo.getStartPoint());
                editor.putString(st_image_key, allo.getImage());
                editor.putString(st_thumb_key, allo.getThumbs());

                editor.putInt("count", i_count+1);
                editor.commit();







                String msg = "\n'" + allo.getTitle() + "' " + st_start_point + "~" + st_end_point + " 구간이 내 알로로 설정되었습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("구간 선택").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setMyAlloDone();

                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            } else {
                SingleToneData.getInstance().deleteMyAllo(allo);
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setMyAlloDone() {
        dismiss();
        ((SelectMyAlloActivity) context).finish();
    }


    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }


    /**
     * Called when the activity is finally destroyed.
     */

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }

    @Override
    protected void onStop() {
        Log.v("Ringdroid", "EditActivity OnDestroy");

        mLoadingKeepGoing = false;

        closeThread(mLoadSoundFileThread);


        mLoadSoundFileThread = null;


        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mPlayer != null) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }

        super.onStop();
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }


    /**
     * Called when the orientation changes and/or the keyboard is shown
     * or hidden.  We don't need to recreate the whole activity in this
     * case, but we do need to redo our layout somewhat.
     */
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        Log.v("Ringdroid", "EditActivity onConfigurationChanged");
//        final int saveZoomLevel = mWaveformView.getZoomLevel();
//        super.onConfigurationChanged(newConfig);
//
//        loadGui();
//
//        mHandler.postDelayed(new Runnable() {
//            public void run() {
//                mStartMarker.requestFocus();
//                markerFocus(mStartMarker);
//
//                mWaveformView.setZoomLevel(saveZoomLevel);
//                mWaveformView.recomputeHeights(mDensity);
//
//                updateDisplay();
//            }
//        }, 500);
//    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            onPlay(mStartPos);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //
    // WaveformListener
    //

    /**
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("waveform touch").build());
        Log.i("wave form", "touch start");
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    public void waveformTouchMove(float x) {
        Log.i("wave form", "touch move");
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        Log.i("wave form", "touch end");
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            mStartPos = trap((int) (mTouchStart + mOffset));
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                mPlayer.seekTo(seekMsec);
//                if (seekMsec >= mPlayStartMsec &&
//                        seekMsec < mPlayEndMsec) {
//                    mPlayer.seekTo(seekMsec);
//                } else {
//                    handlePause();
//                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        if (!is_marker_moving) {
            mWaveformView.zoomIn();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }
    }

    public void waveformZoomOut() {
        if (!is_marker_moving) {
            mWaveformView.zoomOut();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }
    }

    //
    // MarkerListener
    //

    public void markerDraw() {
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("marker touch").build());
        mTouchDragging = true;
        is_marker_moving = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
//            mEndPos = trap((int)(mTouchInitialEndPos + delta));

            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchInitialStartPos + delta));
                mPlayer.seekTo(seekMsec);
//                if (seekMsec >= mPlayStartMsec && seekMsec < mPlayEndMsec) {
//                    mPlayer.seekTo(seekMsec);
//                } else {
//                    handlePause();
//                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }


        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        is_marker_moving = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }


    /**
     * Called from both onCreate and onConfigurationChanged
     * (if the user switched layouts)
     */
    private void loadGui() {
        // Inflate our UI from its XML layout description.


//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        mDensity = metrics.density;

        mMarkerLeftInset = (int) (46 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);

        mStartText = (TextView) findViewById(R.id.starttext);
        mStartText.addTextChangedListener(mTextWatcher);
        mEndText = (TextView) findViewById(R.id.endtext);
        mEndText.addTextChangedListener(mTextWatcher);

        mPlayButton = (ImageView) findViewById(R.id.iv_play_pause);
        mPlayButton.setOnClickListener(mPlayListener);


        TextView markStartButton = (TextView) findViewById(R.id.mark_start);
        markStartButton.setOnClickListener(mMarkStartListener);
        TextView markEndButton = (TextView) findViewById(R.id.mark_end);
        markEndButton.setOnClickListener(mMarkEndListener);

        enableDisableButtons();

        mWaveformView = (WaveformView) findViewById(R.id.waveform);
        mWaveformView.setListener(this);


        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = false;

        updateDisplay();
    }


    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;


        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mMaxPos = mWaveformView.maxPos();
        mEndPos = mMaxPos;
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;

        mStartPos = 0;

        updateDisplay();
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
//            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(
                context.getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                context.getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);
    }

    private Runnable mTimerRunnable = new Runnable() {
        public void run() {
            // Updating an EditText is slow on Android.  Make sure
            // we only do the update if the text has actually changed.
            st_start = formatTimeText(mStartPos);
            st_end = formatTimeText(mEndPos);
            if (mStartPos != mLastDisplayedStartPos &&
                    !mStartText.hasFocus()) {
//                mStartText.setText(formatTime(mStartPos));
                mStartText.setText(st_start);
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos &&
                    !mEndText.hasFocus()) {
                mEndText.setText(st_end);
                mLastDisplayedEndPos = mEndPos;
            }

            String st_section = "선택하기   ( " + st_start + " ∼ " + st_end+" )";

            btn_crop.setText(st_section);

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };

    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(R.drawable.selector_pause);
            mPlayButton.setContentDescription(context.getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(R.drawable.selector_play);
            mPlayButton.setContentDescription(context.getResources().getText(R.string.play));
        }
    }

    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
        mEndPos = mMaxPos;
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }


    private String formatTimeText(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimalText(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimalText(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }
        int min = xWhole / 60;
        int sec = xWhole % 60;
        String st_min = "";
        String st_sec = "";
        if (min < 10)
            st_min = "0" + String.valueOf(min);
        else
            st_min = String.valueOf(min);

        if (sec < 10) {
            st_sec = "0" + String.valueOf(sec);
        } else {
            st_sec = String.valueOf(sec);
        }

        return st_min + ":" + st_sec;

//        if (xFrac < 10)
//            return xWhole + ".0" + xFrac;
//        else
//            return xWhole + "." + xFrac;
    }


    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }
        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }
            mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    handlePause();
                }
            });
            mIsPlaying = true;

            mPlayer.seekTo(mPlayStartMsec);
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
            showFinalAlert(e, R.string.play_error);
            return;
        }
    }

    /**
     * Show a "final" alert dialog that will exit the activity
     * after the user clicks on the OK button.  If an exception
     * is passed, it's assumed to be an error condition, and the
     * dialog is presented as an error, and the stack trace is
     * logged.  If there's no exception, it's a success message.
     */
    private void showFinalAlert(Exception e, CharSequence message) {
        CharSequence title;
        if (e != null) {
            Log.e("Ringdroid", "Error: " + message);
            Log.e("Ringdroid", getStackTrace(e));
            title = context.getResources().getText(R.string.alert_title_failure);
//            setResult(RESULT_CANCELED, new Intent());
        } else {
            Log.v("Ringdroid", "Success: " + message);
            title = context.getResources().getText(R.string.alert_title_success);
        }
//
//        new AlertDialog.Builder(RingdroidEditActivity.this)
//                .setTitle(title)
//                .setMessage(message)
//                .setPositiveButton(
//                        R.string.alert_ok_button,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int whichButton) {
//                                finish();
//                            }
//                        })
//                .setCancelable(false)
//                .show();
    }

    private void showFinalAlert(Exception e, int messageResourceId) {
        showFinalAlert(e, context.getResources().getText(messageResourceId));
    }


    private View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View sender) {
            mTracker.send(new HitBuilders.EventBuilder().setCategory("crop").setAction("play_pause_btn click").build());
            onPlay(mStartPos);
        }
    };


    private View.OnClickListener mMarkStartListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mStartPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition());
                updateDisplay();
            }
        }
    };

    private View.OnClickListener mMarkEndListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mEndPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition());
                updateDisplay();
                handlePause();
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mEndText.getText().toString()));
                    updateDisplay();

                } catch (NumberFormatException e) {
                }
            }


        }
    };

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private String getStackTrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
