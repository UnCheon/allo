package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by baek_uncheon on 2015. 6. 5..
 */
public class LoginUtils {

    Context context;
    public LoginUtils(Context context){
        this.context = context;
    }

    public void onLoginRequired(){
        Intent intent = new Intent(context, IntroActivity.class);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    public String getToken(){
        SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        String st_token = pref.getString("token", "");
        return st_token;
    }

    public String getNickname(){
        SharedPreferences pref = context.getSharedPreferences("userInfo", context.MODE_PRIVATE);
        String st_nickname = pref.getString("nickname", "");
        return st_nickname;
    }
}
