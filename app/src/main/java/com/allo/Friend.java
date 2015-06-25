package com.allo;

import java.io.Serializable;

/**
 * Created by baek_uncheon on 2015. 1. 29..
 */
public class Friend implements Serializable{
    String st_nickname;
    String st_phone_number;
    String st_id;

    String st_title;
    String st_url;
    String st_artist;
    String st_thumbs;
    String st_image;

    Allo allo;

    String st_cash;

    public String getId() {return st_id;}
    public void setId(String st_id){ this.st_id = st_id; }

    public String getNickname() { return st_nickname; }
    public void setNickname(String st_nickname) { this.st_nickname = st_nickname; }

    public String getPhoneNumber() { return st_phone_number; }
    public void setPhoneNumber(String st_phone_number) { this.st_phone_number = st_phone_number; }

    public String getTitle(){ return st_title; }
    public void setTitle(String st_title){ this.st_title= st_title; }

    public String getURL(){ return st_url; }
    public void setURL(String st_url){ this.st_url = st_url; }

    public String getArtist(){ return st_artist; }
    public void setArtist(String st_artist){ this.st_artist = st_artist; }

    public String getThumbs(){ return st_thumbs; }
    public void setThumbs(String st_thumbs){ this.st_thumbs = st_thumbs; }

    public String getImage(){ return st_image; }
    public void setImage(String st_image){ this.st_image = st_image; }


    public String getCash(){ return st_cash; }
    public void setCash(String st_cash){ this.st_cash = st_cash; }


    public Allo getAllo() {return allo;}
    public void setAllo(Allo allo){ this.allo = allo; }

}
