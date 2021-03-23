package com.example.MeetBarber;

public interface StatusClickInterface {
    void onStatusClick(int position,String documentid,String documentDate,String barberid,String finish);
    void onDetailClick(int position, String documentid,String documentDate,String barberid,String customerid);
}
