package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import ringdroid.soundfile.SoundFile;


public class SelectMyAlloActivity extends Activity {

    ImageView ib_back;
    ListView lv_allo;

    ArrayList<Allo> al_allo;

    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private boolean mFinishActivity;

    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private SoundFile mSoundFile;
    private File mFile;
    private String mFilename;
    private Handler mHandler;

    private Thread mLoadSoundFileThread;

    ProgressDialog pd_down;

    AlloCacheAsyncTask alloCacheThread;

    String st_type;
    Friend friend;

    String Save_Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo/cache";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_my_allo);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("SelectMyAlloActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        setLayout();
        setListener();

        st_type = getIntent().getStringExtra("type");

        if (st_type.equals("friend")) {
            friend = (Friend) getIntent().getSerializableExtra("friend");
        }


        al_allo = SingleToneData.getInstance().getMyAlloList();
        Log.i("size my allo list", String.valueOf(al_allo.size()));

        SelectMyAlloAdapter adapter = new SelectMyAlloAdapter(this, R.layout.my_allo_select_list_item, al_allo);
        lv_allo.setAdapter(adapter);

    }


    private void setLayout() {

        ib_back = (ImageView) findViewById(R.id.iv_back);
        lv_allo = (ListView) findViewById(R.id.lv_allo);
    }

    private void setListener() {

        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lv_allo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lv_allo.setEnabled(false);
                final Allo allo = al_allo.get(position);
                final ProgressDialog dialog = ProgressDialog.show(SelectMyAlloActivity.this, "", SelectMyAlloActivity.this.getString(R.string.wait_cache_file), true);


                alloCacheThread = new AlloCacheAsyncTask(SelectMyAlloActivity.this, allo.getURL()) {
                    @Override
                    public void onFinish(String st_cache_path, long l_time) {
                        dialog.dismiss();
                        lv_allo.setEnabled(true);
                        loadFromFile(allo, st_cache_path);

                    }

                    @Override
                    public void onFailed() {
                        dialog.dismiss();

                    }
                };

                alloCacheThread.execute("a","a","a");


            }
        });
    }

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private void loadFromFile(final Allo allo, String st_cache_file_path) {

        mHandler = new Handler();


        mLoadSoundFileThread = null;

        mSoundFile = null;
        mAlertDialog = null;
        mProgressDialog = null;


        mFile = new File(st_cache_file_path);


        mLoadingLastUpdateTime = getCurrentTime();
        mLoadingKeepGoing = true;
        mFinishActivity = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mLoadingKeepGoing = false;
                        mFinishActivity = true;
                    }
                });
        mProgressDialog.show();

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double fractionComplete) {
                        long now = getCurrentTime();
                        if (now - mLoadingLastUpdateTime > 100) {
                            mProgressDialog.setProgress(
                                    (int) (mProgressDialog.getMax() * fractionComplete));
                            mLoadingLastUpdateTime = now;
                        }
                        return mLoadingKeepGoing;
                    }
                };

        // Load the sound file in a background thread
        mLoadSoundFileThread = new Thread() {
            public void run() {
                try {

                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                    if (mSoundFile == null) {
                        mProgressDialog.dismiss();
                        String name = mFile.getName().toLowerCase();
                        String[] components = name.split("\\.");
                        String err;
                        if (components.length < 2) {
                            err = getResources().getString(
                                    R.string.no_extension_error);
                        } else {
                            err = getResources().getString(
                                    R.string.bad_extension_error) + " " +
                                    components[components.length - 1];
                        }
                        final String finalErr = err;
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(new Exception(), finalErr);
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
//                    mPlayer = new SamplePlayer(mSoundFile);

                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();

//                    runOnUiThread(new Runnable() {
//                        public void run() {
//
//                        }
//                    });

                    Runnable runnable = new Runnable() {
                        public void run() {
                            showFinalAlert(e, getResources().getText(R.string.read_error));
                        }
                    };
                    mHandler.post(runnable);
                    return;
                }
                mProgressDialog.dismiss();
                if (mLoadingKeepGoing) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile(allo);

                        }
                    };
                    mHandler.post(runnable);
                } else if (mFinishActivity) {
//                    dismiss();
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    private void finishOpeningSoundFile(Allo allo) {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float mDensity = metrics.density;
        AlloCropDialog alloCropDialog = new AlloCropDialog(SelectMyAlloActivity.this);
        if (st_type.equals("friend")) {
            alloCropDialog.setFriend(friend);
        }


        alloCropDialog.setAllo(allo);
        alloCropDialog.setDensity(mDensity);
        alloCropDialog.setSoundFile(mSoundFile);
        alloCropDialog.show();


    }


    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
        if (alloCacheThread != null) {
            if (!alloCacheThread.isCancelled())
                alloCacheThread.cancel(true);
        }


    }

    private void showFinalAlert(Exception e, CharSequence message) {
        CharSequence title;
        if (e != null) {
            Log.e("Ringdroid", "Error: " + message);
            Log.e("Ringdroid", getStackTrace(e));
            title = getResources().getText(R.string.alert_title_failure);
//            setResult(RESULT_CANCELED, new Intent());
        } else {
            Log.v("Ringdroid", "Success: " + message);
            title = getResources().getText(R.string.alert_title_success);
        }

//        new AlertDialog.Builder(RingdroidEditActivity.this)
//                .setTitle(title)
//                .setMessage(message)
//                .setPositiveButton(
//                        R.string.alert_ok_button,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,
//                                                int whichButton) {
//                                finish();
//                            }
//                        })
//                .setCancelable(false)
//                .show();
    }

    private String getStackTrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private void showFinalAlert(Exception e, int messageResourceId) {
        showFinalAlert(e, getResources().getText(messageResourceId));
    }

    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        Log.v("Ringdroid", "EditActivity OnDestroy");

        mLoadingKeepGoing = false;

        closeThread(mLoadSoundFileThread);


        mLoadSoundFileThread = null;


        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        super.onStop();

        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

}
