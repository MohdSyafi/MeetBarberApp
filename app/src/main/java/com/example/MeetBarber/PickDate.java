package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class PickDate extends AppCompatActivity {

    private CalendarView PickDateCalendar;
    private TextView PDServiceName,PDServicePrice,PDServiceDate,PDServiceType;
    private Button PDContinueButton;
    private ImageView  PDBackButton;
    private String name, price,barber,customer,status,servicetype,date,shopname,customername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date);
        getSupportActionBar().hide();

        PickDateCalendar = findViewById(R.id.PickDateCalendar);
        PDServiceDate = findViewById(R.id.PickDateServiceDate);
        PDServiceName = findViewById(R.id.PickDateServiceName);
        PDServicePrice = findViewById(R.id.PickDateServicePrice);
        PDBackButton = findViewById(R.id.PickDateBackButton);
        PDContinueButton = findViewById(R.id.PDContinueButton);
        PDServiceType = findViewById(R.id.PickDateServiceType);

        Intent intent = getIntent();
        Bundle details = intent.getExtras();
        name = details.getString("PDSERVICENAME");
        price = details.getString("PDSERVICEPRICE");
        barber = details.getString("PDBARBERID");
        customer = details.getString("PDCUSTOMERID");
        status = details.getString("PDSERVICESTATUS");
        servicetype = details.getString("PDSERVICETYPE");
        shopname = details.getString("PDSHOPNAME");
        customername = details.getString("PDCUSTOMERNAME");

        initActivity();

        PDContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(PDServiceDate.getText().toString().equalsIgnoreCase("Please select a date")){

                    Toast.makeText(PickDate.this,"Please select a date first",Toast.LENGTH_LONG).show();

                }else{
                    Intent a = new Intent(PickDate.this,PickTimeSlot.class);
                    Bundle extras = new Bundle();

                    extras.putString("PTSERVICENAME",name);
                    extras.putString("PTSERVICEPRICE",price);
                    extras.putString("PTBARBERID",barber);
                    extras.putString("PTCUSTOMERID",customer);
                    extras.putString("PTSERVICESTATUS",status);
                    extras.putString("PTSERVICETYPE",servicetype);
                    extras.putString("PTSERVICEDATE",date);
                    extras.putString("PTSSHOPNAME",shopname);
                    extras.putString("PTSCUSTOMERNAME",customername);

                    a.putExtras(extras);
                    PickDate.this.startActivity(a);
                }

            }
        });

        PDBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        PickDateCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                String MonthName = getMonthName(i1+1);

                if(i2<10){
                    date = "0" + i2 + "/" + MonthName + "/" + i;
                }else{
                    date = i2 + "/" + MonthName + "/" + i;
                }

                PDServiceDate.setText(date);
            }
        });

    }

    private void initActivity() {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day =  cal.get(Calendar.DAY_OF_MONTH);
        month = month + 1;

        String MonthName  = getMonthName(month);

        PickDateCalendar.setMinDate(cal.getTimeInMillis());
        PickDateCalendar.setDate(cal.getTimeInMillis());

        date = day + "/" + MonthName + "/" + year;
        PDServiceDate.setText("Please select a date");
        PDServiceName.setText(name);
        PDServicePrice.setText("RM" + price);
        PDServiceType.setText(servicetype + " service");
    }

    private String getMonthName(int month) {
        String MonthName = new String();
        if(month == 1){
            MonthName = "JAN";
        }else if (month == 2){
            MonthName = "FEB";
        }else if (month == 3){
            MonthName = "MAR";
        }else if (month == 4){
            MonthName = "APR";
        }else if (month == 5){
            MonthName = "MAY";
        }else if (month == 6){
            MonthName = "JUN";
        }else if (month == 7){
            MonthName = "JUL";
        }else if (month == 8){
            MonthName = "AUG";
        }else if (month == 9){
            MonthName = "SEP";
        }else if (month == 10){
            MonthName = "OCT";
        }else if (month == 11){
            MonthName = "NOV";
        }else if (month == 12){
            MonthName = "DEC";
        }
        return MonthName;
    }

}