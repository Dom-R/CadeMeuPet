package com.cademeupet.cademeupet;

public class PetInfo {

    public String name;

    public PetInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getName() {
        return name;
    }

    public PetInfo(String name) {
        this.name = name;
    }

}
