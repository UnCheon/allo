package com.allo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 12..
 */
public class SingleToneData {

    final String TAG = getClass().getSimpleName();
    String st_token;

    Friend myInfo;

    Friend currentByFriend;

    ArrayList<Friend> ar_friend_list;

    ArrayList<Friend> ar_by_friend_allo_list;

    ArrayList<Allo> ar_my_allo_list;


    private static SingleToneData uniqueInstance;

    public void SingleToneData() {}

    public static SingleToneData getInstance(){
        if(uniqueInstance == null)
            uniqueInstance = new SingleToneData();
        return uniqueInstance;
    }

    public void setToken(String st_token){
        this.st_token = st_token;
    }
    public String getToken(){
        return st_token;
    }


    public void setMainResponseData(String st_response) {
        if (myInfo == null)
            myInfo = new Friend();

        if (this.ar_friend_list == null)
            this.ar_friend_list = new ArrayList<>();

        if (this.ar_friend_list != null)
            this.ar_friend_list.clear();

        try{
            JSONObject jo_response = new JSONObject(st_response);
            JSONObject jo_my_info;
            jo_my_info = jo_response.getJSONObject("my_info");

            myInfo.setNickname(jo_my_info.getString("nickname"));
            myInfo.setPhoneNumber(jo_my_info.getString("phone_number"));

            JSONObject jo_my_allo = jo_my_info.getJSONObject("my_allo");
            try{
                Allo myAllo = new Allo();
                myAllo.setTitle(jo_my_allo.getString("title"));
                myAllo.setArtist(jo_my_allo.getString("artist"));
                myAllo.setURL(jo_my_allo.getString("url"));
                myAllo.setThumbs(jo_my_allo.getString("thumbs"));
                myAllo.setImage(jo_my_allo.getString("image"));

                myInfo.setAllo(myAllo);

            }catch (Exception e){
                Log.i("my allo json exception", "my allo json exception");
            }


            JSONArray ja_friend_list = jo_response.getJSONArray("friend_list");

            for (int i = 0; i < ja_friend_list.length(); i++) {
                JSONObject jo_friend = ja_friend_list.getJSONObject(i);

                Friend mFriend = new Friend();
                mFriend.setNickname(jo_friend.getString("nickname"));
                mFriend.setPhoneNumber(jo_friend.getString("phone_number"));
                mFriend.setId(jo_friend.getString("id"));

                JSONObject jo_friend_allo = jo_friend.getJSONObject("my_allo");
                try{
                    Allo allo = new Allo();
                    allo.setTitle(jo_friend_allo.getString("title"));
                    allo.setArtist(jo_friend_allo.getString("artist"));
                    allo.setURL(jo_friend_allo.getString("url"));
                    allo.setThumbs(jo_friend_allo.getString("thumbs"));
                    allo.setImage(jo_friend_allo.getString("image"));

                    mFriend.setAllo(allo);

                }catch (Exception e){
                    Log.i("friend allo json exception", "friend allo json exception");
                }
                this.ar_friend_list.add(mFriend);

            }


        }catch (Exception e){
            Log.i("json exception", "json exception");
        }
    }

    public void setMyAllo(Allo allo){
        myInfo.allo = allo;
    }

    public Allo getMyAllo() {
        return this.myInfo.allo;
    }

    public Friend getMyInfo(){
        return this.myInfo;
    }

    public ArrayList<Friend> getFriendList(){
        return this.ar_friend_list;
    }


    public void setByFriendAlloList(ArrayList<Friend> ar_by_friend_allo_list) {
        if (this.ar_by_friend_allo_list != null)
            this.ar_by_friend_allo_list.clear();
        this.ar_by_friend_allo_list = ar_by_friend_allo_list;
    }
    public ArrayList<Friend> getByFriendAlloList() {
        return this.ar_by_friend_allo_list;
    }

    public void setMyAlloList(ArrayList<Allo> ar_my_allo_list){
        if (this.ar_my_allo_list != null)
            this.ar_my_allo_list.clear();
        this.ar_my_allo_list = ar_my_allo_list;
    }

    public ArrayList<Allo> getMyAlloList(){
        return this.ar_my_allo_list;
    }

    public void setCurrentByFriend(Friend friend){
        this.currentByFriend = friend;
    }
    public Friend getCurrentByFriend() {
        return this.currentByFriend;
    }

}
