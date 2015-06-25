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

    String st_rank;

    boolean is_playing;


    String ringTitle;
    String ringSinger;
    String ringAlbum;
    String ringURL;
    String ringId;
    String ringRank;
    String count;
    boolean isPlaying;

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

    public String getId(){ return st_id;}
    public void setId(String st_id){ this.st_id = st_id; }

    public String getRank(){ return st_rank;}
    public void setRank(String st_rank){ this.st_rank = st_rank; }

    public void setIsPlaying (boolean is_playing){this.isPlaying = is_playing;}
    public boolean getIsPlaying(){return is_playing;}






    public void setRingTitle(String ringTitle) {
        this.ringTitle = ringTitle;
    }
    public String getRingTitle(){return ringTitle;}

    public void setRingSinger(String ringSinger) {
        this.ringSinger = ringSinger;
    }
    public String getRingSinger(){return ringSinger;}

    public void setRingAlbum(String ringAlbum) {
        this.ringAlbum = ringAlbum;
    }
    public String getRingAlbum(){return ringAlbum;}

    public void setRingURL(String ringURL) {
        this.ringURL = ringURL;
    }
    public String getRingURL(){return ringURL;}

    public void setRingId(String ringId) {
        this.ringId = ringId;
    }
    public String getRingId(){return ringId;}


    public void setRingRank(String ringRank) {
        this.ringRank = ringRank;
    }
    public String getRingRank(){return ringRank;}

    public void setRingCount(String count) {
        this.count = count;
    }
    public String getRingCount(){return count;}

}
