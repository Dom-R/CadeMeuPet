package com.cademeupet.cademeupet;

import java.util.ArrayList;

public class UserInfo {

    public String name;
    public String email;
    public String phoneNumber;

    public UserInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)

    }

    public String getName() {
        return name;
    }

    /*
    public void addPet(String id) {
        petsID.add(id);
    }

    public ArrayList<String> getPetList() {
        return petsID;
    }*/

    public UserInfo(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        //this.petsID = new ArrayList<String>();
    }

}