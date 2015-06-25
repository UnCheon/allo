package com.allo;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 12..
 */
public class AlloJSONUtils {
    Context context;

    public AlloJSONUtils(Context context){
        this.context = context;
    }

    public void parseByFriendAlloList(String st_result){
        try {
            JSONObject jo_result = new JSONObject(st_result);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")){
                Log.i("asdf", "asdf");
                ArrayList<Friend> ar_by_friend_allo_list = new ArrayList<>();

                JSONObject jo_response = jo_result.getJSONObject("response");

                JSONArray ja_friend_list = jo_response.getJSONArray("friend_allo_list");

                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);

                    Friend mFriend = new Friend();
                    mFriend.setNickname(jo_friend.getString("nickname"));
                    mFriend.setPhoneNumber(jo_friend.getString("phone_number"));
                    mFriend.setId(jo_friend.getString("friend_id"));

                    Allo allo = new Allo();
                    allo.setTitle(jo_friend.getString("title"));
                    allo.setArtist(jo_friend.getString("artist"));
                    allo.setId(jo_friend.getString("uid"));
                    allo.setURL(jo_friend.getString("url"));
                    if (jo_friend.has("image"))
                        allo.setImage("image");
                    if (jo_friend.has("thumbs"))
                        allo.setImage("thumbs");

                    mFriend.setAllo(allo);


                    ar_by_friend_allo_list.add(mFriend);
                }
                Log.i("count", String.valueOf(ar_by_friend_allo_list.size()));
                SingleToneData singleToneData = SingleToneData.getInstance();
                singleToneData.setByFriendAlloList(ar_by_friend_allo_list);
            } else if (st_status.equals("fail")) {
                String st_error = jo_result.getString("error");
                if (st_error.equals("not login")) {
                    LoginUtils loginUtils = new LoginUtils(context);
                    loginUtils.onLoginRequired();
                }
            }
        }catch (JSONException e){
            System.out.println(e);

        }
    }

    public void parseSetByFriendAllo(String st_result){
        try {
            JSONObject jo_result = new JSONObject(st_result);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")){

            } else if (st_status.equals("fail")) {
                String st_error = jo_result.getString("error");
                if (st_error.equals("not login")) {
                    LoginUtils loginUtils = new LoginUtils(context);
                    loginUtils.onLoginRequired();
                }
            }
        }catch (JSONException e){
            System.out.println(e);

        }
    }


    public void parseMyAlloList(String st_result) {
        ArrayList<Allo> ar_my_allo_list = new ArrayList<>();
        Allo myAllo = new Allo();

        JSONArray ja_allo_list = null;
        JSONObject jo_my_allo = null;
        String st_status = null;

        try{
            JSONObject jo_result = new JSONObject(st_result);
            JSONObject jo_response = jo_result.getJSONObject("response");
            st_status = jo_result.getString("status");
            if (st_status.equals("success")){


                jo_my_allo = jo_response.getJSONObject("my_allo");
                if (jo_my_allo.has("title")){

                    myAllo.setTitle(jo_my_allo.getString("title"));
                    myAllo.setArtist(jo_my_allo.getString("artist"));
                    myAllo.setURL(jo_my_allo.getString("url"));
                    myAllo.setId(jo_my_allo.getString("uid"));
                    if (jo_my_allo.has("thumbs"))
                        myAllo.setThumbs(jo_my_allo.getString("thumbs"));
                    if (jo_my_allo.has("image"))
                        myAllo.setImage(jo_my_allo.getString("image"));
                    myAllo.setIsPlaying(false);
                }

                ja_allo_list = jo_response.getJSONArray("my_allo_list");
                for (int i = 0; i < ja_allo_list.length(); i++) {
                    JSONObject jo_allo = ja_allo_list.getJSONObject(i);

                    Allo mAllo = new Allo();
                    mAllo.setTitle(jo_allo.getString("title"));
                    mAllo.setArtist(jo_allo.getString("artist"));
                    mAllo.setURL(jo_allo.getString("url"));
                    mAllo.setId(jo_allo.getString("uid"));
                    if (jo_allo.has("thumbs"))
                        mAllo.setThumbs(jo_allo.getString("thumbs"));
                    if (jo_allo.has("image"))
                        mAllo.setImage(jo_allo.getString("image"));
                    mAllo.setIsPlaying(false);

                    ar_my_allo_list.add(mAllo);
                }


            } else if (st_status.equals("fail")) {
                String st_error = jo_result.getString("error");
                if (st_error.equals("not login")) {
                    LoginUtils loginUtils = new LoginUtils(context);
                    loginUtils.onLoginRequired();
                }
            }
            SingleToneData singleToneData = SingleToneData.getInstance();
            singleToneData.setMyAlloList(ar_my_allo_list);
            singleToneData.setMyAllo(myAllo);

        }catch (JSONException e){
            System.out.println(e);
        }
    }
}
