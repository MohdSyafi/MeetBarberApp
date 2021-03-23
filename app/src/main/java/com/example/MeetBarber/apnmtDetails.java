package com.example.MeetBarber;

public class apnmtDetails {
    private String CustomerName;
    private String BarberName;
    private String Barbershop;
    private String ServiceName;
    private String ServiceType;
    private String Servicestatus;
    private String ServicePrice;
    private String ServiceTime;
    private String ServiceDate;
    private String CustomerID;
    private String currentusertype;
    private String documentID;
    private String review;

    public apnmtDetails(String customerName, String barberName,
                        String barbershop, String serviceName,
                        String serviceType, String servicestatus,
                        String servicePrice, String serviceTime,
                        String serviceDate, String customerID,
                        String currentusertype, String documentID,
                        String review) {
        CustomerName = customerName;
        BarberName = barberName;
        Barbershop = barbershop;
        ServiceName = serviceName;
        ServiceType = serviceType;
        Servicestatus = servicestatus;
        ServicePrice = servicePrice;
        ServiceTime = serviceTime;
        ServiceDate = serviceDate;
        CustomerID = customerID;
        this.currentusertype = currentusertype;
        this.documentID = documentID;
        this.review = review;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getCurrentusertype() {
        return currentusertype;
    }

    public void setCurrentusertype(String currentusertype) {
        this.currentusertype = currentusertype;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getServiceDate() {
        return ServiceDate;
    }

    public void setServiceDate(String serviceDate) {
        ServiceDate = serviceDate;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getBarberName() {
        return BarberName;
    }

    public void setBarberName(String barberName) {
        BarberName = barberName;
    }

    public String getBarbershop() {
        return Barbershop;
    }

    public void setBarbershop(String barbershop) {
        Barbershop = barbershop;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public void setServiceName(String serviceName) {
        ServiceName = serviceName;
    }

    public String getServiceType() {
        return ServiceType;
    }

    public void setServiceType(String serviceType) {
        ServiceType = serviceType;
    }

    public String getServicestatus() {
        return Servicestatus;
    }

    public void setServicestatus(String servicestatus) {
        Servicestatus = servicestatus;
    }

    public String getServicePrice() {
        return ServicePrice;
    }

    public void setServicePrice(String servicePrice) {
        ServicePrice = servicePrice;
    }

    public String getServiceTime() {
        return ServiceTime;
    }

    public void setServiceTime(String serviceTime) {
        ServiceTime = serviceTime;
    }

    @Override
    public String toString() {
        return "apnmtDetails{" +
                "CustomerName='" + CustomerName + '\'' +
                ", BarberName='" + BarberName + '\'' +
                ", Barbershop='" + Barbershop + '\'' +
                ", ServiceName='" + ServiceName + '\'' +
                ", ServiceType='" + ServiceType + '\'' +
                ", Servicestatus='" + Servicestatus + '\'' +
                ", ServicePrice='" + ServicePrice + '\'' +
                ", ServiceTime='" + ServiceTime + '\'' +
                '}';
    }
}
