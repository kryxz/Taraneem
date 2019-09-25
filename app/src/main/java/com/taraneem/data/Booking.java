package com.taraneem.data;

import android.content.res.Resources;

import com.taraneem.R;

import java.util.Arrays;

//class used for storing booking data
public class Booking {
    //used to get strings from resources
    private Resources res;


    private String id; //used to identify booking
    private String eventDate, eventTime, hallName,
            others, hospitality, photoOptions, bookingType;
    private int eventDuration, inviteesCount, price, hallCost;

    //empty constructor
    public Booking() {

    }

    public void setRes(Resources res) {
        this.res = res;
    }

    //returns the hour of an event from eventDate
    public int hourOfEvent() {
        return Integer.parseInt(eventTime.substring(0, eventTime.indexOf(":")));
    }

    //returns minutes of an event from eventDate
    public int minutesOfEvent() {
        return Integer.parseInt(eventTime.substring(eventTime.indexOf(":") + 1));
    }

    //returns year of an event from eventDate
    public String yearOfDate() {
        return eventDate.substring(0, 4);
    }

    //returns year of an event from eventDate
    public String monthOfDate() {
        return eventDate.substring(eventDate.indexOf('-') + 1, eventDate.lastIndexOf('-'));
    }

    //returns day of an event from eventDate
    public String dayOfDate() {
        return eventDate.substring(eventDate.lastIndexOf('-') + 1, eventDate.lastIndexOf('-') + 3).replace(" ", "");
    }

    //price is affected by various factors.
    private void setPrice() {
        int inviteesCost = inviteesCount / 100;
        int photographyCost = Arrays.asList(
                res.getStringArray(R.array.photographyOptions)).indexOf(photoOptions) + 1;
        int hospitalityCost = 0;
        if (hospitality.contains(res.getString(R.string.cake)))
            hospitalityCost += 1;
        if (hospitality.contains(res.getString(R.string.pepsi)))
            hospitalityCost += 1;
        if (hospitality.contains(res.getString(R.string.knafeh)))
            hospitalityCost += 1;
        price = 400 + inviteesCost * 50 +
                hallCost * 50 + photographyCost * 50 + hospitalityCost * 50;

        //floor to 1000, or ceil to 600.
        if (price < 600)
            price = 600;
        else if (price > 1000)
            price = 1000;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //checks if all entered data are valid.
    public boolean allFieldsOK() {
        return !eventDate.isEmpty() && !eventTime.isEmpty() &&

                eventDuration != 0 && !hallName.isEmpty() &&
                !others.isEmpty() && !hospitality.isEmpty() &&
                !photoOptions.isEmpty() && inviteesCount != 0;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public int getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(int eventDuration) {
        this.eventDuration = eventDuration;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName, int cost) {
        this.hallName = hallName;
        hallCost = cost;
        setPrice();
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
        setPrice();
    }

    public int getInviteesCount() {
        return inviteesCount;
    }

    public void setInviteesCount(int inviteesCount) {
        this.inviteesCount = inviteesCount;
        setPrice();
    }

    public int getPrice() {
        return price;
    }

    public String getHospitality() {
        return hospitality;
    }

    public void setHospitality(String hospitality) {
        this.hospitality = hospitality;
        setPrice();
    }

    public String getPhotoOptions() {
        return photoOptions;
    }

    public void setPhotoOptions(String photoOptions) {
        this.photoOptions = photoOptions;
        setPrice();
    }

    //booking types. default equals null
    public enum BookingType {
        Default, Wedding, Birthday, Graduation, Store
    }
}

