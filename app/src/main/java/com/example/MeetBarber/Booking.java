package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

///import android.app.DatePickerDialog;
import android.app.Dialog;
///import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MeetBarber.SendNotificationPack.APIService;
import com.example.MeetBarber.SendNotificationPack.Client;
import com.example.MeetBarber.SendNotificationPack.Data;
import com.example.MeetBarber.SendNotificationPack.MyResponse;
import com.example.MeetBarber.SendNotificationPack.NotificationSender;
import com.example.MeetBarber.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class Booking extends AppCompatActivity implements BookClickInterface, AdapterView.OnItemSelectedListener  {

    private String barberId;
    private ArrayList<services> serviceList;
    private ArrayList<HSservice> HSserviceList;
    private ArrayList<ReviewDetails> reviewDetailsList = new ArrayList<>();
    private ArrayList<String> TimeSlotList;
    private ReviewRecyclerAdapter reviewRecyclerAdapter = new ReviewRecyclerAdapter(reviewDetailsList);
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView bookEmail,bookPhone,bookAddress,bookShopName;
    private TextView bookserviceName, bookservicePrice,bookreviewnotice;
    private ImageView bookpic,BookingBackButton;
    private RecyclerView bookrecyclerView,bookHSrecyclerview,Reviewrecyclerview;
    private int from = 0;
    private Dialog Bdialog , resultDialog;
    private Button exitBookdialog, ConfirmBook;
    private EditText BookDate;
    private DatePickerDialog datePickerDialog;
    private String StartHour, EndHour, Duration;
    private Spinner BookTimeSpinner;
    private String SlotChoice , appointmentId , UserId,userType;
    private ProgressBar pBar;
    private Handler mHandler;
    private APIService apiService;
    private RatingBar BookRatingBar;
    private String ServiceType, holdername,customername,shopname,field,holderid;
    DatePickerDialog datePickerDialogtest ;
    TimePickerDialog timePickerDialog ;
    int Year, Month, Day, Hour, Minute;
    Calendar calendar ;
    View rootView;

    private ArrayAdapter spinneradapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        getSupportActionBar().hide();

        Bdialog = new Dialog(this);
        resultDialog = new Dialog(this);

        Bdialog.setContentView(R.layout.bookpopup);
        LayoutInflater inflater = this.getLayoutInflater();
        rootView = inflater.inflate(R.layout.bookpopup, null);
        final EditText text = (EditText)rootView.findViewById(R.id.BookDate);
        Bdialog.setContentView(rootView);

        mHandler = new Handler();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        barberId = getIntent().getStringExtra("BarberID");
        HSserviceList = new ArrayList<>();
        serviceList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        TimeSlotList = new ArrayList<>();

        UserId = mAuth.getCurrentUser().getUid();

        bookEmail =findViewById(R.id.bookmail);
        bookPhone = findViewById(R.id.bookPhone);
        bookAddress = findViewById(R.id.bookAddress);
        bookShopName = findViewById(R.id.bookShopeName);
        bookpic = findViewById(R.id.bookPic);
        bookrecyclerView = findViewById(R.id.bookContainerNS);
        bookHSrecyclerview = findViewById(R.id.bookContainerHS);
        Reviewrecyclerview = findViewById(R.id.reviewcontainer);
        BookingBackButton = findViewById(R.id.BookingBackButton);
        bookreviewnotice = findViewById(R.id.bookreviewnotice);
        BookRatingBar = findViewById(R.id.BookingRattingBar);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ////methods to initialize and fill the activity with barber details
        checkUserType();
        getBarberProfile("Barbers");
        getBarberImage();
        setNSRAdapter();
        getFirestoredata();
        setHSRAdapter();
        getHSFirestoredata();
        getUserName();
        UpdateToken();
        getReviewData();

        LinearLayoutManager manager = new LinearLayoutManager(Booking.this);
        Reviewrecyclerview.setLayoutManager(manager);
        Reviewrecyclerview.setAdapter(reviewRecyclerAdapter);

        BookingBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void getReviewData() {

        DocumentReference docRef = db.collection("Barbers").document(barberId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String rate = value.getString("rating");
                if(rate.equalsIgnoreCase("unrated")){
                    Reviewrecyclerview.setVisibility(View.GONE);
                    bookreviewnotice.setVisibility(View.VISIBLE);
                }else{
                    CollectionReference ref = db.collection("ReviewCollection").document(barberId).collection("Reviews");
                    ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            for (DocumentSnapshot querysnapshot: task.getResult()){

                                String cid = querysnapshot.getString("CustomerID");
                                String comm = querysnapshot.getString("comment");
                                String rate  = querysnapshot.getString("rating");
                                String date = querysnapshot.getString("date");

                                DocumentReference ref = db.collection("Users").document(cid);
                                ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        DocumentSnapshot doc  = task.getResult();

                                        String Cname =doc.getString("username");
                                        String imgUri = doc.getString("piclink");



                                        ReviewDetails reviewDetails = new ReviewDetails(
                                                imgUri,
                                                Cname,
                                                date,
                                                rate,
                                                comm
                                        );
                                        ////initializing a new array list with the section's objects
                                        reviewDetailsList.add(reviewDetails);

                                        reviewRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Booking.this,"failed to load reviews",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Booking.this,"failed to load reviews",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


    }

    private void getUserName() {

        DocumentReference ref = db.collection("Users").document(UserId);
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

    private void getBarberImage() {

        DocumentReference ref = db.collection("UriCollection").document(barberId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (!document.get("uri").toString().equals("n/a")){
                                    Uri temp = Uri.parse(document.get("uri").toString());
                                    Picasso.get().load(temp).into(bookpic);

                                }else{
                                    bookpic.setImageResource(R.drawable.profileicon);
                                    Toast.makeText(Booking.this,"failed to load image",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                bookpic.setImageResource(R.drawable.profileicon);
                                Toast.makeText(Booking.this,"failed to load image",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            bookpic.setImageResource(R.drawable.profileicon);
                            Toast.makeText(Booking.this,"failed to load image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getBarberProfile(String coll) {

        DocumentReference ref = db.collection(coll).document(barberId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                bookShopName.setText(document.getString("shopname"));
                                String temp = document.get("phone").toString();
                                bookPhone.setText(temp);
                                String Fulladdress = document.getString("address") + ", "
                                        + document.get("postcode").toString() + ", "
                                        + document.getString("city");
                                bookAddress.setText(Fulladdress);
                                bookEmail.setText(document.getString("email"));

                                if(document.getString("rating").equalsIgnoreCase("unrated")){
                                    BookRatingBar.setRating(0);
                                }else{
                                    float i  = Float.parseFloat(document.getString("rating"));
                                    BookRatingBar.setRating(i);
                                }

                            } else {
                                bookEmail.setText("not available");
                                bookPhone.setText("not available");
                                bookAddress.setText("not available");
                            }
                        } else {
                            bookEmail.setText("not available");
                            bookPhone.setText("not available");
                            bookAddress.setText("not available");
                        }
                    }
                });

    }

    private void setNSRAdapter() {
        NSRAdapter adapter= new NSRAdapter(Booking.this,serviceList,from,this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        bookrecyclerView.setLayoutManager(layoutManager);
        bookrecyclerView.setItemAnimator(new DefaultItemAnimator());
        bookrecyclerView.setAdapter(adapter);
    }

    private void getFirestoredata(){

        if(serviceList.size()>0)
            serviceList.clear();

        Query query = db.collection("servicesCollection").document(barberId).collection("services");

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot querysnapshot: task.getResult()){
                            services services = new services(querysnapshot.getId(),
                                    querysnapshot.getString("name"),querysnapshot.getString("price"));
                            serviceList.add(services);
                        }
                        setNSRAdapter();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Booking.this,"error: ",Toast.LENGTH_SHORT);
            }
        });

    }

    private void setHSRAdapter() {
        HSRAdapter adapter= new HSRAdapter(Booking.this,HSserviceList,from,this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        bookHSrecyclerview.setLayoutManager(layoutManager);
        bookHSrecyclerview.setItemAnimator(new DefaultItemAnimator());
        bookHSrecyclerview.setAdapter(adapter);
    }

    private void getHSFirestoredata() {
        if(HSserviceList.size()>0)
            HSserviceList.clear();

        Query query = db.collection("servicesCollection").document(barberId).collection("Homeservices");

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (DocumentSnapshot querysnapshot: task.getResult()){
                            HSservice services = new HSservice(querysnapshot.getId(),
                                    querysnapshot.getString("name"),querysnapshot.getString("price"));
                            HSserviceList.add(services);
                        }
                        setHSRAdapter();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Booking.this,"error: ",Toast.LENGTH_SHORT);
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
                        Toast.makeText(Booking.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void checkUserType() {
        DocumentReference docRef = db.collection("Users").document(UserId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    userType = "Users";
                } else {
                    userType = "Barbers";
                }
            }
        });

    }

    @Override
    public void onItemClick(int position) {

        if(userType!="Barbers") {
            String type = "N";

            String name, price,barber,customer,status;

            name = serviceList.get(position).getServicename();
            price = serviceList.get(position).getPrice();
            barber = barberId;
            customer = UserId;
            status = "On hold";

            Intent a = new Intent(this,PickDate.class);
            Bundle extras = new Bundle();

            extras.putString("PDSERVICENAME",name);
            extras.putString("PDSERVICEPRICE",price);
            extras.putString("PDBARBERID",barber);
            extras.putString("PDCUSTOMERID",customer);
            extras.putString("PDSERVICESTATUS",status);
            extras.putString("PDSERVICETYPE","Normal");
            extras.putString("PDSHOPNAME",bookShopName.getText().toString());
            extras.putString("PDCUSTOMERNAME",customername);

            a.putExtras(extras);
            this.startActivity(a);
        }

    }

    @Override
    public void onHomeClick(int position) {

        if(userType!="Barbers"){
            String type = "H";

            String name, price,barber,customer,status;

            name = HSserviceList.get(position).getServicename();
            price = HSserviceList.get(position).getPrice();
            barber = barberId;
            customer = UserId;
            status = "On hold";

            Intent a = new Intent(this,PickDate.class);
            Bundle extras = new Bundle();

            extras.putString("PDSERVICENAME",name);
            extras.putString("PDSERVICEPRICE",price);
            extras.putString("PDBARBERID",barber);
            extras.putString("PDCUSTOMERID",customer);
            extras.putString("PDSERVICESTATUS",status);
            extras.putString("PDSERVICETYPE", "Home");

            a.putExtras(extras);
            this.startActivity(a);
        }
        }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SlotChoice = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}