package com.allo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by baek_uncheon on 2015. 3. 25..
 */
public class HomeFragment extends Fragment {
    Context context;

    LinearLayout mySongPlayLayout;
    LinearLayout ll_more;

    ImageView iv_my_allo;

    ScrollView sv_home;

    TextView myNameTV;
    TextView mySongTV;
    TextView mySongArtistTV;


    Allo currentAllo;

    LinearLayout playSongLayout;
    LinearLayout playSongPlayLayout;


    TextView playSongTV;
    ImageView iv_playing;
    ImageView playSongPlayBtn;

    ImageView iv_side_munu;



    TextView tv_current;

    TextView tv_store_first;
    TextView tv_store_second;
    TextView tv_store_third;


    static final int NUM_ITEMS = 3;


    CirclePageIndicator cpi_desc;
    ViewPager vp_allo_info;
    DescViewPagerAdapter vpa_allo_info;

    LinearLayout ll_store;

    ViewPager vp_store;
    StoreViewPagerAdapter vpa_store;

    HomeAlloDescFragment homeAlloDescFragment0;
    HomeAlloDescFragment homeAlloDescFragment1;
    HomeAlloDescFragment homeAlloDescFragment2;

    HomeStoreFirstFragment homeStoreFirstFragment;
    HomeStoreSecondFragment homeStoreSecondFragment;
    HomeStoreThirdFragment homeStoreThirdFragment;

    ImageLoader imageLoader;
    DisplayImageOptions options;

    final Handler handler = new Handler();
    final Runnable changeView = new Runnable() {
        @Override
        public void run() {
            changeViewPagerAlloInfo();
            handler.postDelayed(this, 3000);
        }
    };

    public void setContext(Context context){this.context = context;}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        setInstances();
        setLayout(view);
        setListener();
        setUI();


        return view;
    }



    private void setInstances(){
        homeAlloDescFragment0 = new HomeAlloDescFragment();
        homeAlloDescFragment0.setContext(context);
        homeAlloDescFragment0.setTurn(0);

        homeAlloDescFragment1 = new HomeAlloDescFragment();
        homeAlloDescFragment1.setTurn(1);
        homeAlloDescFragment1.setContext(context);

        homeAlloDescFragment2 = new HomeAlloDescFragment();
        homeAlloDescFragment2.setTurn(2);
        homeAlloDescFragment2.setContext(context);



        homeStoreFirstFragment = new HomeStoreFirstFragment();
        homeStoreFirstFragment.setContext(context);
        homeStoreFirstFragment.setHomeFragment(this);

        homeStoreSecondFragment = new HomeStoreSecondFragment();
        homeStoreSecondFragment.setContext(context);
        homeStoreSecondFragment.setHomeFragment(this);

        homeStoreThirdFragment = new HomeStoreThirdFragment();
        homeStoreThirdFragment.setContext(context);
        homeStoreThirdFragment.setHomeFragment(this);

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

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("allo-state"));
    }


    private void setLayout(View v){

        iv_side_munu = (ImageView) v.findViewById(R.id.iv_side_menu);

        sv_home = (ScrollView) v.findViewById(R.id.sv_home);

        mySongPlayLayout = (LinearLayout) v.findViewById(R.id.mySongPlayLayout);
        ll_more = (LinearLayout) v.findViewById(R.id.ll_more);
        iv_my_allo = (ImageView) v.findViewById(R.id.iv_my_allo);
        mySongTV = (TextView) v.findViewById(R.id.mySongTV);
        mySongArtistTV = (TextView) v.findViewById(R.id.mySongArtistTV);

        playSongLayout = (LinearLayout) v.findViewById(R.id.playSongLayout);
        playSongPlayLayout = (LinearLayout) v.findViewById(R.id.playSongPlayLayout);
        playSongTV = (TextView) v.findViewById(R.id.playSongTV);
        iv_playing = (ImageView) v.findViewById(R.id.iv_playing);
        playSongPlayBtn = (ImageView) v.findViewById(R.id.playSongPlayBtn);

        tv_store_first = (TextView)v.findViewById(R.id.tv_store_first);
        tv_store_second = (TextView)v.findViewById(R.id.tv_store_second);
        tv_store_third = (TextView)v.findViewById(R.id.tv_store_third);

        tv_current = tv_store_first;


        vpa_allo_info = new DescViewPagerAdapter(getChildFragmentManager());
        vp_allo_info = (ViewPager) v.findViewById(R.id.vp_allo_info);
        vp_allo_info.setAdapter(vpa_allo_info);

        cpi_desc = (CirclePageIndicator)v.findViewById(R.id.cpi_desc);
        cpi_desc.setViewPager(vp_allo_info);

        ll_store = (LinearLayout)v.findViewById(R.id.ll_store);

        vpa_store = new StoreViewPagerAdapter(getChildFragmentManager());
        vp_store = (ViewPager) v.findViewById(R.id.vp_store);
        vp_store.setAdapter(vpa_store);
    }

    private void setListener(){
        iv_side_munu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();
            }
        });

