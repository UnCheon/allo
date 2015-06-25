package com.allo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.urqa.clientinterface.URQAController;

import java.io.File;

import skd.androidrecording.audio.AudioPlaybackManager;
import skd.androidrecording.audio.AudioRecordingHandler;
import skd.androidrecording.audio.AudioRecordingThread;
import skd.androidrecording.video.PlaybackHandler;
import skd.androidrecording.visualizer.VisualizerView;
import skd.androidrecording.visualizer.renderer.BarGraphRenderer;


public class RecordFragment extends Fragment {
    public ViewPager mPager;

    private Context mContext;

    private boolean isPlaying = false, isRecoding = false, isSave = false;
    private AudioPlaybackManager playbackManager = null;
    private VisualizerView visualizerView;
    private AudioRecordingThread recordingThread;
    private ImageButton recordingBtn, playBtn;
    private static String FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo";
    private static String TEMPFILENAME = "temp.3gp";

    public RecordFragment(){

    }
    public void setContext(Context context){this.mContext = context;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        URQAController.InitializeAndStartSession(mContext, "BEAC46A7");

        visualizerView = (VisualizerView) view.findViewById(R.id.visualizerView);
        setupVisualizer();
        playbackManager = new AudioPlaybackManager(mContext, visualizerView, playbackHandler);

        playbackManager.getPlayerManager().getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer tmp) {
                visualizerView.flash();
                tmp.seekTo(0);
                playBtn.setBackgroundResource(R.drawable.play_btn);
                isPlaying = false;
            }
        });

        playBtn = (ImageButton) view.findViewById(R.id.playBtn);
        LinearLayout playBtnLayout = (LinearLayout) view.findViewById(R.id.playBtnLayout);
        playBtn.setOnClickListener(onPlayClickListener);
        playBtnLayout.setOnClickListener(onPlayClickListener);

        recordingBtn = (ImageButton) view.findViewById(R.id.recordingBtn);
        LinearLayout recordingBtnLayout = (LinearLayout) view.findViewById(R.id.recordingBtnLayout);
        recordingBtn.setOnClickListener(onRecordingClickListener);
        recordingBtnLayout.setOnClickListener(onRecordingClickListener);

        ImageButton saveBtn = (ImageButton) view.findViewById(R.id.saveBtn);
        LinearLayout saveBtnLayout = (LinearLayout) view.findViewById(R.id.saveBtnLayout);
        saveBtn.setOnClickListener(onSaveClickListener);
        saveBtnLayout.setOnClickListener(onSaveClickListener);

        return view;
    }

    private View.OnClickListener onPlayClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(!isSave) return;

            if (isPlaying) {
                visualizerView.flash();
                playbackManager.pause();
                playbackManager.seekTo(0);

                playBtn.setBackgroundResource(R.drawable.play_btn);
                isPlaying = false;
            } else {
                visualizerView.flash();
                playbackManager.start();
                playBtn.setBackgroundResource(R.drawable.stop_btn_1);
                isPlaying = true;
            }
        }
    };

    private View.OnClickListener onRecordingClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(isRecoding) {
                stopRecording();
            }
            else {
                startRecording();
            }

        }
    };

    private View.OnClickListener onSaveClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(!isSave) return;
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle("allo");
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.alertdialog_edit, null, false);
            alertDialog.setView(layout);
            alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    EditText input = (EditText)layout.findViewById(R.id.et);

                    if(input.getText().length() < 1) {
                        Toast.makeText(getActivity(), "한글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String title = input.getText().toString();

                    File filePre = new File(FILEPATH + "/" + TEMPFILENAME);

                    SharedPreferences pref = mContext.getSharedPreferences("userInfo", mContext.MODE_PRIVATE);
                    String nickname = pref.getString("nickname", "");

                    Allo allo = new Allo();
                    allo.setRingTitle(title);
                    allo.setRingURL(FILEPATH + "/" + TEMPFILENAME);
                    allo.setRingSinger(nickname);

                    AlloHttpUtils alloHttpUtils = new AlloHttpUtils(mContext);
//                    alloHttpUtils.setUploadFile(allo);


                    File fileTo = new File(FILEPATH + "/" + input.getText().toString() + ".3gp");
//
//                    if(fileTo.exists()) {
//                        Toast.makeText(getActivity(), "이미 같은 이름의 파일이 존재합니다.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if(filePre.renameTo(fileTo)) {
//                        Toast.makeText(getActivity(), "저장 성공", Toast.LENGTH_SHORT).show();
//                        playbackManager.setupPlayback(fileTo.getAbsolutePath());
//                    }
//                    else {
//                        Toast.makeText(getActivity(), "저장 실패", Toast.LENGTH_SHORT).show();
//                    }
                }
            });
            alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            Dialog d = alertDialog.show();
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = d.findViewById(dividerId);
            divider.setBackgroundColor(Color.rgb(249, 30, 47));

            int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            TextView tv = (TextView) d.findViewById(textViewId);
            tv.setTextColor(Color.rgb(249, 30, 47));
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        stopRecording();
        playStop();
    }


    private PlaybackHandler playbackHandler = new PlaybackHandler() {
        @Override
        public void onPreparePlayback() {
        }
    };

    private void releaseVisualizer() {
        visualizerView.release();
        visualizerView = null;
    }

    private void playStop() {
        visualizerView.flash();
        playbackManager.pause();
        playBtn.setBackgroundResource(R.drawable.play_btn);
        isPlaying = false;
    }

    private void startRecording() {
        visualizerView.flash();

        recordingThread = new AudioRecordingThread(getFileName(), new AudioRecordingHandler() {
            @Override
            public void onFftDataCapture(final byte[] bytes) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (visualizerView != null) {
                            visualizerView.updateVisualizerFFT(bytes);
                        }
                    }
                });
            }

            @Override
            public void onRecordSuccess() {
                isSave = true;
                playbackManager.setupPlayback(FILEPATH + "/" + TEMPFILENAME);
            }

            @Override
            public void onRecordingError() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        stopRecording();
                    }
                });
            }

            @Override
            public void onRecordSaveError() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        stopRecording();
                    }
                });
            }
        });

        recordingThread.start();

        recordingBtn.setBackgroundResource(R.drawable.stop_btn_1);
        isRecoding = true;
    }

    private void stopRecording() {
        if (recordingThread != null) {
            recordingThread.stopRecording();
            recordingThread = null;
        }

        visualizerView.flash();

        recordingBtn.setBackgroundResource(R.drawable.record);
        isRecoding = false;
    }

    private String getFileName() {
        File file = new File(FILEPATH);
        if( !file.exists() ) file.mkdirs();
        return FILEPATH + "/" + TEMPFILENAME;
    }

    private void setupVisualizer() {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(255, 249, 31, 48));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(5, linePaint, false);
        visualizerView.addRenderer(barGraphRendererBottom);
    }
}
