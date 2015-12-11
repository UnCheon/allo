package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


//import com.viewpagerindicator.CirclePageIndicator;


/**
 * Created by baek_uncheon on 2015. 3. 25..
 */
public class MainFragment extends Fragment {
    Context context;

    private MaterialViewPager mViewPager;

    LinearLayout ll_cash;

    TextView tv_cach;
    TextView tv_left_time;


    StoreFragment storeRankUCCFragment;
    StoreFragment storeNewUCCFragment;
    StoreFragment storeRankMusicFragment;
    StoreFragment storeNewMusicFragment;

    StoreFragment currentFragment;


    List<Fragment> list_noti_framents;
    List<StoreFragment> list_store_fragments;

    ViewPager viewPager = null;
    TimerTask timerTask = null;
    Timer timer = null;

    private Toolbar toolbar;

    Tracker mTracker;

    long l_page_chage_time = 0;


    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "view");
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("MainFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setFragment();
        setMenu(view);
        setHeaderLayout(view);
        setViewPager(view);


        return view;
    }

    private void setFragment() {
        list_store_fragments = new ArrayList<>();

        storeRankUCCFragment = StoreFragment.newInstance();
        storeRankUCCFragment.setContext(context);
        storeRankUCCFragment.setMainFragment(MainFragment.this);
        storeRankUCCFragment.setType("popular_ucc");

        storeNewUCCFragment = StoreFragment.newInstance();
        storeNewUCCFragment.setContext(context);
        storeNewUCCFragment.setMainFragment(MainFragment.this);
        storeNewUCCFragment.setType("new_ucc");

        storeRankMusicFragment = StoreFragment.newInstance();
        storeRankMusicFragment.setContext(context);
        storeRankMusicFragment.setMainFragment(MainFragment.this);
        storeRankMusicFragment.setType("popular_music");

        storeNewMusicFragment = StoreFragment.newInstance();
        storeNewMusicFragment.setContext(context);
        storeNewMusicFragment.setMainFragment(MainFragment.this);
        storeNewMusicFragment.setType("new_music");

        list_store_fragments.add(storeRankMusicFragment);
        list_store_fragments.add(storeNewMusicFragment);
        list_store_fragments.add(storeRankUCCFragment);
        list_store_fragments.add(storeNewUCCFragment);
    }

    private void setMenu(View view) {
        ImageView iv_side_menu = (ImageView) view.findViewById(R.id.iv_side_menu);
        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });

        ImageView iv_search = (ImageView) view.findViewById(R.id.iv_search);
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SearchActivity.class);
                startActivity(intent);

            }
        });

    }


    private void setViewPager(View view) {
        mViewPager = (MaterialViewPager) view.findViewById(R.id.materialViewPager);
        StoreViewPagerAdapter adapter = new StoreViewPagerAdapter(getChildFragmentManager());

        mViewPager.getViewPager().setAdapter(adapter);

        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.MaterialViewPagerListener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {

                return null;
            }
        });

        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        mViewPager.getViewPager().setCurrentItem(0);
        currentFragment = list_store_fragments.get(0);

        mViewPager.getViewPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (SingleToneData.getInstance().is_scrolled){
                    Log.i("single tone ", "true");
                    for (int i = 0; i < list_store_fragments.size() ; i++){
                        if (list_store_fragments.get(i) != currentFragment){
                            list_store_fragments.get(i).setScrollY(currentFragment.getScrollY());
                            Log.i("single tone ", "set scroll y");
                        }
                    }
                }

                currentFragment = list_store_fragments.get(position);

                SingleToneData.getInstance().setIsScrolled(false);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        PagerSlidingTabStrip pagerTitleStrip = mViewPager.getPagerTitleStrip();


        pagerTitleStrip.setIndicatorColorResource(R.color.allo_red);
        pagerTitleStrip.setBackgroundResource(R.color.white);
        pagerTitleStrip.setTabBackground(R.color.white);
        pagerTitleStrip.setTypeface(null, Typeface.NORMAL);
        pagerTitleStrip.setTextColor(Color.parseColor("#000000"));
        float scale = getResources().getDisplayMetrics().density;

        pagerTitleStrip.setTextSize((int)(13*scale));

        mViewPager.getViewPager().setBackgroundResource(R.color.white);
        mViewPager.setBackgroundResource(R.color.white);


        pagerTitleStrip.setIndicatorHeight(3);


        Log.i("title strip height", String.valueOf(pagerTitleStrip.getHeight()));
        toolbar = mViewPager.getToolbar();
        toolbar.setVisibility(View.GONE);

    }

    private void setHeaderLayout(final View view) {

        list_noti_framents = new ArrayList<>();

        ArrayList<Notice> ar_notice_list = SingleToneData.getInstance().getNoticeList();
        if (ar_notice_list == null) {
            LoginUtils loginUtils = new LoginUtils(context){
                @Override
                public void onLoginSuccess(){
                    setHeaderLayout(view);
                }

                @Override
                public void onLoginFailure(){
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                    alert_confirm.setTitle("").setMessage(context.getText(R.string.on_failure)).setCancelable(false).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((MainActivity)context).finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();

                }
            };

            String st_id = loginUtils.getId();
            String st_pw = loginUtils.getPw();
            loginUtils.login(st_id, st_pw, "", "");

        }

        for (int i = 0; i < ar_notice_list.size(); i++) {
            NoticeFragment noticeFragment = new NoticeFragment();
            noticeFragment.setContext(context);
            noticeFragment.setNotice(ar_notice_list.get(i));
            list_noti_framents.add(noticeFragment);
        }

        viewPager = (ViewPager) view.findViewById(R.id.vp_noti);
        NotiPagerAdapter notiPagerAdapter = new NotiPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(notiPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                l_page_chage_time = System.currentTimeMillis();
            }
        });

        CirclePageIndicator cpi_noti = (CirclePageIndicator)view.findViewById(R.id.cpi_noti);
        cpi_noti.setFillColor(getResources().getColor(R.color.allo_red));
        cpi_noti.setPageColor(getResources().getColor(R.color.aaa));
        cpi_noti.setStrokeColor(getResources().getColor(R.color.transparent));
        cpi_noti.setViewPager(viewPager);


        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);

            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 3000,3000);


        ll_cash = (LinearLayout) view.findViewById(R.id.ll_cash);
        ll_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Help").setAction("help_ll click").build());
                Intent intent = new Intent(context, HelpFragmentActivity.class);
                intent.putExtra("index", "cash");
                startActivity(intent);
            }
        });

        tv_cach = (TextView) view.findViewById(R.id.tv_cach);
        tv_left_time = (TextView) view.findViewById(R.id.tv_left_time);

        tv_cach.setText(SingleToneData.getInstance().getCash());

        String st_left_day = "0";

        int i_left_day = SingleToneData.getInstance().getLeftTime() / 24;

        if (i_left_day < 1) {
            st_left_day = String.valueOf(SingleToneData.getInstance().getLeftTime()) + "시간 후 충전";
        } else {
            st_left_day = String.valueOf(i_left_day) + "일 후 충전";
        }


        tv_left_time.setText(st_left_day);
    }


    public class NotiPagerAdapter extends FragmentPagerAdapter {
        public NotiPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int num) {
            return list_noti_framents.get(num);
        }

        @Override
        public int getCount() {
            return list_noti_framents.size();
        }
    }


    public class StoreViewPagerAdapter extends FragmentPagerAdapter {
        public StoreViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return list_store_fragments.get(0);
                case 1:
                    return list_store_fragments.get(1);
                case 2:
                    return list_store_fragments.get(2);
                case 3:
                    return list_store_fragments.get(3);
                default:
                    return storeRankMusicFragment;
            }
        }

        @Override
        public int getCount() {
            return list_store_fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return " 인기앨범 ";
                case 1:
                    return " 신규앨범 ";
                case 2:
                    return "Best UCC";
                case 3:
                    return "New UCC";
            }
            return "";
        }


    }

    public void onReload() {
        tv_cach.setText(SingleToneData.getInstance().getCash());

        Log.i("reload", SingleToneData.getInstance().getCash());
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (System.currentTimeMillis() - l_page_chage_time > 2900){
                switch (msg.what){
                    case 0:
                        if (viewPager.getCurrentItem() < viewPager.getChildCount())
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                        else
                            viewPager.setCurrentItem(0);
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (timer != null)
            timer.cancel();
    }


    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }

    @Override
    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity)context);
    }
}