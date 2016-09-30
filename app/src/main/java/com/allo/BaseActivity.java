package com.allo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    boolean isHome = false;


    private ProgressDialog pd = null;


    private ProgressDialog waitDialog;
    private AlertDialog userDialog;



    public void showUpdateDialog() {
        pd = ProgressDialog.show(this, "", "동기화 중입니다.", true);

    }

    public void dismissUpdateDialog() {
        if (pd.isShowing()){
            pd.dismiss();
            pd = null;
        }

    }

    @Override
    protected void onUserLeaveHint() {
        Log.i(TAG, "onUserLeaveHint: ");
        isHome = true;
        super.onUserLeaveHint();
    }



    public void showReviewDialog() {
        pd = ProgressDialog.show(this, "", "잠시만 기다려주세요.", true);
    }

    public void dismissReviewDialog() {
        if (pd != null){
            if (pd.isShowing()){
                pd.dismiss();
                pd = null;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void sendEventToFirebase(String category ,String event, String value){
        Bundle params = new Bundle();
        params.putString(event, value);
    }

    public void sendMarkerToFirebase(String category ,String event, String value, int id, int alliance, int price){

    }


    public void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    //
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    public void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.setCancelable(false);
        waitDialog.show();
    }

    public void closeWaitDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    waitDialog.dismiss();
                    Toast.makeText(BaseActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
