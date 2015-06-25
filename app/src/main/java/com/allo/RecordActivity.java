package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

import skd.androidrecording.audio.AudioPlaybackManager;
import skd.androidrecording.audio.AudioRecordingHandler;
import skd.androidrecording.audio.AudioRecordingThread;
import skd.androidrecording.video.PlaybackHandler;
import skd.androidrecording.visualizer.renderer.BarGraphRenderer;


public class RecordActivity extends Activity {

    LinearLayout ll_back;

    TextView tv_title;

    LinearLayout ll_record;
    TextView tv_status;
    TextView tv_play;
    TextView tv_record;
    TextView tv_time;
    TextView btn_get_mp3;
    TextView btn_make_allo;
    ProgressBar pb_play;
    public volatile Thread th_progressbar_play;
    public int CurrentPosition = 0;
    public int i_total;

    ListView lv_file_list;

    ArrayList<Allo> al_allo_list;


    MediaPlayer player;
    MediaRecorder recorder;

    private boolean is_playing = false, is_recoding = false, is_temp = false;

    private String RECORD_FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo/temp.3gp";

    private final String ST_TITLE_RECORD = "녹음하기";
    private final String ST_TITLE_FILE = "MP3파일 업로드하기";

    Allo allo_temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        setLayout();
        setListener();
        setAlloList();
        UploadAdapter adapter = new UploadAdapter(getApplicationContext(), R.layout.layout_upload_item, al_allo_list);
        lv_file_list.setAdapter(adapter);

