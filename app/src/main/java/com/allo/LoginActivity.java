package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends Activity {

    EditText et_id;
    EditText et_pw;


    Button btn_login;
    TextView tv_register;

    String st_reg_id;
    String st_phone_number;

    private GoogleCloudMessaging _gcm;
    private String _regId;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "793263929001";

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkGCMPossible();
        st_reg_id = getRegistrationId();

        setLayout();
        setListener();

    }


    private void setLayout(){
        et_id = (EditText) findViewById(R.id.et_id);
        et_pw = (EditText) findViewById(R.id.et_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_register = (TextView) findViewById(R.id.tv_register);
    }

    private void setListener() {

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void setPhoneNumber(){
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        st_phone_number = telManager
                .getLine1Number();
        if (st_phone_number.startsWith("+82")) {
            st_phone_number = st_phone_number.replace("+82", "0");
        }
    }

    private void login(){
        String st_id = et_id.getText().toString();
        String st_pw= et_pw.getText().toString();

        if (st_id.equals("") || st_pw.equals("")) {
            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }else{
            connect_http_login(st_id, st_pw);
        }
    }

    private void register(){
        Intent intent = new Intent(this, AgreeActivity.class);
        startActivity(intent);
    }


    private void connect_http_login(String st_id, String st_pw){
        setPhoneNumber();

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        params.put("id", st_id);
        params.put("pw", st_pw);
        params.put("endpoint", st_reg_id);
        params.put("phone_number", st_phone_number);

        String url = getApplication().getString(R.string.url_login);

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("Login HTTP RESPONSE......", new String(responseBody));
                onLoginRequestSuccess(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요. ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLoginRequestSuccess(String st_response_body){
        try{
            JSONObject jo_response_body = new JSONObject(st_response_body);
            String st_status = jo_response_body.getString("status");
            if (st_status.equals("success")){
                JSONObject jo_response = jo_response_body.getJSONObject("response");
                String st_token = jo_response.getString("token");

                JSONObject jo_my_info = jo_response.getJSONObject("my_info");
                String st_nickname = jo_my_info.getString("nickname");


                SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();

                editor.putString("id", et_id.getText().toString());
                editor.putString("pw", et_pw.getText().toString());
                editor.putString("nickname", st_nickname);
                editor.putString("phone_number", st_phone_number);
                editor.putString("token", st_token);
                editor.putString("endpoint", st_reg_id);
                editor.commit();

                SingleToneData singleToneData = SingleToneData.getInstance();
                singleToneData.setToken(st_token);

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                finish();

            }else{
                Toast.makeText(getApplicationContext(), "등록되지 않은 아이디 이거나 비밀번호가 잘못되었습니다. ", Toast.LENGTH_SHORT).show();
            }
        }catch(JSONException e){

        }
    }


//    GCM

    private void checkGCMPossible(){

        // google play service가 사용가능한가

        if (checkPlayServices())
        {
            _gcm = GoogleCloudMessaging.getInstance(this);
            _regId = getRegistrationId();

            if (TextUtils.isEmpty(_regId))
                registerInBackground();
        }
        else
        {
            Log.i("MainActivity.java | onCreate", "|No valid Google Play Services APK found.|");

        }


        Log.i("reg_id", getRegistrationId());

        // display received msg
        String msg = getIntent().getStringExtra("msg");
        if (!TextUtils.isEmpty(msg)){

        }

        context = this;

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        // display received msg
        String msg = intent.getStringExtra("msg");
        Log.i("MainActivity.java | onNewIntent", "|" + msg + "|");
        if (!TextUtils.isEmpty(msg))
            Log.i("MainActivity.java | onNewIntent", "|" + msg + "|");


    }

    // google play service가 사용가능한가
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i("MainActivity.java | checkPlayService", "|This device is not supported.|");

                finish();
            }
            return false;
        }
        return true;
    }

    // registration  id를 가져온다.
    private String getRegistrationId()
    {
        String registrationId = PreferenceUtil.instance(getApplicationContext()).regId();
        if (TextUtils.isEmpty(registrationId))
        {
            Log.i("MainActivity.java | getRegistrationId", "|Registration not found.|");

            return "";
        }
        int registeredVersion = PreferenceUtil.instance(getApplicationContext()).appVersion();
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion)
        {
            Log.i("MainActivity.java | getRegistrationId", "|App version changed.|");


            return "";
        }
        return registrationId;
    }

    // app version을 가져온다. 뭐에 쓰는건지는 모르겠다.
    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    // gcm 서버에 접속해서 registration id를 발급받는다.
    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (_gcm == null)
                    {
                        _gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    _regId = _gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + _regId;

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(_regId);
                }
                catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                Log.i("MainActivity.java | onPostExecute", "|" + msg + "|");

            }
        }.execute(null, null, null);
    }

    // registraion id를 preference에 저장한다.
    private void storeRegistrationId(String regId)
    {
        int appVersion = getAppVersion();
        Log.i("MainActivity.java | storeRegistrationId", "|" + "Saving regId on app version " + appVersion + "|");
        PreferenceUtil.instance(getApplicationContext()).putRedId(regId);
        PreferenceUtil.instance(getApplicationContext()).putAppVersion(appVersion);
    }
}
