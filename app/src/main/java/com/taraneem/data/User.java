package com.taraneem.data;

public class User {
    private String name;
    private String dob;
    private String phoneNo;
    private String email;

    private boolean gender;

    public String getGender() {
        return gender ? "Male" : "Female";
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public User(String name, String dob, String phoneNo, String email) {
        this.name = name;
        this.dob = dob;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public User() {
    }

    //Firebase requires to have public variables or getter/setter.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
