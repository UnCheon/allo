package com.allo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class IntroActivity extends Activity {

    String st_id;
    String st_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        syncContacts();
        checkRegister();
        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkRegister();
            }

        }, 1000);
        */
    }

    private void syncContacts(){
        ContactSync contactSync = new ContactSync(getApplicationContext());
        contactSync.syncLocalContacts();
    }

    private void checkRegister(){
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);

        st_id = pref.getString("id", "");
        st_pw = pref.getString("pw", "");

        if (st_id.equals("") || st_pw.equals("")) {
            goLoginActivity();

        } else {
            connect_http_login();
        }
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
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
//                System.out.println(new String(responseBody));
                Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요. ", Toast.LENGTH_SHORT).show();
                goLoginActivity();
                finish();
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
                String st_phone_number = jo_my_info.getString("phone_number");

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
                SingleToneData singleToneData = SingleToneData.getInstance();
                singleToneData.setToken(st_token);
                singleToneData.setMainResponseData(st_response);

                startActivity(intent);

                finish();

            }else{
                Toast.makeText(getApplicationContext(), "intro login fail", Toast.LENGTH_SHORT).show();
                goLoginActivity();
                finish();
            }
        }catch(JSONException e){

        }
    }



}