//        ll_more.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((MainActivity) context).moveFragment(3);
//            }
//        });

        mySongPlayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleToneData singleToneData = SingleToneData.getInstance();
                RingbackTone ringbackTone = RingbackTone.getInstance();
                ringbackTone.setCurrentAllo(singleToneData.getMyAllo());
                playRingbackTone();
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


        tv_store_first.setOnClickListener(categoryListener);
        tv_store_second.setOnClickListener(categoryListener);
        tv_store_third.setOnClickListener(categoryListener);

        vp_store.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        clickStoreFirst();
                        break;
                    case 1:
                        clickStoreSecond();
                        break;
                    case 2:
                        clickStoreThird();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    View.OnClickListener categoryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_store_first:
                    if (tv_current != tv_store_first) {
                        clickStoreFirst();
                    }
                    break;
                case R.id.tv_store_second:
                    if (tv_current != tv_store_second) {
                        clickStoreSecond();
                    }
                    break;
                case R.id.tv_store_third:
                    if (tv_current != tv_store_third)
                        clickStoreThird();
                    break;
            }
        }
    };

    private void clickStoreFirst(){
        int i_sv_position = sv_home.getScrollY();

        tv_current = tv_store_first;
        tv_store_first.setBackgroundResource(R.drawable.border_bottom_red);
        tv_store_first.setTextColor(Color.parseColor("#f91e2f"));

        tv_store_second.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_second.setTextColor(Color.parseColor("#000000"));

        tv_store_third.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_third.setTextColor(Color.parseColor("#000000"));

        vp_store.setCurrentItem(0, true);

        sv_home.setScrollY(i_sv_position);


    }

    private void clickStoreSecond(){
        tv_current = tv_store_second;

        tv_store_first.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_first.setTextColor(Color.parseColor("#000000"));

        tv_store_second.setBackgroundResource(R.drawable.border_bottom_red);
        tv_store_second.setTextColor(Color.parseColor("#f91e2f"));

        tv_store_third.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_third.setTextColor(Color.parseColor("#000000"));

        vp_store.setCurrentItem(1, true);
    }

    private void clickStoreThird(){
        tv_current = tv_store_third;

        tv_store_first.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_first.setTextColor(Color.parseColor("#000000"));

        tv_store_second.setBackgroundResource(R.drawable.grey_border_flat);
        tv_store_second.setTextColor(Color.parseColor("#000000"));

        tv_store_third.setBackgroundResource(R.drawable.border_bottom_red);
        tv_store_third.setTextColor(Color.parseColor("#f91e2f"));

        vp_store.setCurrentItem(2, true);
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
        playSongLayout.setVisibility(View.GONE);

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



    private void setUI(){
        handler.postDelayed(changeView, 2000);


        SingleToneData singleToneData = SingleToneData.getInstance();
        Friend myInfo = singleToneData.getMyInfo();


        if (myInfo.getAllo() != null){
            Allo myAllo = myInfo.getAllo();
            mySongTV.setText(myAllo.getTitle());
            mySongArtistTV.setText(myAllo.getArtist());


            if (myAllo.getThumbs() != null){
                imageLoader.displayImage(myAllo.getThumbs(), iv_my_allo, options);
            }

        }

//        myNameTV.setText(myInfo.getNickname());

    }

    private void changeViewPagerAlloInfo(){
        int i_item = vp_allo_info.getCurrentItem();
        if (i_item < vp_allo_info.getChildCount() - 1) {
            i_item++;
            vp_allo_info.setCurrentItem(i_item, true);
        } else {
            vp_allo_info.setCurrentItem(0, true);
        }
    }

//    call back method
    public void setLinearStoreHeight(int height) {

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)vp_store.getLayoutParams();
        params.height = height;
        ll_store.setLayoutParams(params);
    }



    @Override
    public void onPause(){
        Log.i("onPause", "onPause");
        super.onPause();
    }
    @Override
    public void onStop(){
        Log.i("onStop", "onStop");
        super.onStop();
    }

    @Override
    public void onStart(){
        Log.i("onStart", "onStart");
        super.onStart();
    }





    @Override
    public void onResume() {
        Log.i("onResume", "view");
        super.onResume();
        setResumePlayBarUI();
    }

    private void setResumePlayBarUI(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        if (mRingbackTone.getCurrentAllo() != null) {
            currentAllo = mRingbackTone.getCurrentAllo();
            String play_title = currentAllo.getTitle() + " - " + currentAllo.getArtist();
            playSongTV.setText(play_title);


            boolean isPlaying = mRingbackTone.isPlayingNow();
            if (isPlaying){
                playSongLayout.setVisibility(View.VISIBLE);

                playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);
                if (currentAllo.getThumbs() != null)
                    imageLoader.displayImage(currentAllo.getThumbs(), iv_playing, options);
                else
                    iv_playing.setImageResource(R.drawable.allo);
            }
            else{
                playSongLayout.setVisibility(View.GONE);
                playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
            }

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
        Log.i("onDestroy", "view");
        // Unregister since the activity is about to be closed.
        // This is somewhat like [[NSNotificationCenter defaultCenter] removeObserver:name:object:]
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        handler.removeCallbacks(changeView);
//        timer.cancel();
        super.onDestroy();
    }

    public ViewParent getScrollView() {
        return sv_home;
    }


    public class DescViewPagerAdapter extends FragmentPagerAdapter {
        public DescViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int num) {
            switch (num) {
                case 0:
                    return homeAlloDescFragment0;
                case 1:
                    return homeAlloDescFragment1;
                case 2:
                    return homeAlloDescFragment2;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

    public class StoreViewPagerAdapter extends FragmentPagerAdapter {
        public StoreViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int num) {
            switch (num) {
                case 0:
                    return homeStoreFirstFragment;
                case 1:
                    return homeStoreSecondFragment;
                case 2:
                    return homeStoreThirdFragment;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}