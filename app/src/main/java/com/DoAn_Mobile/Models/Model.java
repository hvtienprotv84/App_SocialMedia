package com.DoAn_Mobile.Models;


public class Model {
    public int image;
    public String content;
    public String desc;

    public Model(int image, String content, String desc) {
        this.image = image;
        this.content = content;
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
