package com.cademeupet.cademeupet;

public class UserInfo {

    public String name;
    public String email;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

}