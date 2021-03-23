package com.example.MeetBarber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null){

            Intent i = new Intent(MainActivity.this, HomePage.class);
            startActivity(i);

        }else{
            Intent i = new Intent(MainActivity.this,Login.class);
            startActivity(i);
        }
        
    }

}