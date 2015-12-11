package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class RecordActivity extends Activity {

    ImageView iv_back;

    TextView tv_menu_title;

    LinearLayout ll_record;
    TextView tv_status;
    Button btn_play;
    Button btn_record;
    TextView tv_time;
    Button btn_get_mp3;
    ImageView btn_make_allo;
    //    ProgressBar pb_play;
    public volatile Thread th_progressbar_play;
    public int CurrentPosition = 0;
    public int i_total;

    ListView lv_file_list;

    ArrayList<Allo> al_allo_list;


    MediaPlayer player;
    MediaRecorder recorder;

    Timer timer = null;
    final Handler myHandler = new Handler();
    int i_progress_time = 0;

    private boolean is_playing = false, is_recoding = false, is_temp = false;

    private String RECORD_FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo/temp.3gp";

    private final String ST_TITLE_RECORD = "나만의 알로 만들기";
    private final String ST_TITLE_FILE = "MP3파일 업로드하기";

    private final String ST_RECORDING = "녹음중..";
    private final String ST_STANDING = "대기중";
    private final String ST_PLAYING = "재생중..";

    Allo allo_temp;

    Context context;

    Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_make_ucc);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("RecordActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout();
        setListener();
        setAlloList();
        UploadAdapter adapter = new UploadAdapter(getApplicationContext(), R.layout.layout_upload_item, al_allo_list);
        lv_file_list.setAdapter(adapter);

        allo_temp = new Allo();
    }

    private void setLayout() {

        iv_back = (ImageView) findViewById(R.id.iv_back);

        tv_menu_title = (TextView) findViewById(R.id.tv_menu_title);

        ll_record = (LinearLayout) findViewById(R.id.ll_record);
        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_record = (Button) findViewById(R.id.btn_record);
        tv_time = (TextView) findViewById(R.id.tv_time);
        btn_get_mp3 = (Button) findViewById(R.id.btn_get_mp3);
        btn_make_allo = (ImageView) findViewById(R.id.btn_make_allo);
//        pb_play = (ProgressBar) findViewById(R.id.pb_play);

        lv_file_list = (ListView) findViewById(R.id.lv_file_list);
    }


    private void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lv_file_list.getVisibility() == View.VISIBLE)
                    backToRecord();
                else
                    clickBack();
            }
        });

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Record").setAction("btn_record click").build());
                clickRecord();
            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Record").setAction("btn_play click").build());
                clickPlay();
            }
        });

        btn_get_mp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Record").setAction("btn_get_mp3 click").build());
                clickGetMp3();
            }
        });

        btn_make_allo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Record").setAction("btn_make_allo click").build());
                clickMakeAllo();
            }
        });

        lv_file_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Record").setAction("mp3_list click").build());
                lv_file_list.setEnabled(false);
                PlayAllo.getInstance().setType("RECORD");
                PlayAllo.getInstance().setRecordActivity(RecordActivity.this);
                PlayAllo.getInstance().setAlloPrepare(al_allo_list.get(position));
            }
        });

        tv_status.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String status = tv_status.getText().toString();
                switch (status) {
                    case ST_STANDING:
                        btn_play.setEnabled(true);
                        btn_record.setEnabled(true);
                        btn_get_mp3.setEnabled(true);
                        break;
                    case ST_RECORDING:
                        btn_play.setEnabled(false);
                        btn_record.setEnabled(true);
                        btn_get_mp3.setEnabled(false);
                        break;
                    case ST_PLAYING:
                        btn_play.setEnabled(true);
                        btn_record.setEnabled(false);
                        btn_get_mp3.setEnabled(false);
                        break;
                }
            }
        });

    }

    public void onSelectAlloFinish(Allo allo) {
        is_temp = true;
        allo_temp = allo;
        backToRecord();
    }

    private void clickRecord() {
        if (is_recoding) {
            stopRecord();
            is_recoding = false;
            is_temp = true;

            btn_record.setText("다시 녹음하기");
            tv_status.setText(ST_STANDING);

            allo_temp.setTitle("");
            allo_temp.setArtist("");
            allo_temp.setImage("");
            allo_temp.setURL(RECORD_FILEPATH);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(RECORD_FILEPATH);

            allo_temp.setDuration(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            Log.i("record", "stop record");

        } else {
            startRecord();
            is_recoding = true;
            btn_record.setText("정지하기");
            tv_status.setText(ST_RECORDING);
            Log.i("record", "start record");
        }
    }

    private void clickPlay() {
        if (!is_temp) {
            Log.i("record", "temp is not exist");
            return;
        }

        if (is_playing) {
            is_playing = false;
            stopRecordFile();
            tv_status.setText(ST_STANDING);
            btn_play.setText("미리듣기");
            Log.i("record", "stop record file");

        } else {
            is_playing = true;
            playRecordFile();
            tv_status.setText(ST_PLAYING);
            btn_play.setText("정지하기");
            Log.i("record", "start record file");
        }
    }

    private void clickGetMp3() {
        ll_record.setVisibility(View.GONE);
        lv_file_list.setVisibility(View.VISIBLE);
        btn_make_allo.setVisibility(View.INVISIBLE);
        tv_menu_title.setText(ST_TITLE_FILE);
    }

    private void startRecord() {
        if (recorder != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        recorder.setOutputFile(RECORD_FILEPATH);

        try {
            recorder.prepare();
            recorder.start();
            i_total = 180000;
        } catch (Exception e) {
            System.out.println(e);
        }
        startTimer();

    }

    private void stopRecord() {
        if (recorder == null)
            return;

        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        stopTimer();


    }

    private void playRecordFile() {
        if (player != null) {
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
        player = new MediaPlayer();
        player.setLooping(false);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                clickPlay();
            }
        });

        try {
            player.setDataSource(allo_temp.getURL());
            player.prepare();
            player.start();
        } catch (Exception e) {

        }
        i_total = player.getDuration();

        startTimer();

    }

    private void stopRecordFile() {
        if (player == null)
            return;

        player.stop();
        player.reset();
        player.release();
        player = null;

        is_playing = false;

        stopTimer();


    }

    private void startTimer() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }


        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                i_progress_time = i_progress_time + 1;
                Log.i("timer", String.valueOf(i_progress_time));
                setTextProgressTime();
            }
        }, 1000, 1000);

    }


    private void stopTimer() {
        i_progress_time = 0;
        setTextProgressTime();
        if (timer != null)
            timer.cancel();
    }


    private void setTextProgressTime() {
        myHandler.post(myRunnable);
    }

    final Runnable myRunnable = new Runnable() {
        public void run() {
            AlloUtils alloUtils = AlloUtils.getInstance();
            String st_time = alloUtils.getSecToTime(i_progress_time);
            tv_time.setText(st_time);
        }
    };


    private void setAlloList() {
        al_allo_list = new ArrayList<>();


        Cursor c = getApplication().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,


                }, "1=1", null, null);

        Cursor c_album = getApplication().getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM_ART
                }, null, null, null);

        while (c.moveToNext()) {
            c_album.moveToNext();

            Allo allo = new Allo();
            allo.setTitle(c.getString(4));
            allo.setURL(c.getString(1));
            allo.setArtist(c.getString(2));
            allo.setDuration(c.getInt(5));
            allo.setStartPoint(0);
            al_allo_list.add(allo);
        }
    }

    private void clickMakeAllo() {
        if (allo_temp.getURL() == null) {
            Toast.makeText(getApplicationContext(), "알로를 녹음하거나 파일을 가져온 후 클릭해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
        intent.putExtra("allo_temp", allo_temp);
        startActivity(intent);
    }


    private void backToRecord() {

        ll_record.setVisibility(View.VISIBLE);
        lv_file_list.setVisibility(View.GONE);
        btn_make_allo.setVisibility(View.VISIBLE);
        tv_menu_title.setText(ST_TITLE_RECORD);
    }

    public void onPrepared(Allo allo){
        AlloRecordDialog alloRecordDialog = new AlloRecordDialog(this);
        alloRecordDialog.setAllo(allo);
        alloRecordDialog.show();
        lv_file_list.setEnabled(true);
    }

    public void onPrepareFailed(){
        Log.i("Record Activity", "onPreparedFailed");
        Toast.makeText(this, this.getResources().getString(R.string.prepare_fail), Toast.LENGTH_SHORT).show();
        lv_file_list.setEnabled(true);
    }


    @Override
    public void onBackPressed() {
        if (lv_file_list.getVisibility() == View.VISIBLE)
            backToRecord();
        else
            clickBack();
    }

    private void clickBack() {
        stopRecord();
        stopRecordFile();
        finish();
    }


    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

        if (timer != null)
            timer.cancel();
    }

}
