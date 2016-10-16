package com.cademeupet.cademeupet;

import android.location.Location;

public class PetInfo {

    public String name;
    public String sex;
    public String lastLocation;
    public String userID;
    public String specie;

    public PetInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getSpecie() {
        return specie;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    public PetInfo(String userID, String name, String sex, String specie) {
        this.userID = userID;
        this.name = name;
        this.sex = sex;
        this.specie = specie;
        lastLocation = ",";
    }

}
