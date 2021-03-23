package com.example.MeetBarber;

public class services {
    private String servicename;
    private String price;
    private String userId;

    public services(String userId, String servicename, String price) {
        setUserId(userId);
        this.servicename = servicename;
        this.price = price;
    }

    public services() {
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
