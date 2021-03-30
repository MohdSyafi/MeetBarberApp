package com.example.MeetBarber;

public interface StatusClickInterface {
    void onStatusClick(int position,String documentid,String documentDate,String barberid,String finish);
    void onDetailClick(int position, String documentid,String documentDate,String barberid,String customerid);
    void onCancelClick(int position,String documentid,String documentDate,String barberid,String finish);
    void onAcceptClick(int position,String documentid,String documentDate,String barberid,String finish);
    void onCompleteClick(int position,String documentid,String documentDate,String barberid,String finish);
}
