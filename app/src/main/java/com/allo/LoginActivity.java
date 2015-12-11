package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class LoginActivity extends Activity {

    EditText et_id;
    EditText et_pw;


    Button btn_login;
    Button btn_register;

    String st_reg_id = "";
    String st_phone_number = "";

    ProgressDialog pd = null;

    Context context;
    Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("LoginActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout();
        setListener();

    }


    private void setLayout() {
        et_id = (EditText) findViewById(R.id.et_id);
        et_pw = (EditText) findViewById(R.id.et_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);

        LoginUtils loginUtils = new LoginUtils(LoginActivity.this);

        et_id.setText(loginUtils.getId());
        et_pw.setText(loginUtils.getPw());
    }

    private void setListener() {

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("login").setAction("btn_login click").build());
                login();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("register").setAction("btn_register click").build());
                register();
            }
        });
    }


    private void login() {
        String st_id = et_id.getText().toString();
        String st_pw = et_pw.getText().toString();

        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        st_phone_number = telManager
                .getLine1Number();
        if (st_phone_number.startsWith("+82")) {
            st_phone_number = st_phone_number.replace("+82", "0");
        }

        st_reg_id = getRegistrationId();

        if (st_id.equals("") || st_pw.equals("")) {
            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        } else {
            final ProgressDialog pd = ProgressDialog.show(LoginActivity.this, "", "로그인 중입니다.", true);
            LoginUtils loginUtils = new LoginUtils(LoginActivity.this) {
                @Override
                public void onLoginSuccess() {
                    pd.dismiss();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                    LoginActivity.this.finish();
                }

                @Override
                public void onLoginFailure() {
                    pd.dismiss();
                }

                @Override
                public void onNetworkFail(){
                    pd.dismiss();
                }
            };

            loginUtils.login(st_id, st_pw, st_phone_number, st_reg_id);
        }
    }

    private void register() {
        SingleToneData.getInstance().setLoginActivity(this);
        Intent intent = new Intent(this, AgreeActivity.class);
        startActivity(intent);

    }


//    GCM


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

    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
