package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MeetBarber.SendNotificationPack.APIService;
import com.example.MeetBarber.SendNotificationPack.Client;
import com.example.MeetBarber.SendNotificationPack.Data;
import com.example.MeetBarber.SendNotificationPack.MyResponse;
import com.example.MeetBarber.SendNotificationPack.NotificationSender;
import com.example.MeetBarber.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickTimeSlot extends AppCompatActivity implements BookClickInterface{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String name,price,barberId,customerId,status,servicetype,date,appointmentId,shopname,customername;
    private TextView PTSServiceName,PTSServicePrice,PTSServiceDate,PTSServiceType,PTSServiceTime;
    private ArrayList<TimeSlot> TimeSlotList = new ArrayList<>();
    private ArrayList<String> CheckList = new ArrayList<>();
    private Button PTSConfirmButton;
    private ImageView PTSBackButton;
    private RecyclerView PTSrecyclerview;
    PTSRecyclerAdapter adapter;
    private Dialog resultDialog;
    private Handler mHandler;
    private APIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_time_slot);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        Bundle details = intent.getExtras();
        name = details.getString("PTSERVICENAME");
        price = details.getString("PTSERVICEPRICE");
        barberId = details.getString("PTBARBERID");
        customerId = details.getString("PTCUSTOMERID");
        status = details.getString("PTSERVICESTATUS");
        servicetype = details.getString("PTSERVICETYPE");
        date = details.getString("PTSERVICEDATE");
        getShopName();
        getCustomerName();

        resultDialog = new Dialog(this);
        mHandler = new Handler();
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        PTSServiceDate = findViewById(R.id.PickTimeSlotServiceDate);
        PTSServiceName = findViewById(R.id.PickTimeSlotServiceName);
        PTSServicePrice = findViewById(R.id.PickTimeSlotServicePrice);
        PTSBackButton = findViewById(R.id.PickTimeSlotBackButton);
        PTSConfirmButton = findViewById(R.id.PickTimeSlotContinueButton);
        PTSServiceType = findViewById(R.id.PickTimeSlotServiceType);
        PTSrecyclerview = findViewById(R.id.PTScontainer);
        PTSServiceTime = findViewById(R.id.PickTimeSlotServiceTime);

        initActivity();
        getBarberOperationDetails();

        adapter = new PTSRecyclerAdapter(TimeSlotList,this,this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(PickTimeSlot.this,2,GridLayoutManager.VERTICAL,false);
        PTSrecyclerview.setLayoutManager(gridLayoutManager);
        PTSrecyclerview.setAdapter(adapter);

        PTSBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        PTSConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PTSServiceTime.getText().equals("Please select a time slot")){
                    Toast.makeText(PickTimeSlot.this,"Please select a time slot first",Toast.LENGTH_LONG).show();
                }else{

                    UpdateToken();

                    String name, price,timeslot,date,barber,customer,status,type;

                    name = PTSServiceName.getText().toString();
                    price = PTSServicePrice.getText().toString();
                    timeslot = PTSServiceTime.getText().toString();
                    date =  PTSServiceDate.getText().toString();
                    barber = barberId;
                    customer = customerId;
                    status = "On hold";
                    type = servicetype;

                    String temp = date.replaceAll("/" ,"");

                    appointmentId = timeslot + " " + temp;

                    final Map<String, Object> appoint = new HashMap<>();

                    appoint.put("name",name );
                    appoint.put("price",price);
                    appoint.put("time slot",timeslot);
                    appoint.put("date",date);
                    appoint.put("barberID",barber);
                    appoint.put("customerID",customer);
                    appoint.put("status",status);
                    appoint.put("type",type);
                    appoint.put("customer name",customername);
                    appoint.put("shop name",shopname);
                    appoint.put("review","no");

                    final Map<String, Object> datedoc = new HashMap<>();
                    datedoc.put("date",date );

                    db.collection("appointmentsColl").document(barberId)
                            .collection("Date").document(temp)
                            .collection("appointmentsID").document(appointmentId)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    resultDialog.setContentView(R.layout.requestresultpopup);
                                    resultDialog.findViewById(R.id.SuccessTV).setVisibility(View.INVISIBLE);
                                    resultDialog.findViewById(R.id.resultimageView).setVisibility(View.INVISIBLE);

                                    resultDialog.show();

                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            resultDialog.dismiss();
                                        }
                                    },3000);

                                } else {

                                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(barberId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String usertoken=snapshot.getValue(String.class);
                                            sendNotifications(usertoken, "New Booking request","Open your app to see the details");
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });



                                    db.collection("appointmentsColl").document(barberId)
                                            .collection("Date").document(temp)
                                            .collection("appointmentsID").document(appointmentId)
                                            .set(appoint);

                                    db.collection("appointmentsColl").document(barberId)
                                            .collection("Date").document(temp)
                                            .set(datedoc);


                                    db.collection("appointmentsColl").document(customerId)
                                            .collection("Date").document(temp)
                                            .collection("appointmentsID").document(appointmentId)
                                            .set(appoint);

                                    db.collection("appointmentsColl").document(customerId)
                                            .collection("Date").document(temp)
                                            .set(datedoc);

                                    resultDialog.setContentView(R.layout.requestresultpopup);
                                    resultDialog.findViewById(R.id.failTV).setVisibility(View.INVISIBLE);
                                    resultDialog.findViewById(R.id.failimageview).setVisibility(View.INVISIBLE);
                                    resultDialog.show();

                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            resultDialog.dismiss();

                                            Intent a = new Intent(PickTimeSlot.this,HomePage.class);
                                            a .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            finish();
                                            PickTimeSlot.this.startActivity(a);
                                        }
                                    },2000L);


                                }
                            } else {
                                Log.d("Booking failed", "Failed with: ", task.getException());
                            }

                        }
                    });
                }
            }
        });
    }

    private void getCustomerName() {

        DocumentReference ref = db.collection("Users").document(customerId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            customername = document.getString("username");
                        } else {

                        }
                    }
                });
    }

    private void getShopName() {
        DocumentReference ref = db.collection("Barbers").document(barberId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            shopname = document.getString("shopname");
                        } else {

                        }
                    }
                });
    }

    private void UpdateToken() {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String temp = task.getResult();
                        String refreshToken = temp;
                        Token token= new Token(refreshToken);
                        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
                    }
                });
    }

    private void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifications(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(PickTimeSlot.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void initActivity() {
        PTSServiceDate.setText(date);
        PTSServiceName .setText(name);
        PTSServicePrice.setText(price);
        PTSServiceType.setText(servicetype);
        PTSServiceTime.setText("Please select a time slot");
    }

    private void getBarberOperationDetails() {
        DocumentReference ref = db.collection("OperationDetailsCollection").document(barberId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {

                                String ST,ET,D;
                                D = document.getString("Duration");
                                ST = document.getString("StartHours");
                                ET = document.getString("EndHours");

                                createtimeslot(D,ST,ET);

                            } else {
                                Log.d("LOGGER", "get failed with ", task.getException());
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void createtimeslot(String D, String ST, String ET) {

        String firstTime = ST;
        String secondTime = ET;

        String format = "HH:mm";

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date dateObj1 = null;
        Date dateObj2 = null;

        try {
            dateObj1 = sdf.parse("" + firstTime);
            dateObj2 = sdf.parse("" + secondTime);

            long dif = dateObj1.getTime();

            long dur = 0L;

            if(D.equalsIgnoreCase("30 minutes"))
                dur = 1800000;
            if(D.equalsIgnoreCase("45 minutes"))
                dur = 2700000;
            if(D.equalsIgnoreCase("1 hour"))
                dur = 3600000;
            if(D.equalsIgnoreCase("1 hour 30 minutes"))
                dur = 5400000 ;
            if(D.equalsIgnoreCase("2 hour"))
                dur = 7200000;

            while (dif < dateObj2.getTime()) {

                Date slot = new Date(dif);
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                String strTime= formatter.format(slot);

                long holder =  dif;

                holder  += dur;

                String endTime = formatter.format(holder);

                TimeSlot timeSlot = new TimeSlot(strTime + " to " + endTime,false,false);
                TimeSlotList.add(timeSlot);

                dif += dur;
            }

            if(!TimeSlotList.equals(null)){
                checkOccupied();
                adapter.notifyDataSetChanged();
            }

        } catch (ParseException e) {
            Log.i("Timeslot","error");
        }
    }

    private void checkOccupied() {
        String temp = date.replaceAll("/" ,"");

        db.collection("appointmentsColl").document(barberId)
                .collection("Date").document(temp)
                .collection("appointmentsID")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                for(QueryDocumentSnapshot querySnapshot :task.getResult() ){

                    CheckList.add(querySnapshot.getString("time slot"));
                }

                if(!CheckList.isEmpty()){
                    for(int j=0;j<TimeSlotList.size();j++){
                        if(CheckList.contains(TimeSlotList.get(j).getTimeslot())){
                            TimeSlotList.get(j).setBooked(true);
                        }
                        adapter.notifyDataSetChanged();
                    }

                }

            }
        });
    }

    @Override
    public void onItemClick(int position) {
        PTSServiceTime.setText(TimeSlotList.get(position).getTimeslot());
    }

    @Override
    public void onHomeClick(int position) {

    }
}