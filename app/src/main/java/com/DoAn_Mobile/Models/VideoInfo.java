package com.DoAn_Mobile.Models;

import com.DoAn_Mobile.Authentication.User;

public class VideoInfo {
    String iduser;
    private String url;
    private String description;

    public VideoInfo() {
    }

    public VideoInfo(String url, String description){
        this.url = url;
        this.description = description;
    }
    public VideoInfo(String iduser, String url, String description) {
        this.iduser = iduser;
        this.url = url;
        this.description = description;
    }
    public String getIduser() {
        return iduser;
    }
    public void setIduser(String iduser) {
        this.iduser = iduser;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}

