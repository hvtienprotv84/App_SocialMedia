package com.DoAn_Mobile.Authentication;


public class Location {
    private double latitude;
    private double longitude;

    public Location() {
        // Constructor mặc định cần thiết cho Firebase
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Có thể thêm các phương thức khác nếu cần

}

