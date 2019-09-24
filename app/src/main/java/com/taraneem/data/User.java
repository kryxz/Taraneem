package com.taraneem.data;

import java.util.HashMap;

public class User {
    private String name, dob, id, phoneNo, email;
    private boolean gender;

    //bookings used in the recycler view. Stores the booking id, hall etc.
    private HashMap<String, String> bookings;

    public void setBookings(HashMap<String, String> bookings) {
        this.bookings = bookings;
    }

    public HashMap<String, String> getBookings() {
        return bookings;
    }

    //if Male checkbox in Register fragment is checked, then user is male.
    // Otherwise, they are a female.
    public String getGender() {
        return gender ? "Male" : "Female";
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }


    //empty constructor that can be used to initialize a User object.
    public User() {
    }

    //FireBase requires to have public variables or getter/setter.
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDob() {
        return dob.substring(0, dob.lastIndexOf('-') + 3);
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
