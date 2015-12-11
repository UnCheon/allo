package com.allo;

import java.io.Serializable;

/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class Allo implements Serializable {
    String st_title;
    String st_artist;
    String st_url;
    String st_thumbs;
    String st_image;
    String st_id;

    String st_content;
    String st_uploader;

    int i_duration;
    int i_start;
    int i_end;

    boolean is_ucc;


    boolean isPlaying;

    public String getTitle() {
        return st_title;
    }

    public void setTitle(String st_title) {
        this.st_title = st_title;
    }

    public String getURL() {
        return st_url;
    }

    public void setURL(String st_url) {
        this.st_url = st_url;
    }

    public String getArtist() {
        return st_artist;
    }

    public void setArtist(String st_artist) {
        this.st_artist = st_artist;
    }

    public String getThumbs() {
        return st_thumbs;
    }

    public void setThumbs(String st_thumbs) {
        this.st_thumbs = st_thumbs;
    }

    public String getImage() {
        return st_image;
    }

    public void setImage(String st_image) {
        this.st_image = st_image;
    }

    public String getId() {
        return st_id;
    }

    public void setId(String st_id) {
        this.st_id = st_id;
    }

    public String getContent() {
        return st_content;
    }

    public void setContent(String st_content) {
        this.st_content = st_content;
    }

    public String getUploader() {
        return st_uploader;
    }

    public void setUploader(String st_uploader) {
        this.st_uploader = st_uploader;
    }

    public boolean getIsUcc() {
        return this.is_ucc;
    }

    public void setIsUcc(boolean is_ucc) {
        this.is_ucc = is_ucc;
    }

    public void setDuration(int i_duration) {
        this.i_duration = i_duration;
    }

    public int getDuration() {
        return this.i_duration;
    }

    public void setStartPoint(int i_start) {
        this.i_start = i_start;
    }

    public int getStartPoint() {
        return this.i_start;
    }

    public void setEndPoint(int i_end) {
        this.i_end = i_end;
    }

    public int getEndPoint() {
        return i_end;
    }



}
