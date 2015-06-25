package com.allo;

public class Contact {
    String phone_number;
    String nickname;
    Boolean is_new;
    int friend_id;


    Contact(){}

    Contact(String phone_number, String nickname, boolean is_new){
        this.phone_number = phone_number;
        this.nickname = nickname;
        this.is_new = is_new;
    }

    Contact(String phone_number, String nickname){
        this.phone_number = phone_number;
        this.nickname = nickname;
    }
    public String getPhonenum() {
		return phone_number;
	}
	public void setPhonenum(String phone_number) {
		this.phone_number = phone_number;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {	this.nickname = nickname;}
    public Boolean getIsNew(){ return is_new; }
    public void setIsNew(Boolean is_new){ this.is_new = is_new; }

    public void setFriend_id(int friend_id){this.friend_id = friend_id;}
    public int getFriend_id(){return this.friend_id;}
}
