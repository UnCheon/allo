package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baek_uncheon on 2015. 3. 25..
 */
public class RegisterActivity extends Activity {

    Context context;

    EditText et_id;
    EditText et_pw;
    EditText et_pw_check;

    EditText et_name;
    EditText et_phone_number;

    Button btn_register;

    String st_id;
    String st_pw;

    String st_phone_number = "";
    String st_nickname;

    String st_reg_id = "";

    String st_phone_number_list;

    ProgressDialog pd = null;


    Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("RegisterActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        st_phone_number = telManager
                .getLine1Number();
        if (st_phone_number.startsWith("+82")) {
            st_phone_number = st_phone_number.replace("+82", "0");
        }

        setLayout();

    }


    private void setLayout() {


        et_id = (EditText) findViewById(R.id.et_id);
        et_pw = (EditText) findViewById(R.id.et_password);
        et_pw_check = (EditText) findViewById(R.id.et_password_check);

        et_name = (EditText) findViewById(R.id.et_name);
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        et_phone_number.setText(st_phone_number);

        btn_register = (Button) findViewById(R.id.btn_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("register").setAction("btn_register click").build());
                btnRegister();
            }
        });
    }


    private void setPhoneNumberList() {
        st_phone_number_list = "[";

        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(getApplicationContext());
        mContactDBOpenHelper.open_writableDatabase();

        Cursor mCursor;
        mCursor = mContactDBOpenHelper.getNewContacts();


        while (mCursor.moveToNext()) {
            if (st_phone_number_list.equals("["))
                st_phone_number_list = st_phone_number_list + "\"" + mCursor.getString(1) + "\"";
            else
                st_phone_number_list = st_phone_number_list + ",\"" + mCursor.getString(1) + "\"";
        }

        st_phone_number_list = st_phone_number_list + "]";


        mContactDBOpenHelper.close();
    }


    public void btnRegister() {

        st_id = et_id.getText().toString();
        st_pw = et_pw.getText().toString();
        st_nickname = et_name.getText().toString();

        if (st_id.equals("") || st_pw.equals("")) {
            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(st_pw.equals(et_pw_check.getText().toString()))) {
            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (st_nickname.equals("")) {
            Toast.makeText(this, "사용하실 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        st_reg_id = getRegistrationId();




        // register user info

        String url = getApplicationContext().getString(R.string.url_register);

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());

        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        pd = ProgressDialog.show(RegisterActivity.this, "", RegisterActivity.this.getString(R.string.wait_register), true);

        setPhoneNumberList();

        RequestParams params = new RequestParams();
        params.put("id", st_id);
        params.put("pw", st_pw);
        params.put("phone_number", st_phone_number);
        params.put("nickname", st_nickname);
        params.put("platform", "android");
        params.put("device_type", getDeviceName());
        params.put("endpoint", st_reg_id);
        params.put("phone_number_list", st_phone_number_list);

//        params.put("phone_number_list", st_phone_number_list);
//        Log.i("pnl in main", st_phone_number_list);



        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject result_object = new JSONObject(new String(responseBody));
                    String status = result_object.getString("status");

                    if (status.equals("success")) {
                        registerSuccess();
                    } else if (status.equals("fail")) {
                        ErrorHandler errorHandler = new ErrorHandler(getApplicationContext());
                        errorHandler.handleErrorCode(result_object);

                    }

                } catch (JSONException e) {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "Json Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void registerSuccess() {

        // save ID PASSWORD
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();

        editor.putString("id", st_id);
        editor.putString("nickname", st_nickname);
        editor.putString("pw", st_pw);
        editor.putString("phone_number", st_phone_number);

        editor.commit();

        final ProgressDialog pd_sync = ProgressDialog.show(RegisterActivity.this, "", "동기화 중입니다.", true);
        LoginUtils loginUtils = new LoginUtils(RegisterActivity.this){
            @Override
            public void onLoginSuccess(){
                pd_sync.dismiss();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                RegisterActivity.this.finish();
                Activity loginActivity = SingleToneData.getInstance().getLoginActivity();
                loginActivity.finish();
            }
            @Override
            public void onLoginFailure(){
                pd_sync.dismiss();
            }

            @Override
            public void onNetworkFail(){
                pd_sync.dismiss();
            }
        };

        loginUtils.login(st_id, st_pw, "", "");

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

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

