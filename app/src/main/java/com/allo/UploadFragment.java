package com.allo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
public class UploadFragment extends Fragment {
    public ViewPager mPager;
    ListView uploadLv;
    ImageView imageView;
    TextView playSongTV;
    LinearLayout playSongLayout;
    ImageButton playSongPlayBtn;

    Allo currentAllo;
    Context mContext;

    ArrayList<Allo> allo_list_array;

    public UploadFragment(){

    }
    public void setContext(Context context){this.mContext = context;}

    public Context getContext(){
        return mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        URQAController.InitializeAndStartSession(mContext, "BEAC46A7");

        setLayout(view);
        setListener();

        ArrayList<Allo> allo_list = getAlloList();
        UploadAdapter_backup adapter = new UploadAdapter_backup(mContext, R.layout.layout_upload_item, allo_list, this);
        uploadLv.setAdapter(adapter);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver, new IntentFilter("allo-state"));

        return view;
    }

    private void setLayout(View view){
        playSongLayout = (LinearLayout)view.findViewById(R.id.playSongLayout);
        uploadLv = (ListView)view.findViewById(R.id.uploadLv);
        imageView = (ImageView)view.findViewById(R.id.imageView);
        playSongTV = (TextView)view.findViewById(R.id.playSongTV);
        playSongPlayBtn = (ImageButton)view.findViewById(R.id.playSongPlayBtn);

    }

    private void setListener(){
        playSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSongPlayBtn();
            }
        });
        playSongLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSongPlayBtn();
            }
        });
        uploadLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("click list view", "click click");
                currentAllo = allo_list_array.get(position);
                playRingbackTone();
            }
        });
    }


    private void playRingbackTone(){
        playSongLayout.setVisibility(View.VISIBLE);
        RingbackTone mRingbackTone = RingbackTone.getInstance();

        String storage_string = currentAllo.getRingURL().substring(1, 8);
        String http_string = currentAllo.getRingURL().substring(0,4);
        Log.i("http string", http_string);
        String url;
        if (storage_string.equals("storage"))
            url = currentAllo.getRingURL();
        else if (http_string.equals("http"))
            url = currentAllo.getRingURL();
        else
            url = mContext.getString(R.string.base_media_url)+currentAllo.getRingURL();

        mRingbackTone.playRingbackTone(url);
        mRingbackTone.setCurrentAllo(currentAllo);

        playSongPlayBtn.setBackgroundResource(R.drawable.pause_white_btn);
        String play_title = currentAllo.getRingTitle()+" - "+currentAllo.getRingSinger();
        playSongTV.setText(play_title);

        setPlayImage(currentAllo.getRingCount());

    }

    private void setPlayImage(String count){
        String path = count+".png";
        Log.i("path path", path);
        InputStream ims = null;
        try {
            ims = mContext.getApplicationContext().getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.allo);
        }
    }

    private void pauseRingbackTone(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        mRingbackTone.pauseRingBackTone();
        playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
    }


    private void playSongPlayBtn(){
        RingbackTone mRingbackTone = RingbackTone.getInstance();
        if (mRingbackTone.isPlayingNow()){
            pauseRingbackTone();
        }else{
            playRingbackTone();
        }
    }




    private void playBarUIInit(){
        playSongPlayBtn.setBackgroundResource(R.drawable.play_white_btn);
    }


    private ArrayList<Allo> getAlloList(){
        allo_list_array = new ArrayList<>();
        Cursor c = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME }, "1=1", null, null);

        while (c.moveToNext()){
            Allo allo= new Allo();
            allo.setRingTitle(c.getString(4));
            allo.setRingURL(c.getString(1));
            allo.setRingSinger(c.getString(2));
            allo.setRingAlbum(c.getString(3));
            allo.setIsPlaying(false);
            allo_list_array.add(allo);
        }

        return allo_list_array;
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
            String play_title = currentAllo.getRingTitle() + " - " + currentAllo.getRingSinger();
            playSongTV.setText(play_title);
            playSongLayout.setVisibility(View.VISIBLE);

            setPlayImage(currentAllo.getRingCount());

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
