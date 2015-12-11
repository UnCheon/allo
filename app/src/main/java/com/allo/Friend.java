package com.allo;

/**
 * Created by baek_uncheon on 2015. 1. 29..
 */
public class Friend extends Allo {
    String st_nickname;
    String st_phone_number;
    String st_friend_id;

    public String getFriendId() {
        return st_friend_id;
    }

    public void setFriendId(String st_friend_id) {
        this.st_friend_id = st_friend_id;
    }

    public String getNickname() {
        return st_nickname;
    }

    public void setNickname(String st_nickname) {
        this.st_nickname = st_nickname;
    }

    public String getPhoneNumber() {
        return st_phone_number;
    }

    public void setPhoneNumber(String st_phone_number) {
        this.st_phone_number = st_phone_number;
    }
//
}
