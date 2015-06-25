package com.allo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 13..
 */
public class ByChangeActivity extends Activity {
    LinearLayout ll_back;
    ListView lv_by_change;
    TextView tv_title;

    LinearLayout ll_current_info;
    TextView tv_current_title;
    TextView tv_current_artist;

    ArrayList<Allo> ar_my_allo_list;
    Allo currentAllo;

    LinearLayout playSongLayout;

    TextView playSongTV;
    ImageView imageView;
    ImageButton playSongPlayBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_change);

        setLayout();
        setListener();
        getAlloList();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter("allo-state"));
    }

    private void setLayout() {
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        tv_title = (TextView) findViewById(R.id.tv_title);
        lv_by_change = (ListView) findViewById(R.id.lv_by_change);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v_header = inflater.inflate(R.layout.layout_by_change_header, null, false);
        lv_by_change.addHeaderView(v_header);

        ll_current_info = (LinearLayout) v_header.findViewById(R.id.ll_current_info);
        tv_current_title = (TextView) v_header.findViewById(R.id.tv_current_title);
        tv_current_artist = (TextView) v_header.findViewById(R.id.tv_current_artist);


        playSongLayout = (LinearLayout) findViewById(R.id.playSongLayout);
        playSongTV = (TextView) findViewById(R.id.playSongTV);
        imageView = (ImageView) findViewById(R.id.imageView);
        playSongPlayBtn = (ImageButton) findViewById(R.id.playSongPlayBtn);

    }

    private void setListener() {
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ll_current_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        lv_by_change.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onItemClickPlay(ar_my_allo_list.get(position - 1));
            }
        });

        playSongLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RingbackTone mRingbackTone = RingbackTone.getInstance();
                if (mRingbackTone.isPlayingNow()) {
                    pauseRingbackTone();
                } else {
                    playRingbackTone();
                }
            }
        });


    }

    private void getAlloList(){
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(getApplicationContext());
        alloHttpUtils.getMyAlloList(this);
    }

//    call back method
    public void setUI(){
        SingleToneData singleToneData = SingleToneData.getInstance();
        ar_my_allo_list = singleToneData.getMyAlloList();
        ByChangeAdapter adapter = new ByChangeAdapter(ByChangeActivity.this, R.layout.layout_by_change_item, ar_my_allo_list);
        lv_by_change.setAdapter(adapter);

        Friend current_by_friend = singleToneData.getCurrentByFriend();
        Allo current_allo = current_by_friend.getAllo();

        tv_title.setText(current_by_friend.getNickname());

        Log.i("current allo", current_allo.getTitle());


        if (current_allo != null){
            tv_current_title.setText(current_allo.getTitle());
            tv_current_artist.setText(current_allo.getArtist());
        }
    }





    //    Ringbacktone play & pause
    public void playRingbackTone(){
        pauseRingbackTone();
        playSongLayout.setVisibility(View.VISIBLE);

        if (currentAllo != null){
            currentAllo.setIsPlaying(true);
            RingbackTone mRingbackTone = RingbackTone.getInstance();
            mRingbackTone.playRingbackTone(currentAllo.getURL());
            mRingbackTone.setCurrentAllo(currentAllo);

            playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);
            String st_play_title = currentAllo.getTitle()+" - "+currentAllo.getArtist();
            playSongTV.setText(st_play_title);
        }else{
            playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
            playSongTV.setText("재생할 곡을 선택하세요.");
        }
    }


    public void pauseRingbackTone(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        mRingbackTone.pauseRingBackTone();
        playBarUIInit();
    }

    //    listView Click Listener
    public void onItemClickPlay(Allo allo) {
        currentAllo = allo;
        playRingbackTone();
    }

    //    Play Bar UI init
    public void playBarUIInit(){
        playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
    }


    @Override
    public void onResume() {
        super.onResume();
        setResumePlayBarUI();
    }

    private void setResumePlayBarUI(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        if (mRingbackTone.getCurrentAllo() != null) {
            currentAllo = mRingbackTone.getCurrentAllo();
            String play_title = currentAllo.getTitle() + " - " + currentAllo.getArtist();
            playSongTV.setText(play_title);
            playSongLayout.setVisibility(View.VISIBLE);

            boolean isPlaying = mRingbackTone.isPlayingNow();
            if (isPlaying){
                playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);

            }
            else
                playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
        }
    }

    //    play complete listener method
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.i("receiver", "stop stop stop");
            String message = intent.getStringExtra("message");
            if (message.equals("stop")){
                playBarUIInit();
            }
        }
    };

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        // This is somewhat like [[NSNotificationCenter defaultCenter] removeObserver:name:object:]
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
