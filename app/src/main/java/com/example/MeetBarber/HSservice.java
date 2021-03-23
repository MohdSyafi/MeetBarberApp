package com.example.MeetBarber;

public class HSservice {
    private String servicename;
    private String price;
    private String userId;

    public HSservice(String userId, String servicename, String price) {
        setUserId(userId);
        this.servicename = servicename;
        this.price = price;
    }

    public HSservice() {
    }

    public String getServicename() {
        return servicename;
    }

    public void setServicename(String servicename) {
        this.servicename = servicename;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
