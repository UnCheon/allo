package com.allo;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 12..
 */
public class SingleToneData {

    final String TAG = getClass().getSimpleName();

    ArrayList<Notice> ar_notice;

    static String phone_state = "";

    int scroll_y_0 = 0;
    int scroll_y_1 = 0;
    int scroll_y_2 = 0;
    int scroll_y_3 = 0;

    boolean is_scrolled = false;


    String st_cash = "";

    int i_left_time = 0;
    int i_volume = 0;


    Activity loginActivity = null;


    Friend myInfo;

    ArrayList<Allo> al_my_allo_list = null;
    ArrayList<Allo> al_my_allo = null;


    ArrayList<Friend> al_friend = null;
    ArrayList<Friend> al_friend_allo_list = null;

    long l_out_call_time = 0;


    private static SingleToneData uniqueInstance;

    public void SingleToneData() {
    }

    public static SingleToneData getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new SingleToneData();
        return uniqueInstance;
    }

    public void setVolume(int i_volume) {
        this.i_volume= i_volume;
    }

    public int getVolume() { return this.i_volume; }

    public void setPhoneState(String st_phone_state) {
        this.phone_state = st_phone_state;
    }

    public String getPhoneState() {
        return this.phone_state;
    }


    public Friend getMyInfo() {
        return this.myInfo;
    }


    public void setNoticeList(ArrayList ar_notice) {
        this.ar_notice = ar_notice;
    }

    public ArrayList getNoticeList() {
        return ar_notice;
    }

    public void setMyAllo(ArrayList al_my_allo) {
        if (this.al_my_allo != null)
            this.al_my_allo.clear();

        this.al_my_allo = al_my_allo;
    }

    public void addMyAllo(Allo allo) {
        if (al_my_allo == null)
            al_my_allo = new ArrayList<>();

        al_my_allo.add(allo);
    }

    public void deleteMyAllo(Allo allo) {
        for (int i=0 ; i < al_my_allo.size() ; i++){
            Allo s_allo = al_my_allo.get(i);
            if (s_allo.getId().equals(allo.getId())){
                al_my_allo.remove(i);
                break;
            }
        }
    }

    public ArrayList getMyAllo() {
        return al_my_allo;
    }


    public String getMyAlloListString() {
        JSONArray ja_my_allo = new JSONArray();

        try {
            for (int i = 0; i < al_my_allo.size(); i++) {
                Allo allo = al_my_allo.get(i);
                JSONObject jo_allo = new JSONObject();

                jo_allo.put("uid", allo.getId());
                jo_allo.put("start_point", allo.getStartPoint());
                jo_allo.put("end_point", allo.getEndPoint());

                ja_my_allo.put(jo_allo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String st_my_allo_list = ja_my_allo.toString();
        return st_my_allo_list;

    }


    public void setMyAlloList(ArrayList<Allo> al_my_allo_list) {
        if (this.al_my_allo_list != null)
            this.al_my_allo_list.clear();
        this.al_my_allo_list = al_my_allo_list;
    }

    public ArrayList<Allo> getMyAlloList() {
        return this.al_my_allo_list;
    }


//    By Friend Allo

//    public void deleteFriendAllo(Friend friend){ al_friend_allo_list.remove(friend);}
//    public void addFriendAllo(Friend friend){ al_friend_allo_list.add(friend);}
//    public void addFriendAllo(int i, Friend friend){ al_friend_allo_list.add(i, friend);}

    public void setFriendAlloList(ArrayList<Friend> al_friend_allo) {
        if (this.al_friend_allo_list != null)
            this.al_friend_allo_list.clear();
        this.al_friend_allo_list = al_friend_allo;
    }

    public ArrayList<Friend> getFriend() {
        return this.al_friend;
    }

    public void setFriend(ArrayList<Friend> al_friend) {
        if (this.al_friend != null)
            this.al_friend.clear();
        this.al_friend = al_friend;
    }

    public ArrayList<Friend> getFriendAlloList() {
        return this.al_friend_allo_list;
    }


    public void setCash(String st_cash) {
        this.st_cash = st_cash;
    }

    public String getCash() {
        return (this.st_cash);
    }

    public void setLeftTime(int i_left_time) {
        this.i_left_time = i_left_time;
    }

    public int getLeftTime() {
        return this.i_left_time;
    }


    public void setOutCallTime(long l_out_call_time) { this.l_out_call_time = l_out_call_time; }
    public long getOutCallTime(){ return this.l_out_call_time; }

    public int getTimeOutValue(){
        return 1000;
    }

    public void setLoginActivity(Activity loginActivity){ this.loginActivity = loginActivity;}
    public Activity getLoginActivity(){ return this.loginActivity; }




    public void setScroll_y_0(int y){
        this.scroll_y_0 = y;
    }

    public void setScroll_y_1(int y){
        this.scroll_y_1 = y;
    }

    public void setScroll_y_2(int y){
        this.scroll_y_2 = y;
    }

    public void setScroll_y_3(int y){
        this.scroll_y_3 = y;
    }

    public void setIsScrolled(boolean is_scrolled){
        this.is_scrolled = is_scrolled;
    }

}
