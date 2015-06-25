package com.allo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class MyAlloFragment extends Fragment {

    Allo currentAllo;

    ImageView iv_side_menu;

    TextView nameTv;

    LinearLayout mySongLayout;
    ImageView iv_my_allo;
    TextView mySongTV;
    TextView mySongArtistTV;

    ImageButton mySongInfoBtn;

    ListView alloList;


    LinearLayout playSongLayout;
    LinearLayout playSongPlayLayout;
    ImageView iv_playing;
    ImageView playSongPlayBtn;
    TextView playSongTV;



    ArrayList<Allo> ar_my_allo_list ;
    Allo myAllo;

    Context context;

    ImageLoader imageLoader;
    DisplayImageOptions options;



    public void setContext(Context context){this.context = context;}
    public Context getContext(){
        return context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_allo, container, false);

        setInstances();
        setLayout(view);
        setListener(view);


        getAlloList();


        return view;
    }

// on create
    private void setInstances(){

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("allo-state"));

        options = new DisplayImageOptions.Builder()
//                        .showImageOnLoading(R.drawable.ic_stub)
//                        .showImageForEmptyUri(R.drawable.ic_empty)
//                        .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20)).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));


    }

    private void setLayout(View v){

        alloList = (ListView)v.findViewById(R.id.friendList);

//        header view set Layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_my_allo_header, null, false);
        alloList.addHeaderView(view);

        iv_side_menu = (ImageView)v.findViewById(R.id.iv_side_menu);

        iv_my_allo = (ImageView)v.findViewById(R.id.iv_my_allo);
        nameTv = (TextView) v.findViewById(R.id.nameTV);
        mySongTV = (TextView) view.findViewById(R.id.mySongTV);
        mySongArtistTV = (TextView) view.findViewById(R.id.mySongArtistTV);

        mySongLayout = (LinearLayout) view.findViewById(R.id.mySongLayout);
        mySongInfoBtn = (ImageButton) view.findViewById(R.id.mySongInfoBtn);

        iv_playing = (ImageView)v.findViewById(R.id.iv_playing);
        playSongTV = (TextView)v.findViewById(R.id.playSongTV);

        playSongPlayBtn = (ImageView)v.findViewById(R.id.playSongPlayBtn);
        playSongPlayLayout = (LinearLayout)v.findViewById(R.id.playSongPlayLayout);
        playSongLayout = (LinearLayout)v.findViewById(R.id.playSongLayout);
    }

    private void setListener(View view){
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    ((MainActivity) context).moveFragment(0);

                }
                return true;
            }
        });

        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();

            }
        });

        playSongPlayLayout.setOnClickListener(new View.OnClickListener() {
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

        mySongLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RingbackTone ringbackTone = RingbackTone.getInstance();
                ringbackTone.setCurrentAllo(myAllo);
                playRingbackTone();
            }
        });


        alloList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RingbackTone ringbackTone = RingbackTone.getInstance();
                Log.i("list count", String.valueOf(ar_my_allo_list.size()));
                Log.i("position", String.valueOf(position-1));
                Log.i("position title", ar_my_allo_list.get(position-1).getTitle());

                ringbackTone.setCurrentAllo(ar_my_allo_list.get(position-1));
                playRingbackTone();
            }
        });
    }

    private void setMyInfoUI(){
        if (myAllo != null){

            if (myAllo.getThumbs() != null){
                imageLoader.displayImage(myAllo.getThumbs(), iv_my_allo, options);
            }

            mySongTV.setText(myAllo.getTitle());
            mySongArtistTV.setText(myAllo.getArtist());
        }
    }

    //    Ringbacktone play & pause
    public void playRingbackTone(){
        pauseRingbackTone();
        playSongLayout.setVisibility(View.VISIBLE);
        RingbackTone mRingbackTone = RingbackTone.getInstance();

        if (mRingbackTone.getCurrentAllo() != null) {

            currentAllo = mRingbackTone.getCurrentAllo();
            currentAllo.setIsPlaying(true);
            mRingbackTone.playRingbackTone();

            if (currentAllo.getThumbs() != null)
                imageLoader.displayImage(currentAllo.getThumbs(), iv_playing, options);
            else
                iv_playing.setImageResource(R.drawable.allo);

            playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);
            String st_play_title = currentAllo.getTitle()+" - "+currentAllo.getArtist();
            playSongTV.setText(st_play_title);
        }else{
            playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
            playSongTV.setText("재생할 곡을 선택하세요.");
            iv_playing.setImageResource(R.drawable.allo);
        }
    }


    public void pauseRingbackTone(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        mRingbackTone.pauseRingBackTone();
        playBarUIInit();

    }


    private void playBarUIInit(){
        playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
    }




    private void getAlloList(){
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(context);
        alloHttpUtils.getMyAlloList(this);
    }


//    call back
    public void setUI(){
        SingleToneData singleToneData = SingleToneData.getInstance();
        ar_my_allo_list = singleToneData.getMyAlloList();
        MyAlloAdapter adapter = new MyAlloAdapter(context, R.layout.layout_my_allo_item, ar_my_allo_list, this);
        alloList.setAdapter(adapter);

        myAllo = singleToneData.getMyAllo();
        setMyInfoUI();
    }

    public void completeSetMainAllo(Allo allo) {
        myAllo = allo;
        setMyInfoUI();
        SingleToneData singleToneData = SingleToneData.getInstance();
        singleToneData.setMyAllo(allo);

    }


//    Override method
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
                if (currentAllo.getThumbs() != null)
                    imageLoader.displayImage(currentAllo.getThumbs(), iv_playing, options);
                else
                    iv_playing.setImageResource(R.drawable.allo);
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
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


}