        allo_temp = new Allo();

    }

    private void setLayout() {

        ll_back = (LinearLayout) findViewById(R.id.ll_back);

        tv_title = (TextView) findViewById(R.id.tv_title);

        ll_record = (LinearLayout) findViewById(R.id.ll_record);
        tv_status = (TextView)findViewById(R.id.tv_status);
        tv_play = (TextView)findViewById(R.id.tv_play);
        tv_record = (TextView)findViewById(R.id.tv_record);
        tv_time = (TextView) findViewById(R.id.tv_time);
        btn_get_mp3 = (Button) findViewById(R.id.btn_get_mp3);
        btn_make_allo = (Button) findViewById(R.id.btn_make_allo);
        pb_play = (ProgressBar) findViewById(R.id.pb_play);

        lv_file_list = (ListView) findViewById(R.id.lv_file_list);
    }

    private void setListener() {
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBack();
            }
        });

        tv_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRecord();
            }
        });

        tv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPlay();
            }
        });

        btn_get_mp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickGetMp3();
            }
        });

        btn_make_allo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMakeAllo();
            }
        });

        lv_file_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                is_temp = true;
                allo_temp = al_allo_list.get(position);
                Log.i("allo_title", allo_temp.getTitle());
                Log.i("allo_artist", allo_temp.getArtist());
                Log.i("allo_url", allo_temp.getURL());


                backToRecord();
            }
        });

    }

    private void clickRecord(){
        if (is_recoding){
            stopRecord();
            is_recoding = false;
            is_temp = true;

            tv_record.setText("다시 녹음하기");

            allo_temp.setTitle("녹음파일");
            allo_temp.setArtist("");
            allo_temp.setImage("");
            allo_temp.setURL(RECORD_FILEPATH);
            Log.i("record", "stop record");

        }else{
            startRecord();
            is_recoding = true;
            tv_record.setText("정지하기");
            Log.i("record", "start record");
        }
    }

    private void clickPlay(){
        if (!is_temp) {
            Log.i("record", "temp is not exist");
            return;
        }

        if (is_playing){
            is_playing = false;
            stopRecordFile();
            tv_play.setText("미리듣기");
            Log.i("record", "stop record file");

        }else{
            is_playing = true;
            playRecordFile();
            tv_play.setText("정지하기");
            Log.i("record", "start record file");
        }
    }

    private void clickGetMp3(){
        ll_record.setVisibility(View.GONE);
        lv_file_list.setVisibility(View.VISIBLE);
        tv_title.setText(ST_TITLE_FILE);
    }

    private void startRecord(){
        if (recorder != null){
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

        try{
            recorder.prepare();
            recorder.start();
            i_total = 180000;
            startProgressBarThread();
        }catch (Exception e){
            System.out.println(e);
        }

    }

    private void stopRecord(){
        if (recorder == null)
            return;

        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        stopProgressBarThread();



    }

    private void playRecordFile(){
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

        try{
            player.setDataSource(allo_temp.getURL());
            player.prepare();
            player.start();
        }catch (Exception e){

        }
        i_total = player.getDuration();
        startProgressBarThread();
    }
    private void stopRecordFile(){
        if (player == null)
            return;

        player.stop();
        player.reset();
        player.release();
        player = null;

        is_playing = false;
        stopProgressBarThread();

    }

    public synchronized void startProgressBarThread(){
        if (th_progressbar_play == null) {
            th_progressbar_play = new Thread(null, backgroundThread, "startOrigressBarThread");
            CurrentPosition = 0;

            pb_play.setMax(i_total);
            th_progressbar_play.start();
        }
    }

    public synchronized void stopProgressBarThread(){
        CurrentPosition = 0;
        pb_play.setProgress(0);
        setTimeText();

        if (th_progressbar_play != null) {
            Thread tmpThread = th_progressbar_play;
            th_progressbar_play = null;
            tmpThread.interrupt();
        }
    }

    private Runnable backgroundThread = new Runnable() {
        @Override
        public void run() {
            if (Thread.currentThread() == th_progressbar_play){
                CurrentPosition = 0;
                while (CurrentPosition < i_total) {
                    try{
                        progressBarHandler.sendMessage(progressBarHandler.obtainMessage());
                        Thread.sleep(100);
                    }catch (final InterruptedException e) {
                        return;
                    }catch (final Exception e) {

                    }
                }
            }
        }
    };

    Handler progressBarHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            CurrentPosition = CurrentPosition + 100;
            pb_play.setProgress(CurrentPosition);
            if (CurrentPosition%1000 == 0){
                setTimeText();

            }
            if (CurrentPosition > i_total | CurrentPosition == i_total){
                stopProgressBarThread();
            }
        }
    };

    private void setTimeText(){
        String st_time;
        if (CurrentPosition == 0){
            st_time = "00:00";
        }else{
            int i_total_sec = CurrentPosition/1000;
            int i_min = i_total_sec/60;
            int i_sec = i_total_sec%60;
            String st_min = String.valueOf(i_min);
            String st_sec = String.valueOf(i_sec);

            if (i_min/10 == 0){
                st_min = "0"+st_min;
            }
            if (i_sec/10 == 0){
                st_sec = "0"+st_sec;
            }
            st_time = st_min+":"+st_sec;
        }
        tv_time.setText(st_time);
    }

    private void setAlloList(){
        al_allo_list = new ArrayList<>();

        Cursor c = getApplication().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME,

                        }, "1=1", null, null);

        Cursor c_album = getApplication().getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM_ART
                }, null, null, null);

        while (c.moveToNext()){
            c_album.moveToNext();

            Allo allo= new Allo();
            allo.setTitle(c.getString(4));
            allo.setURL(c.getString(1));
            allo.setArtist(c.getString(2));
            allo.setIsPlaying(false);
            al_allo_list.add(allo);
        }
    }

    private void clickMakeAllo(){
        if (allo_temp.getTitle() == null){
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
        tv_title.setText(ST_TITLE_RECORD);
    }


    @Override
    public void onBackPressed() {
        String st_title = tv_title.getText().toString();
        if (st_title.equals(ST_TITLE_FILE))
            backToRecord();
        else if (st_title.equals(ST_TITLE_RECORD))
            clickBack();
    }

    private void clickBack() {
        stopRecord();
        stopRecordFile();
        finish();
    }
}
