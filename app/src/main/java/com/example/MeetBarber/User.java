package com.example.MeetBarber;

import android.net.Uri;

public class User {

    private String username, address, city, role, shopname, piclink,rating;
    private Integer phone, postcode;

    public User(String username, String address, String city, String role, String shopname, String piclink, String rating, Integer phone, Integer postcode) {
        this.username = username;
        this.address = address;
        this.city = city;
        this.role = role;
        this.shopname = shopname;
        this.piclink = piclink;
        this.rating = rating;
        this.phone = phone;
        this.postcode = postcode;
    }


    public User(String username, String address, String city, String role, String shopname, String rating, Integer phone, Integer postcode) {
        this.username = username;
        this.address = address;
        this.city = city;
        this.role = role;
        this.shopname = shopname;
        this.rating = rating;
        this.phone = phone;
        this.postcode = postcode;
    }

    public User(){

    }

    public String getRating() {
        return rating;
    }

    public String getPiclink() {
        return piclink;
    }

    public void setPiclink(String piclink) {
        this.piclink = piclink;
    }

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getRole() {
        return role;
    }

    public String getShopname() {
        return shopname;
    }

    public Integer getPhone() {
        return phone;
    }

    public Integer getPostcode() {
        return postcode;
    }
}
