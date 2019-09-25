package com.taraneem.data;

import android.app.Application;

import java.util.HashMap;


//We'll handle all application-global data here.
public class TempData extends Application {

    private static Booking currentBooking; //used to retrieve booking data later.
    private static User userData; //Gets data in MainActivity from Fire base .Used to retrieve user data in profile fragment.


    public static User getUserData() {
        return userData;
    }

    public static void setUserData(User userData) {
        TempData.userData = userData;
    }

    public static void removeBookings() {
        userData.setBookings(new HashMap<String, String>());
    }

    public static Booking getCurrentBooking() {
        return currentBooking;
    }


    public static void setCurrentBooking(Booking currentBooking) {
        TempData.currentBooking = currentBooking;
    }

}
