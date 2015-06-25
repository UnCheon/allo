package com.allo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.urqa.clientinterface.URQAController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class StoreFragment extends Fragment{
    public ViewPager mPager;
    FrameLayout frameLayout;
    Context mContext;

    ImageView iv_side_menu;

    ListView rankLv;
    ListView newLv;

    ArrayList<Allo> allo_rank_array;
//    ListView searchLv;
    LinearLayout searchLayout;

    TextView currentBtn;
    TextView popularBtn;
    TextView newBtn;
    TextView searchBtn;

    TextView playSongTV;
    ImageView imageView;

    LinearLayout playSongLayout;
    ImageButton playSongPlayBtn;


    Button btn_fee_current;
    Button btn_charged;
    Button btn_free;

    String st_url;

    Allo currentAllo;

    public StoreFragment(){

    }

    public void setContext(Context context){this.mContext = context;}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);
        URQAController.InitializeAndStartSession(mContext, "BEAC46A7");


        currentAllo = new Allo();
        currentAllo.setRingTitle("none");
        setLayout(view);
        setListener();

        btnCharged();

        return view;
    }

    private void setLayout(View view){
        iv_side_menu = (ImageView) view.findViewById(R.id.iv_side_menu);

        btn_charged = (Button) view.findViewById(R.id.btn_charged);
        btn_free = (Button) view.findViewById(R.id.btn_free);

        playSongLayout = (LinearLayout)view.findViewById(R.id.playSongLayout);
        frameLayout = (FrameLayout)view.findViewById(R.id.frameLayout);
        rankLv = (ListView)view.findViewById(R.id.rankLv);
        newLv = (ListView)view.findViewById(R.id.newLv);
        searchLayout = (LinearLayout)view.findViewById(R.id.searchLayout);

        popularBtn = (TextView)view.findViewById(R.id.popularBtn);
        newBtn = (TextView)view.findViewById(R.id.newBtn);
        searchBtn = (TextView)view.findViewById(R.id.searchBtn);

        playSongTV = (TextView)view.findViewById(R.id.playSongTV);
        imageView = (ImageView)view.findViewById(R.id.imageView);
        playSongPlayBtn = (ImageButton)view.findViewById(R.id.playSongPlayBtn);

        currentBtn = popularBtn;


    }

    private void setListener(){

        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mContext).openDrawerLayout();
            }
        });

        btn_charged.setOnClickListener(feeListener);
        btn_free.setOnClickListener(feeListener);

        popularBtn.setOnClickListener(categoryListener);
        newBtn.setOnClickListener(categoryListener);
        searchBtn.setOnClickListener(categoryListener);
        playSongLayout.setOnClickListener(playListener);
        playSongPlayBtn.setOnClickListener(playListener);
    }


    View.OnClickListener categoryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.popularBtn:
                    if (currentBtn != popularBtn) {
                        popularBtn();
                        getAlloStoreList();
                    }
                    break;
                case R.id.newBtn:
                    if (currentBtn != newBtn) {
                        newBtn();
                        getAlloStoreList();
                    }
                    break;
                case R.id.searchBtn:
                    if (currentBtn != searchBtn)
                        searchBtn();
                    break;
            }
        }
    };

    View.OnClickListener feeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_charged:
                    btnCharged();
                    break;
                case R.id.btn_free:
                    btnFree();
                    break;

            }
            popularBtn();
            getAlloStoreList();
        }
    };

    private void btnCharged(){
        if (btn_fee_current == btn_charged)
            return;
        btn_fee_current = btn_charged;
        btn_charged.setBackgroundColor(Color.parseColor("#f91e2f"));
        btn_free.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    private void btnFree(){
        if (btn_fee_current == btn_free)
            return;
        btn_fee_current = btn_free;
        btn_charged.setBackgroundColor(Color.parseColor("#ffffff"));
        btn_free.setBackgroundColor(Color.parseColor("#f91e2f"));
    }

    private void popularBtn(){
        currentBtn = popularBtn;
        popularBtn.setBackgroundResource(R.drawable.border_bottom_red);
        popularBtn.setTextColor(Color.parseColor("#f91e2f"));
        rankLv.setVisibility(TextView.VISIBLE);

        newBtn.setBackgroundResource(R.drawable.grey_border_flat);
        newBtn.setTextColor(Color.parseColor("#000000"));
//        newLv.setVisibility(TextView.GONE);

        searchBtn.setBackgroundResource(R.drawable.grey_border_flat);
        searchBtn.setTextColor(Color.parseColor("#000000"));
        searchLayout.setVisibility(TextView.GONE);
    }

    private void newBtn(){
        currentBtn = newBtn;
        popularBtn.setBackgroundResource(R.drawable.grey_border_flat);
        popularBtn.setTextColor(Color.parseColor("#000000"));
//        rankLv.setVisibility(TextView.GONE);

        newBtn.setBackgroundResource(R.drawable.border_bottom_red);
        newBtn.setTextColor(Color.parseColor("#f91e2f"));
        rankLv.setVisibility(TextView.VISIBLE);

        searchBtn.setBackgroundResource(R.drawable.grey_border_flat);
        searchBtn.setTextColor(Color.parseColor("#000000"));
        searchLayout.setVisibility(TextView.GONE);
    }

    private void searchBtn(){
        currentBtn = searchBtn;
        popularBtn.setBackgroundResource(R.drawable.grey_border_flat);
        popularBtn.setTextColor(Color.parseColor("#000000"));
        rankLv.setVisibility(TextView.GONE);

        newBtn.setBackgroundResource(R.drawable.grey_border_flat);
        newBtn.setTextColor(Color.parseColor("#000000"));
//        newLv.setVisibility(TextView.GONE);

        searchBtn.setBackgroundResource(R.drawable.border_bottom_red);
        searchBtn.setTextColor(Color.parseColor("#f91e2f"));
        searchLayout.setVisibility(TextView.VISIBLE);
    }


    private void getAlloStoreList(){
        if (btn_fee_current == btn_charged){
            if (currentBtn == popularBtn){
                st_url = mContext.getString(R.string.url_store_charged_rank);
            }else if (currentBtn == newBtn){
                st_url = mContext.getString(R.string.url_store_charged_new);
            }

        }else if (btn_fee_current == btn_free){
            if (currentBtn == popularBtn){
                st_url = mContext.getString(R.string.url_store_free_rank);
            }else if (currentBtn == newBtn){
                st_url = mContext.getString(R.string.url_store_free_new);
            }
        }

        Log.i("get Allo list url", st_url);

        setListView();

    }

    private void setListView(){
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(mContext);
        alloHttpUtils.getAlloStoreList(this, st_url);
    }


// call back
    public void setRankAdapter(ArrayList<Allo> allo_array){
        allo_rank_array = allo_array;
        StoreAdapter adapter = new StoreAdapter(mContext, R.layout.layout_store_item, allo_rank_array);
        rankLv.setAdapter(adapter);
        rankLv.setOnItemClickListener(rankItemClickListener);
    }


    AdapterView.OnItemClickListener rankItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            currentAllo = allo_rank_array.get(position);
            playRingbackTone();
        }
    };

    public void playRingbackTone(){
        pauseRingbackTone();
        playSongLayout.setVisibility(View.VISIBLE);

        if (currentAllo.getTitle().equals("none")) {
        }else{
            Log.i("allo url", currentAllo.getURL());
            RingbackTone mRingbackTone = RingbackTone.getInstance();
            mRingbackTone.playRingbackTone(currentAllo.getURL());
            mRingbackTone.setCurrentAllo(currentAllo);

            playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);
            String st_play_title = currentAllo.getTitle()+" - "+currentAllo.getArtist();
            playSongTV.setText(st_play_title);
        }
    }

    public void pauseRingbackTone(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        mRingbackTone.pauseRingBackTone();
        playBarUIInit();
    }
    //    UI init
    public void playBarUIInit(){
        playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
    }

    //     onClick method
    View.OnClickListener playListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RingbackTone mRingbackTone = RingbackTone.getInstance();
            if (mRingbackTone.isPlayingNow()){
                pauseRingbackTone();
            }else{
                playRingbackTone();
            }
        }
    };

    public void playSongPlayBtn(View v){

    }


    @Override
    public void onResume(){
        super.onResume();
        setResumePlayBarUI();
    }

    public void setResumePlayBarUI(){
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


}
