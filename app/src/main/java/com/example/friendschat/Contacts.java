package com.example.friendschat;

public class Contacts {
    private String name;
    private String status;
    private String imageurl;
    private String uid;

    public Contacts() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Contacts(String name, String status, String imageurl, String uid) {
        this.name = name;
        this.status = status;
        this.imageurl = imageurl;
        this.uid=uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
