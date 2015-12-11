package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 5..
 */
public class LoginUtils {

    Context context;

    public LoginUtils(Context context) {
        this.context = context;
    }

    public void onLoginRequired() {
        Intent intent = new Intent(context, IntroActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public String getNickname() {
        SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        String st_nickname = pref.getString("nickname", "");
        return st_nickname;
    }

    public String getMyAlloTitle() {
        SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
        String st_title = pref.getString("0title", "");
        return st_title;
    }

    public String getMyAlloArtist() {
        SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
        String st_artist = pref.getString("0artist", "");
        return st_artist;
    }

    public String getMyAlloUrl() {
        SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
        String st_title = pref.getString("0title", "");
        return st_title;
    }

    public String getMyAlloImage() {
        SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
        String st_image = pref.getString("0image", "");
        return st_image;
    }

    public int getMyAlloStartPoint() {
        SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
        int i_start_point = pref.getInt("0key", 0);
        return i_start_point;
    }

    public String getId() {
        SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        String st_id = pref.getString("id", "");
        return st_id;
    }

    public String getPw() {
        SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        String st_pw = pref.getString("pw", "");
        return st_pw;
    }



    public void login(String st_id, String st_pw, String st_phone_number, String st_reg_id){

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();


        params.put("id", st_id);
        params.put("pw", st_pw);
        if (!st_phone_number.equals(""))
            params.put("phone_number", st_phone_number);
        if (!st_reg_id.equals(""))
            params.put("endpoint", st_reg_id);

        Log.i("LoginUtils login try", "id : "+st_id+", pw : " + st_pw + ", phone_number : " + st_phone_number+", endPoint : "+st_reg_id);

        String url = context.getString(R.string.url_login);


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                onLoginRequestSuccess(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, context.getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
                onNetworkFail();
            }
        });
    }


    private void onLoginRequestSuccess(String st_response_body) {
        try {
            JSONObject jo_response_body = new JSONObject(st_response_body);
            String st_status = jo_response_body.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_response_body.getJSONObject("response");
                JSONObject jo_my_info = jo_response.getJSONObject("my_info");

                if (jo_response.has("remain_charge_time")) {
                    SingleToneData.getInstance().setLeftTime(jo_response.getInt("remain_charge_time"));
                }

                ArrayList<Notice> ar_notice_list = new ArrayList<>();
                if (jo_response.has("notice")) {
                    JSONArray ja_notice = jo_response.getJSONArray("notice");

                    for (int i = 0; i < ja_notice.length(); i++) {
                        JSONObject jo_notice = ja_notice.getJSONObject(i);
                        String st_uid = jo_notice.getString("uid");
                        String st_value = jo_notice.getString("value");
                        String st_image_url = jo_notice.getString("img");

                        Notice notice = new Notice();
                        notice.setImageUrl(st_image_url);
                        notice.setUid(st_uid);
                        notice.setValue(st_value);

                        ar_notice_list.add(notice);

                    }


                    SingleToneData.getInstance().setNoticeList(ar_notice_list);
                }

                if (jo_my_info.has("cash")) {
                    SingleToneData.getInstance().setCash(jo_my_info.getString("cash"));
                }


                SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();

                String st_nickname = "";
                String st_id = "";
                String st_pw = "";
                String st_phone_number = "";

                if (jo_my_info.has("nickname"))
                    st_nickname = jo_my_info.getString("nickname");
                if (jo_my_info.has("id"))
                    st_id = jo_my_info.getString("id");
                if (jo_my_info.has("pw"))
                    st_pw = jo_my_info.getString("pw");
                if (jo_my_info.has("phone_number"))
                    st_phone_number = jo_my_info.getString("phone_number");

                editor.putString("id", st_id);
                editor.putString("pw", st_pw);
                editor.putString("nickname", st_nickname);
                editor.putString("phone_number", st_phone_number);

                editor.commit();

                ContactSync contactSync = new ContactSync(context){
                    @Override
                    public void onFinishSyncContact(){
                        onLoginSuccess();
                    }
                };
                contactSync.execute("a", "a", "a");


                Log.i("Login success info", "id : "+st_id + ", pw : "+st_pw + ", phone_number : "+st_phone_number + ", nickname : " +st_nickname );


            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_response_body);
                onLoginFailure();
            }
        } catch (JSONException e) {
            Toast.makeText(context, "JSon Error", Toast.LENGTH_SHORT).show();
            onLoginFailure();

        }
    }

    public void onLoginSuccess(){

    }

    public void onNetworkFail(){

    }

    public void onLoginFailure(){

    }
}
