package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.allo.MainActivity;
import com.allo.PreferenceUtil;
import com.allo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    Button btn_next;
    Button btn_register;


    String st_id;
    String st_pw;

    String st_phone_number;
    String st_nickname;

    String st_reg_id;

    String st_phone_number_list;

    Context ctx_login;

    private GoogleCloudMessaging _gcm;
    private String _regId;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "793263929001";

    public void setCtxLogin(Context ctx_login){
        this.ctx_login = ctx_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkGCMPossible();
        setVariables();
        setRegisterLayout();
        setRegisterListener();

    }


    private void setRegisterLayout(){
        setContentView(R.layout.activity_register);

        et_id = (EditText)findViewById(R.id.et_id);
        et_pw = (EditText) findViewById(R.id.et_password);
        et_pw_check = (EditText) findViewById(R.id.et_password_check);

        btn_next = (Button) findViewById(R.id.btn_next);
    }

    private void setNameLayout(){
        setContentView(R.layout.activity_name);

        et_name = (EditText)findViewById(R.id.et_name);
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        et_phone_number.setText(st_phone_number);

        btn_register = (Button) findViewById(R.id.btn_register);

    }

    private void setVariables(){
        setPhoneNumber();
        setPhoneNumberList();
        setRegId();
    }

    private void setRegId(){
        st_reg_id = getRegistrationId();
    }

    private void setPhoneNumber(){
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        st_phone_number = telManager
                .getLine1Number();
        if (st_phone_number.startsWith("+82")) {
            st_phone_number = st_phone_number.replace("+82", "0");
        }
    }

    private void setPhoneNumberList(){
        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(context);
        mContactDBOpenHelper.open_writableDatabase();

        Cursor mCursor;
        mCursor = mContactDBOpenHelper.getNewContacts();


        st_phone_number_list = "[";

        while(mCursor.moveToNext()){
            if (st_phone_number_list.equals("["))
                st_phone_number_list = st_phone_number_list +"\""+mCursor.getString(1)+"\"";
            else
                st_phone_number_list = st_phone_number_list +",\""+mCursor.getString(1)+"\"";

        }
        mContactDBOpenHelper.close();
        st_phone_number_list = st_phone_number_list+"]";
    }


    private void setRegisterListener(){
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnNext();
            }
        });
    }

    private void setNameListener(){
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRegister();
            }
        });
    }

    private void btnNext(){
        st_id = et_id.getText().toString();
        st_pw = et_pw.getText().toString();

        if (st_id.equals("") || st_pw.equals("")){
            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(st_pw.equals(et_pw_check.getText().toString()))){
            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        setNameLayout();
        setNameListener();

    }


    public void btnRegister(){

        st_nickname = et_name.getText().toString();

        if (st_nickname.equals("")) {
            Toast.makeText(this, "사용하실 닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }


        // register user info

        String url = context.getString(R.string.url_register);

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);

        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        params.put("id", st_id);
        params.put("pw", st_pw);
        params.put("phone_number", st_phone_number);
        params.put("nickname", st_nickname);
        params.put("platform", "android");
        params.put("device_type", getDeviceName());
        params.put("endpoint", st_reg_id);

        params.put("phone_number_list", st_phone_number_list);


        Log.i("Devicename", getDeviceName());
        Log.i("phoneNumberList", st_phone_number_list);


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject result_object = new JSONObject(new String(responseBody));
                    String status = result_object.getString("status");

                    if (status.equals("success")) {
                        JSONObject jo_response = result_object.getJSONObject("response");
                        String st_token = jo_response.getString("token");
                        registerSuccess(st_token);
                    } else {
                        // id is duplicated.
                        Toast.makeText(getApplicationContext(), "회원가입실패", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(new String(responseBody));
            }
        });

    }

    private void registerSuccess(String st_token){
        // save ID PASSWORD
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();

        editor.putString("id", st_id);
        editor.putString("nickname", st_nickname);
        editor.putString("pw", st_pw);
        editor.putString("phone_number", st_phone_number);
        editor.putString("endpoint", st_reg_id);
        editor.putString("token", st_token);

        editor.commit();

        SingleToneData singleToneData = SingleToneData.getInstance();
        singleToneData.setToken(st_token);


//       update all contacts'is_new as false

        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(context);
        mContactDBOpenHelper.open_writableDatabase();
        mContactDBOpenHelper.updateContacts();
        mContactDBOpenHelper.close();

        connect_http_login();

    }

    private void connect_http_login(){
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        params.put("id", st_id);
        params.put("pw", st_pw);

        Log.i("id pw", st_id + st_pw);

        String url = getApplication().getString(R.string.url_login);

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
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
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")){

                JSONObject jo_response = jo_result.getJSONObject("response");
                String st_token = jo_response.getString("token");


                SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();

                editor.putString("id", st_id);
                editor.putString("pw", st_pw);
                editor.putString("nickname", st_nickname);
                editor.putString("phone_number", st_phone_number);
                editor.putString("token", st_token);
                editor.commit();

                String st_response = jo_response.toString();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("st_response", st_response);
                startActivity(intent);

                ((Activity)ctx_login).finish();
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
    // registraion id를 preference에 저장한다.
    private void storeRegistrationId(String regId)
    {
        int appVersion = getAppVersion();
        Log.i("MainActivity.java | storeRegistrationId", "|" + "Saving regId on app version " + appVersion + "|");
        PreferenceUtil.instance(getApplicationContext()).putRedId(regId);
        PreferenceUtil.instance(getApplicationContext()).putAppVersion(appVersion);
    }
}

