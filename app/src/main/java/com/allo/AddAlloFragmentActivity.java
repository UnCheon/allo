package com.allo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import com.urqa.clientinterface.URQAController;

/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class AddAlloFragmentActivity extends FragmentActivity {
    static final int NUM_ITEMS = 3;

    Context mContext = this;

    ViewPager mPager;
    SlidePagerAdapter mPagerAdapter;

    StoreFragment mStoreFragment;
    UploadFragment mUploadFragment;
    RecordFragment mFragmentRecord;

    ImageButton storeBtn;
    ImageButton uploadBtn;
    ImageButton recordBtn;

    int status = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_add_allo);
        URQAController.InitializeAndStartSession(getApplicationContext(), "BEAC46A7");


        mPager = (ViewPager)findViewById(R.id.pager);
        mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mStoreFragment = new StoreFragment();
        mUploadFragment = new UploadFragment();
        mFragmentRecord = new RecordFragment();

        mStoreFragment.setContext(mContext);
        mUploadFragment.setContext(mContext);
        mFragmentRecord.setContext(mContext);


        setLayout();
        setListener();
    }

    private void setLayout(){
        storeBtn = (ImageButton)findViewById(R.id.storeBtn);
        uploadBtn = (ImageButton)findViewById(R.id.uploadBtn);
        recordBtn = (ImageButton)findViewById(R.id.recordBtn);
    }
    private void setListener(){
        storeBtn.setOnClickListener(addOnClickListener);
        uploadBtn.setOnClickListener(addOnClickListener);
        recordBtn.setOnClickListener(addOnClickListener);
        mPager.setOnPageChangeListener(pageChangeListener);
    }

    private View.OnClickListener addOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.storeBtn:
                    if (status == 0){
                        break;
                    }
                    else{
                        status = 0;
                        clickStore();
                        mPager.setCurrentItem(0, true);
                    }
                    break;
                case R.id.uploadBtn:
                    if (status == 1){
                        break;
                    }
                    else{
                        status =1;
                        clickUpload();
                        mPager.setCurrentItem(1, true);
                    }
                    break;
                case R.id.recordBtn:
                    if (status == 2){
                        break;
                    }else{
                        status = 2;
                        clickRecord();
                        mPager.setCurrentItem(2, true);
                    }
                    break;
            }
        }
    };

    private void clickStore(){
        storeBtn.setBackgroundResource(R.drawable.store_btn_1);
        uploadBtn.setBackgroundResource(R.drawable.upload_btn_2);
        recordBtn.setBackgroundResource(R.drawable.record_btn_2);
        mStoreFragment.setResumePlayBarUI();
    }
    private void clickUpload(){
        storeBtn.setBackgroundResource(R.drawable.store_btn_2);
        uploadBtn.setBackgroundResource(R.drawable.upload_btn_1);
        recordBtn.setBackgroundResource(R.drawable.record_btn_2);
        mUploadFragment.setResumePlayBarUI();
    }
    private void clickRecord(){
        storeBtn.setBackgroundResource(R.drawable.store_btn_2);
        uploadBtn.setBackgroundResource(R.drawable.upload_btn_2);
        recordBtn.setBackgroundResource(R.drawable.record_btn_1);
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        if (mRingbackTone.isPlayingNow())
            mRingbackTone.stopRingbackTone();
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case 0:
                    clickStore();
                    break;
                case 1:
                    clickUpload();
                    break;
                case 2:
                    clickRecord();
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public class SlidePagerAdapter extends FragmentPagerAdapter {
        public SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                mStoreFragment.mPager = mPager;
                return mStoreFragment;
            }else if (position == 1){
                mUploadFragment.mPager = mPager;
                return mUploadFragment;
            }else{
                mFragmentRecord.mPager = mPager;
                return mFragmentRecord;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

    public void backBtn(View v){
        finish();
    }
}
