package com.example.textapp.recycler_views.USERS;

public class userObject {

    private String uid,name ,phnum;


    public userObject(String uid, String name, String phnum) {
        this.uid =uid;
        this.name = name;
        this.phnum = phnum;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhnum() {
        return phnum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhnum(String phnum) {
        this.phnum = phnum;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
