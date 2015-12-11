package com.allo;

/**
 * Created by uncheon on 2015. 7. 31..
 */

public class Gift extends Allo {
    String st_msg;
    String st_sender_id;
    String st_sender_nickname;
    String i_sent_time;

    public void setMsg(String st_msg) {
        this.st_msg = st_msg;
    }

    public String getMsg() {
        return this.st_msg;
    }

    public void setSenderId(String st_sender_id) {
        this.st_sender_id = st_sender_id;
    }

    public String getSenderId() {
        return st_sender_id;
    }

    public void setSenderNickname(String st_sender_nickname) {
        this.st_sender_nickname = st_sender_nickname;
    }

    public String getSenderNickname() {
        return st_sender_nickname;
    }

    public void setSentTime(String i_sent_time) {
        this.i_sent_time = i_sent_time;
    }

    public String getSentTime() {
        return i_sent_time;
    }

}