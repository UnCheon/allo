package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;



public class IntroActivity extends Activity {
    Context context;

    String st_id;
    String st_pw;

    private GoogleCloudMessaging _gcm;
    private String _regId;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "793263929001";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("introActivity", "intro activity on create");
        setContentView(R.layout.activity_intro);





        startAllo();


    }

    private void startAllo(){
        if (checkNetwork()){
            if (getRegistrationId().equals(""))
                checkGCMPossible();
            checkRegister();
        }else{
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setTitle("알림").setMessage("네트워크에 연결되어있지 않습니다.\n다시 시도하시겠습니까?").setCancelable(false).
                    setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }).setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startAllo();
                        }
                    });
            AlertDialog alert = alert_confirm.create();
            alert.show();

        }
    }

    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) IntroActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mobile.isConnected() || wifi.isConnected()) {
            return true;
        }else{
            return false;
        }
    }





    private void checkRegister() {
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);

        st_id = pref.getString("id", "");
        st_pw = pref.getString("pw", "");

        Log.i("IntroActivity", "id : "+st_id +", pw : "+st_pw);

        if (st_id.equals("") || st_pw.equals("")) {
            goLoginActivity();

        } else {

            LoginUtils loginUtils = new LoginUtils(IntroActivity.this){
                @Override
                public void onLoginSuccess(){
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    startActivity(intent);
                    IntroActivity.this.finish();
                }

                @Override
                public void onLoginFailure(){
                    goLoginActivity();
                }

                @Override
                public void onNetworkFail(){
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(IntroActivity.this);
                    alert_confirm.setTitle("알림").setMessage("네트워크에 연결되어있지 않습니다.\n다시 시도하시겠습니까?").setCancelable(false).
                            setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startAllo();
                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            };
            loginUtils.login(st_id, st_pw, "", "");
        }
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        IntroActivity.this.finish();
    }



    private void checkGCMPossible() {

        // google play service가 사용가능한가

        if (checkPlayServices()) {
            _gcm = GoogleCloudMessaging.getInstance(this);
            _regId = getRegistrationId();

            if (TextUtils.isEmpty(_regId))
                registerInBackground();
        } else {
            Log.i("MainActivity.java | onCreate", "|No valid Google Play Services APK found.|");

        }


        Log.i("reg_id", getRegistrationId());

        // display received msg
        String msg = getIntent().getStringExtra("msg");
        if (!TextUtils.isEmpty(msg)) {

        }

        context = this;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // display received msg
        String msg = intent.getStringExtra("msg");
        Log.i("MainActivity.java | onNewIntent", "|" + msg + "|");
        if (!TextUtils.isEmpty(msg))
            Log.i("MainActivity.java | onNewIntent", "|" + msg + "|");


    }

    // google play service가 사용가능한가
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MainActivity.java | checkPlayService", "|This device is not supported.|");

                finish();
            }
            return false;
        }
        return true;
    }

    // registration  id를 가져온다.
    private String getRegistrationId() {
        String registrationId = PreferenceUtil.instance(getApplicationContext()).regId();
        if (TextUtils.isEmpty(registrationId)) {
            Log.i("MainActivity.java | getRegistrationId", "|Registration not found.|");

            return "";
        }
        int registeredVersion = PreferenceUtil.instance(getApplicationContext()).appVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i("MainActivity.java | getRegistrationId", "|App version changed.|");


            return "";
        }
        return registrationId;
    }

    // app version을 가져온다. 뭐에 쓰는건지는 모르겠다.
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // gcm 서버에 접속해서 registration id를 발급받는다.
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (_gcm == null) {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + _regId;

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(_regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("MainActivity.java | onPostExecute", "|" + msg + "|");

            }
        }.execute(null, null, null);
    }

    // registraion id를 preference에 저장한다.
    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion();
        Log.i("MainActivity.java | storeRegistrationId", "|" + "Saving regId on app version " + appVersion + "|");
        PreferenceUtil.instance(getApplicationContext()).putRedId(regId);
        PreferenceUtil.instance(getApplicationContext()).putAppVersion(appVersion);
    }
}

