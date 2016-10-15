package com.cademeupet.cademeupet;

import android.location.Location;

public class PetInfo {

    public String name;
    public String sex;
    public String lastLocation;

    public PetInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    public PetInfo(String name, String sex) {
        this.name = name;
        this.sex = sex;
        lastLocation = ",";
    }

}
