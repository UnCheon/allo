package com.allo;

/**
 * Created by baek_uncheon on 2015. 7. 23..
 */
public class Notice {
    String st_image_url;
    String st_uid;
    String st_value;

    public void setImageUrl(String st_image_url) {
        this.st_image_url = st_image_url;
    }

    public String getImageUrl() {
        return this.st_image_url;
    }

    public void setUid(String st_uid) {
        this.st_uid = st_uid;
    }

    public String getUid() {
        return this.st_uid;
    }

    public void setValue(String st_value) {
        this.st_value = st_value;
    }

    public String getValue() {
        return this.st_value;
    }

}
