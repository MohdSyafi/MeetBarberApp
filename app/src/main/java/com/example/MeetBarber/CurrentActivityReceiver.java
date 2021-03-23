package com.example.MeetBarber;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CurrentActivityReceiver extends BroadcastReceiver {

    private static final String TAG = CurrentActivityReceiver.class.getSimpleName();
    public static final String CURRENT_ACTIVITY_ACTION = "current.activity.action";
    public static final IntentFilter CURRENT_ACTIVITY_RECEIVER_FILTER = new IntentFilter(CURRENT_ACTIVITY_ACTION);

    private Activity receivingActivity;

    public CurrentActivityReceiver(Activity activity) {
        this.receivingActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive: finishing:" + receivingActivity.getClass().getSimpleName());

        if (receivingActivity.getClass().getSimpleName().equalsIgnoreCase("HomePage")) {


            receivingActivity.recreate();

        }else{

           /// Toast.makeText(receivingActivity.getApplicationContext(),"New appointment has been added",Toast.LENGTH_SHORT).show();
        }
    }
}
