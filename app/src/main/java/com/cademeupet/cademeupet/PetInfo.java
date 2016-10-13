package com.cademeupet.cademeupet;

public class PetInfo {

    public String name;
    public String sex;

    public PetInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public PetInfo(String name, String sex) {
        this.name = name;
        this.sex = sex;
    }

}
