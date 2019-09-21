package com.taraneem.data;

import android.app.Application;

//We'll handle all application-global data here.
public class TempData extends Application {

    private static Booking currentBooking;
    private static User userData;

    public static User getUserData() {
        return userData;
    }

    public static void setUserData(User userData) {
        TempData.userData = userData;
    }

    public static Booking getCurrentBooking() {

        return currentBooking;
    }

    public static void setCurrentBooking(Booking currentBooking) {
        TempData.currentBooking = currentBooking;
    }

}
