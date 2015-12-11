package com.allo;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by baek_uncheon on 2015. 7. 20..
 */
public class PlayAllo {
    final String TAG = getClass().getSimpleName();

    private static PlayAllo uniqueInstance;
    private MediaPlayer mediaPlayer;
    private Allo allo;
    boolean is_prepare = false;
    int i_prev_volume = 0;

    AlloDialog alloDialog;

    StoreRecyclerViewAdapter storeRecyclerViewAdapter;
    MyAlloFragment myAlloFragment;
    FriendFragment friendFragment;
    RecordActivity recordActivity;

    AlloStoreDialog alloStoreDialog;
    AlloMyDialog alloMyDialog;
    AlloFriendDialog alloFriendDialog;
    AlloRecordDialog alloRecordDialog;


    String st_type;

    private PlayAllo() {
    }

    public static PlayAllo getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new PlayAllo();

        return uniqueInstance;
    }


    public void setStoreRecyclerViewAdapter(StoreRecyclerViewAdapter storeRecyclerViewAdapter) {
        this.storeRecyclerViewAdapter = storeRecyclerViewAdapter;
    }

    public void setMyAlloFragment(MyAlloFragment myAlloFragment) {
        this.myAlloFragment = myAlloFragment;
    }

    public void setFriendFragment(FriendFragment friendFragment) {
        this.friendFragment = friendFragment;
    }

    public void setRecordActivity(RecordActivity recordActivity) {
        this.recordActivity = recordActivity;
    }

    public void setStoreDialog(AlloStoreDialog alloStoreDialog) {
        this.alloStoreDialog = alloStoreDialog;
        this.alloMyDialog = null;
        this.alloFriendDialog = null;
        this.alloRecordDialog = null;
    }

    public void setMyDialog(AlloMyDialog alloMyDialog) {
        this.alloMyDialog = alloMyDialog;
        this.alloStoreDialog = null;
        this.alloFriendDialog = null;
        this.alloRecordDialog = null;
    }

    public void setFriendDialog(AlloFriendDialog alloFriendDialog) {
        this.alloFriendDialog = alloFriendDialog;
        this.alloStoreDialog = null;
        this.alloMyDialog = null;
        this.alloRecordDialog = null;
    }

    public void setRecordDialog(AlloRecordDialog alloRecordDialog) {
        this.alloRecordDialog = alloRecordDialog;
        this.alloStoreDialog = null;
        this.alloMyDialog = null;
        this.alloFriendDialog = null;
    }


    public void setType(String st_type) {
        this.st_type = st_type;
    }


    public void setBackgroundPrepare(Allo allo) {
        this.allo = allo;
        is_prepare = false;

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        mediaPlayer.setLooping(true);


        try {
            mediaPlayer.setDataSource(allo.getURL());
            mediaPlayer.prepare();
            is_prepare = true;
            mediaPlayer.seekTo(allo.getStartPoint());
        } catch (IOException e) {
            SingleToneData.getInstance().setPhoneState("IDLE");
        }
    }

    public void backgroundPlayAllo() {
        if (mediaPlayer != null && is_prepare) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    public void backgroundStopAllo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public Allo getAllo(){
        return this.allo;
    }


    public void setAlloPrepare(final Allo allo) {
        this.allo = allo;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);


        try {
            mediaPlayer.setDataSource(allo.getURL());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    switch (st_type) {
                        case "STORE":
                            storeRecyclerViewAdapter.onPrepared(allo);
                            storeRecyclerViewAdapter = null;
                            break;
                        case "MY":
                            myAlloFragment.onPrepared(allo);
                            myAlloFragment = null;
                            break;

                        case "FRIEND":
                            friendFragment.onPrepared(allo);
                            friendFragment = null;
                            break;
                        case "RECORD":
                            recordActivity.onPrepared(allo);
                            recordActivity = null;
                            break;
                    }

                }
            });
        } catch (IOException e) {
            switch (st_type) {
                case "STORE":
                    storeRecyclerViewAdapter.onPrepareFailed();
                    storeRecyclerViewAdapter = null;
                    break;
                case "MY":
                    myAlloFragment.onPrepareFailed();
                    myAlloFragment = null;
                    break;

                case "FRIEND":
                    friendFragment.onPrepareFailed();
                    friendFragment = null;
                    break;
                case "RECORD":
                    recordActivity.onPrepareFailed();
                    recordActivity = null;
                    break;
            }
            e.printStackTrace();

        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (st_type) {
                    case "STORE":
                        alloStoreDialog.onCompleteAllo();
                        break;
                    case "MY":
                        alloMyDialog.onCompleteAllo();
                        break;
                    case "FRIEND":
                        alloFriendDialog.onCompleteAllo();
                        break;
                    case "RECORD":
                        alloRecordDialog.onCompleteAllo();
                        break;
                }
            }
        });
    }


    public void setDeleteAlloPrepare(String st_url) {

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(st_url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    alloDialog.onPreparedAllo();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                alloDialog.onCompleteAllo();
            }
        });
    }


    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }


    public boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        else
            return false;
    }

    public void playAllo() {
        mediaPlayer.start();
    }

    public void pauseAllo() {
        mediaPlayer.pause();
    }

    public void stopAllo() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    public void seekToAllo(int i_to) {
        mediaPlayer.seekTo(i_to);
    }

}
