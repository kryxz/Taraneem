package com.taraneem.data;

public class Booking {
    public enum BookingType {
        Default, Wedding, Birthday, Graduation, Store
    }


    private String id; //used to identify booking
    private String eventDate;
    private String eventTime;
    private int eventDuration;
    private String hallName;
    private String others;
    private String hospitality;
    private String photoOptions;

    private int inviteesCount;
    private int price;

    private String bookingType;

    public int hourOfEvent() {
        return Integer.parseInt(eventTime.substring(0, eventTime.indexOf(":")));
    }

    public int minutesOfEvent() {
        return Integer.parseInt(eventTime.substring(eventTime.indexOf(":") + 1));
    }

    public String yearOfDate() {
        return eventDate.substring(0, 4);
    }

    public String monthOfDate() {
        return eventDate.substring(eventDate.indexOf('-') + 1, eventDate.lastIndexOf('-'));
    }

    public String dayOfDate() {
        return eventDate.substring(eventDate.lastIndexOf('-') + 1, eventDate.lastIndexOf('-') + 3).replace(" ", "");
    }

    public Booking() {

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    //price will be calculated according to many factors.
    public void calculatePrice() {
        price = inviteesCount * 2;
    }


    //checks if all entered data are valid.
    public boolean allFieldsOK() {
        return !eventDate.isEmpty() && !eventTime.isEmpty() &&

                eventDuration != 0 && !hallName.isEmpty() &&
                !others.isEmpty() && !hospitality.isEmpty() &&
                !photoOptions.isEmpty() && inviteesCount != 0;
    }

    public Booking(String eventDate, String eventTime, int eventDuration,
                   String hallName, String others, int inviteesCount, int price,
                   String hospitality, String photoOptions, String bookingType) {

        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventDuration = eventDuration;
        this.hallName = hallName;
        this.others = others;
        this.inviteesCount = inviteesCount;
        this.price = price;
        this.hospitality = hospitality;
        this.photoOptions = photoOptions;
        this.bookingType = bookingType;
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

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public int getInviteesCount() {
        return inviteesCount;
    }

    public void setInviteesCount(int inviteesCount) {
        this.inviteesCount = inviteesCount;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getHospitality() {
        return hospitality;
    }

    public void setHospitality(String hospitality) {
        this.hospitality = hospitality;
    }

    public String getPhotoOptions() {
        return photoOptions;
    }

    public void setPhotoOptions(String photoOptions) {
        this.photoOptions = photoOptions;
    }
}

