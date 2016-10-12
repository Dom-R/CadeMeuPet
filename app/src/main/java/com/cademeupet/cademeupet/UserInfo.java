package com.cademeupet.cademeupet;

public class UserInfo {

    public String name;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserInfo(String name) {
        this.name = name;
    }

}