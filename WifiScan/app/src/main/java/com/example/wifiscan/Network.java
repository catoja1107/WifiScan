package com.example.wifiscan;

public class Network {
    private String ssid, mac_address, wpa;
    private double latitude, longitude, strength, elevation;

    public Network(String ssid, double latitude, double longitude, String mac_address, String wpa, double strength) {
        this.ssid = ssid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mac_address = mac_address;
        this.wpa = wpa;
        this.strength = strength;
        this.elevation = -1;
    }

    public Network(String ssid, double latitude, double longitude, double elevation, String mac_address, String wpa, double strength) {
        this.ssid = ssid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mac_address = mac_address;
        this.wpa = wpa;
        this.strength = strength;
        this.elevation = elevation;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getWpa() {
        return wpa;
    }

    public void setWpa(String wpa) {
        this.wpa = wpa;
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

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }
}
