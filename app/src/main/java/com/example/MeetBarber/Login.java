package com.example.MeetBarber;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private Button RegButton,LogIn,regbuttonB;
    private EditText logemail,logpwd;
    private ProgressBar PBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login);

        RegButton = findViewById(R.id.regbutton);
        LogIn = findViewById(R.id.login);
        logemail = findViewById(R.id.Lemail);
        logpwd = findViewById(R.id.Lpwd);
        mAuth = FirebaseAuth.getInstance();
        PBar = findViewById(R.id.LogPBar);
        regbuttonB = findViewById(R.id.regbuttonB);

        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = logemail.getText().toString().trim();
                String pwd = logpwd.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    logemail.setError("Email is empty");
                    PBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(pwd)){
                    logpwd.setError("Password is empty");
                    PBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if(pwd.length()<6){
                    logpwd.setError("Password length must be more than 6 characters");
                    PBar.setVisibility(View.INVISIBLE);
                    return;
                }

                PBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(Login.this,"Logged in succesfully",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Login.this, HomePage.class);
                            startActivity(i);

                        }else{

                            Toast.makeText(Login.this,"Error " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });

        RegButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this, register.class);
                startActivity(i);
            }
        });

        regbuttonB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this, registerBarber.class);
                startActivity(i);
            }
        });

    }

}
