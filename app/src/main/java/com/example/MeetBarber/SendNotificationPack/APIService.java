package com.example.MeetBarber.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAcIJBMy8:APA91bGV9hMhr0WOWvK7Gz2S6gyH9zDzVMYlzXvEStGHVayBphUHYIJC-F5pgYHo1DDwTGE4zZhIUmaaAvYCR__WWmfhV6rD_X_xw37cNBSEAlIl-dSkmYpRl79k98NGA-bNwn6TZu4P" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifications(@Body NotificationSender body);
}