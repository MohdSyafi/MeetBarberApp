package com.example.MeetBarber;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MeetBarber.SendNotificationPack.APIService;
import com.example.MeetBarber.SendNotificationPack.Client;
import com.example.MeetBarber.SendNotificationPack.Data;
import com.example.MeetBarber.SendNotificationPack.MyResponse;
import com.example.MeetBarber.SendNotificationPack.NotificationSender;
import com.example.MeetBarber.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends AppCompatActivity implements StatusClickInterface{

private Button SignoutButton,addServicesB, yesbutton,nobutton,acceptbutton,rejectbutton;
private FirebaseAuth mAuth;
private FirebaseFirestore db = FirebaseFirestore.getInstance();
public FirebaseStorage storageRef;
public String UserId,placeholder,userType,lang,keyword;
private RecyclerView MainRecyclerView;
private ArrayList<Section> sectionList = new ArrayList<>();
private ArrayList<String> Datelist  = new ArrayList<>();
private ArrayList<apnmtDetails>  apnmntList = new ArrayList<>();
private MainRecyclerAdapter mainRecyclerAdapter = new MainRecyclerAdapter(sectionList,this);
private Section section = new Section();
private ListenerRegistration HPListener;
private BroadcastReceiver currentActivityReceiver;
private Dialog UserStatusDialog,signoutDialog,updateDialog;
private APIService apiService;
private DrawerLayout drawerLayout;
private Context context;
private Resources resources;
private Handler mHandler;
private Boolean lang_selected;
private TextView pagetitle , drawer_logout,drawer_language,drawer_history;
List<String> KWlist = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.homepage);

        updateDialog = new Dialog(this);
        mHandler = new Handler();

        sectionList.clear();
        apnmntList.clear();

        ///initialize api and variable
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        UserStatusDialog = new Dialog(this);
        signoutDialog = new Dialog(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ///storageRef = FirebaseStorage.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.appointment);
        drawerLayout = findViewById(R.id.drawer_layout);
        MainRecyclerView = findViewById(R.id.MainContainer);
        pagetitle = findViewById(R.id.AppointmentTitle);
        drawer_history = findViewById(R.id.drawer_history);
        drawer_language = findViewById(R.id.drawer_language);
        drawer_logout = findViewById(R.id.drawer_logout);

        ///check for customer or barber
        checkUserType();

        ///Control the bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(),Search.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.appointment:
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        ///Get user token for FCM
        UpdateToken();

        ///initialize recyclerview
        LinearLayoutManager manager = new LinearLayoutManager(HomePage.this);
        MainRecyclerView.setLayoutManager(manager);
        MainRecyclerView.setAdapter(mainRecyclerAdapter);

        initdata();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            context = LocaleHelper.setLocale(HomePage.this, "ms");
            resources =  context.getResources();
            pagetitle.setText(resources.getString(R.string.page_title_Appointments));
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));

        }else{

            context = LocaleHelper.setLocale(HomePage.this, "en");
            resources =  context.getResources();
            pagetitle.setText(resources.getString(R.string.page_title_Appointments));
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));
        }


    }

    private void createKeyWord() {

        String coll;
        if(userType.equalsIgnoreCase("Users")){
            Log.i("Keyword", "No keyword needed ");

        }else{
            coll = "Barbers";

            DocumentReference ref = db.collection(coll).document(UserId);
            ref.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {

                                    String name, shopname, email, city, address, postcode, rating;

                                    name = document.getString("username").toLowerCase();
                                    splitString(name);
                                    shopname = document.getString("shopname").toLowerCase();
                                    splitString(shopname);
                                    email = mAuth.getCurrentUser().getEmail().toLowerCase();
                                    splitString(email);
                                    city = document.getString("city").toLowerCase();
                                    splitString(city);
                                    address = document.getString("address").toLowerCase();
                                    splitString(address);
                                    postcode = document.get("postcode").toString();
                                    splitString(postcode);
                                    rating = document.getString("rating").toLowerCase();
                                    splitString(rating);

                                    Query query = db.collection("servicesCollection").document(UserId).collection("services");

                                    query.get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for (DocumentSnapshot querysnapshot: task.getResult()){

                                                        String holder = querysnapshot.getString("name").toLowerCase();
                                                        splitString(holder);

                                                    }

                                                    Query query = db.collection("servicesCollection").document(UserId).collection("Homeservices");

                                                    query.get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    for (DocumentSnapshot querysnapshot: task.getResult()){

                                                                        String holder = querysnapshot.getString("name").toLowerCase();
                                                                        splitString(holder);
                                                                    }

                                                                    Map<String, Object> keywordupdate = new HashMap<>();
                                                                    keywordupdate.put("keyword", KWlist);

                                                                    DocumentReference documentReference = db.collection("Barbers").document(UserId);

                                                                    documentReference.update(keywordupdate);

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(HomePage.this,"error: ",Toast.LENGTH_SHORT);
                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(HomePage.this,"error: ",Toast.LENGTH_SHORT);
                                        }
                                    });

                                } else {
                                    Toast.makeText(HomePage.this,"Failed to create keywords",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(HomePage.this,"Failed to create keywords",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }

    private void splitString(String input) {

        String str[] = input.split(" ");
        List<String> al = new ArrayList<String>();
        al = Arrays.asList(str);

        List<String> al2 = new ArrayList<String>();
        al2 = Arrays.asList(input);

        String placeholder = "";

        for (int i = 0 ;i< al.size() ; i++){

            placeholder = "";

            for(int j = 0; j<al.get(i).length();j++){

                placeholder += al.get(i).charAt(j);
                if(!KWlist.contains(placeholder)){
                    KWlist.add(placeholder);
                }

            }

        }

        for (int i = 0 ;i< al2.size() ; i++){

            placeholder = "";

            for(int j = 0; j<al2.get(i).length();j++){

                placeholder += al2.get(i).charAt(j);
                if(!KWlist.contains(placeholder)){
                    KWlist.add(placeholder);
                }
            }

        }

    }

    public  void ClickLanguage(View view ){
        final String[] Language = {"ENGLISH", "MELAYU"};
        final int checkedItem;


        Dialog languageDialog = new Dialog(this);
        Button english, melayu;
        languageDialog.setContentView(R.layout.languagepopup);
        languageDialog.show();
        english = languageDialog.findViewById(R.id.languageEngBttn);
        melayu = languageDialog.findViewById(R.id.languageMYBttn);

        melayu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context = LocaleHelper.setLocale(HomePage.this, "ms");
                resources = context.getResources();
                recreate();
            }
        });
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context = LocaleHelper.setLocale(HomePage.this, "en");
                resources = context.getResources();
                languageDialog.dismiss();
                recreate();
            }
        });

    }
   public void SideBarOnclick(View view){
        openDrawer(drawerLayout);
    }

    ///open sidebar
   public void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    ///close sidebar
    public void ClickLogo(View view){
        
        closeDrawer(drawerLayout);

    }

    ///start history activity
    public void ClickHistory(View view){
        Intent a = new Intent(this,History.class);
        this.startActivity(a);
    }

    private static void closeDrawer(DrawerLayout drawerLayout) {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){

            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }


    public void ClickSignOut(View view){
        signOut(this);
    }

    ///log user out
    private void signOut(HomePage homePage) {

        Dialog signoutDialog = new Dialog(homePage);
        Button yesbutton,nobutton;
        FirebaseAuth currentAuth;
        currentAuth = FirebaseAuth.getInstance();

        signoutDialog.setContentView(R.layout.signoutpopup);
        signoutDialog.show();
        yesbutton = signoutDialog.findViewById(R.id.YesSignOutButton);
        nobutton = signoutDialog.findViewById(R.id.NoSignOutButton);

        yesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homePage.mAuth.signOut();
                Intent a = new Intent(homePage,Login.class);
                homePage.startActivity(a);
                homePage.finishAffinity();
            }
        });
        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signoutDialog.dismiss();
            }
        });

    }

    ///get appointments list from firestore
    public void initdata(){

        sectionList.clear();
        apnmntList.clear();

        ///get the list of date
        db.collection("appointmentsColl").document(UserId)
                .collection("Date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                            ///get the list of appointments inside the same date
                            db.collection("appointmentsColl").document(UserId)
                                    .collection("Date").document(queryDocumentSnapshot .getId())
                                    .collection("appointmentsID")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            apnmntList = new ArrayList();
                                            for (DocumentSnapshot querysnapshot: task.getResult()){

                                                ///save appointment fields to class
                                                apnmtDetails details = new apnmtDetails(
                                                        querysnapshot.getString("customer name"),
                                                        querysnapshot.getString("barberID"),
                                                        querysnapshot.getString("shop name"),
                                                        querysnapshot.getString("name"),
                                                        querysnapshot.getString("type"),
                                                        querysnapshot.getString("status"),
                                                        querysnapshot.getString("price"),
                                                        querysnapshot.getString("time slot"),
                                                        querysnapshot.getString("date"),
                                                        querysnapshot.getString("customerID"),
                                                        userType,
                                                        querysnapshot.getId(),
                                                        querysnapshot.getString("review")
                                                );

                                                if(!details.getServicestatus().equalsIgnoreCase("Completed")){
                                                    ///adding appointmnets into an arraylist
                                                    apnmntList.add(details);
                                                    ///saving the value of the section title and the appointments arraylist inside one object
                                                    String tempdate = queryDocumentSnapshot .getString("date");

                                                    DateTimeFormatter f = new DateTimeFormatterBuilder().parseCaseInsensitive()
                                                            .append(DateTimeFormatter.ofPattern("dd/MMM/yyyy")).toFormatter(Locale.ENGLISH);

                                                    LocalDate datetime = LocalDate.parse(tempdate, f);

                                                    section = new Section(queryDocumentSnapshot .getString("date"),datetime,apnmntList);
                                                }
                                            }

                                            ///check if date exist but there is no appointment
                                            if(section.getSectionItem() == null){

                                                section.setSectionName(null);

                                           }else{
                                                ////initializing a new array list with the section's objects
                                                sectionList.add(section);

                                                ///remove duplicates if there are any
                                                Set<Section> set = new HashSet<>(sectionList);
                                                sectionList.clear();
                                                sectionList.addAll(set);

                                                ///sort according to date
                                                Collections.sort(sectionList, new Comparator<Section>() {
                                                    public int compare(Section o1, Section o2) {
                                                        return o1.getSectionDate().compareTo(o2.getSectionDate());
                                                    }
                                                });
                                                ///notify main recyclerview
                                                mainRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();

        ////handle notifications
        currentActivityReceiver = new CurrentActivityReceiver(this);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(currentActivityReceiver, CurrentActivityReceiver.CURRENT_ACTIVITY_RECEIVER_FILTER);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mainRecyclerAdapter.notifyDataSetChanged();

        ///close broadcast receiver
        LocalBroadcastManager.getInstance(this).
                unregisterReceiver(currentActivityReceiver);
        currentActivityReceiver = null;

        closeDrawer(drawerLayout);

    }

    private String getDateinNumber(String date) {

        String string = date;
        String[] parts = string.split("/");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        String NumDate = new String();

        if(part2.equalsIgnoreCase("JAN")){
            NumDate = part1+"/01/"+part3;
        }else if(part2.equalsIgnoreCase("FEB")){
            NumDate = part1+"/02/"+part3;
        }else if(part2.equalsIgnoreCase("MAR")){
            NumDate = part1+"/03/"+part3;
        }else if(part2.equalsIgnoreCase("APR")){
            NumDate = part1+"/04/"+part3;
        }else if(part2.equalsIgnoreCase("MAY")){
            NumDate = part1+"/05/"+part3;
        }else if(part2.equalsIgnoreCase("JUN")){
            NumDate = part1+"/06/"+part3;
        }else if(part2.equalsIgnoreCase("JUL")){
            NumDate = part1+"/07/"+part3;
        }else if(part2.equalsIgnoreCase("AUG")){
            NumDate = part1+"/08/"+part3;
        }else if(part2.equalsIgnoreCase("SEP")){
            NumDate = part1+"/09/"+part3;
        }else if(part2.equalsIgnoreCase("OCT")){
            NumDate = part1+"/10/"+part3;
        }else if(part2.equalsIgnoreCase("NOV")){
            NumDate = part1+"/11/"+part3;
        }else if(part2.equalsIgnoreCase("DEC")){
            NumDate = part1+"/12/"+part3;
        }
        return NumDate;
    }

    private void checkUserType() {
        DocumentReference docRef = db.collection("Users").document(UserId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    userType = "Users";
                    createKeyWord();
                } else {
                    userType = "Barbers";
                    createKeyWord();
                }
            }
        });

    }

    private void UpdateToken() {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    ///handle users click on status button
    @Override
    public void onStatusClick(int position,String documentId,String documentDate,String targetID, String finish) {
    }

    @Override
    public void onDetailClick(int position, String documentid,String documentDate,String barberid,String customerid) {
        Intent a = new Intent(this,Detail.class);

        Bundle extras = new Bundle();

        extras.putString("DETAILDOCUNENTID",documentid);
        extras.putString("DETAIILDOCUMENTDATE",documentDate);
        extras.putString("DETAILBARBERID",barberid);
        extras.putString("DETAILCUSTOMERID",customerid);
        extras.putString("DETAILCOLLECTION","appointmentsColl");

        a.putExtras(extras);
        this.startActivity(a);
    }

    @Override
    public void onCancelClick(int position,String documentid,String documentDate,String targetID,String finish) {

           openCancelDialog(documentid,documentDate, targetID);
    }

    @Override
    public void onAcceptClick(int position, String documentid, String documentDate, String targetID, String finish) {
        UserStatusDialog.setContentView(R.layout.barberstatuspopup);
        UserStatusDialog.show();
        acceptbutton = UserStatusDialog.findViewById(R.id.AcceptHPbutton);
        rejectbutton = UserStatusDialog.findViewById(R.id.RejectHPbutton);

        rejectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserStatusDialog.dismiss();
            }
        });

        acceptbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> updateStatus= new HashMap<>();
                updateStatus.put("status","Accepted" );

                ///change appointment status
                DocumentReference docref =   db.collection("appointmentsColl").document(UserId)
                        .collection("Date").document(documentDate)
                        .collection("appointmentsID").document(documentid);

                docref.update(updateStatus);

                ///do the same for the customer side
                DocumentReference docrefB =   db.collection("appointmentsColl").document(targetID)
                        .collection("Date").document(documentDate)
                        .collection("appointmentsID").document(documentid);

                docrefB.update(updateStatus);

                ///notify the acceptence of appointments
                FirebaseDatabase.getInstance().getReference().child("Tokens").child(targetID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String usertoken=snapshot.getValue(String.class);
                        sendNotifications(usertoken, "Appointment accepted","Please be ready for your appointment");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                updateDialog.setContentView(R.layout.updatepopup);
                updateDialog.show();

                ////refresh activity
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        updateDialog.dismiss();

                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    }
                },2000L);
            }
        });
    }

    @Override
    public void onCompleteClick(int position, String documentid, String documentDate, String targetID, String finish) {
        ///open dialog for finishing apppointments
        UserStatusDialog.setContentView(R.layout.finishstatuspopup);
        UserStatusDialog.show();
        yesbutton = UserStatusDialog.findViewById(R.id.FinishYesButton);
        nobutton = UserStatusDialog.findViewById(R.id.FinishNOButton);

        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserStatusDialog.dismiss();
            }
        });

        yesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserStatusDialog.dismiss();

                DocumentReference docref =   db.collection("appointmentsColl").document(UserId)
                        .collection("Date").document(documentDate)
                        .collection("appointmentsID").document(documentid);

                docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        DocumentSnapshot documentSnapshot = task.getResult();

                        Map<String, Object> HistoryDetails = new HashMap<>();

                        ///get the appointment details and save into a hash map
                        HistoryDetails.put("name",documentSnapshot.get("name"));
                        HistoryDetails.put("price",documentSnapshot.get("price"));
                        HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                        HistoryDetails.put("date",documentSnapshot.get("date"));
                        HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                        HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                        HistoryDetails.put("status","Completed");
                        HistoryDetails.put("type",documentSnapshot.get("type"));
                        HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                        HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                        HistoryDetails.put("review",documentSnapshot.get("review"));

                        Map<String, Object> HistoryDate = new HashMap<>();

                        HistoryDate.put("date",documentSnapshot.get("date"));

                        ////save the current appointment into History collection
                        db.collection("HistoryColl").document(UserId)
                                .collection("Date").document(documentDate)
                                .collection("appointmentsID").document(documentid)
                                .set(HistoryDetails);

                        ///save the date field in date document in the History collection
                        db.collection("HistoryColl").document(UserId)
                                .collection("Date").document(documentDate)
                                .set(HistoryDate);

                        ///delete the current appointment from the active collection
                        docref.delete();
                    }
                });

                ////do the same for the corresponding customers
                DocumentReference docrefB =   db.collection("appointmentsColl").document(targetID)
                        .collection("Date").document(documentDate)
                        .collection("appointmentsID").document(documentid);

                docrefB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        DocumentSnapshot documentSnapshot = task.getResult();

                        Map<String, Object> HistoryDetails = new HashMap<>();

                        HistoryDetails.put("name",documentSnapshot.get("name"));
                        HistoryDetails.put("price",documentSnapshot.get("price"));
                        HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                        HistoryDetails.put("date",documentSnapshot.get("date"));
                        HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                        HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                        HistoryDetails.put("status","Completed");
                        HistoryDetails.put("type",documentSnapshot.get("type"));
                        HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                        HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                        HistoryDetails.put("review",documentSnapshot.get("review"));

                        Map<String, Object> HistoryDate = new HashMap<>();

                        HistoryDate.put("date",documentSnapshot.get("date"));

                        db.collection("HistoryColl").document(targetID)
                                .collection("Date").document(documentDate)
                                .collection("appointmentsID").document(documentid)
                                .set(HistoryDetails);

                        db.collection("HistoryColl").document(targetID)
                                .collection("Date").document(documentDate)
                                .set(HistoryDate);

                        docrefB.delete();

                        updateDialog.setContentView(R.layout.updatepopup);
                        updateDialog.show();

                        ////refresh the activity
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                ///send notification to notify the completed appointments
                                FirebaseDatabase.getInstance().getReference().child("Tokens").child(targetID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String usertoken=snapshot.getValue(String.class);
                                        sendNotifications(usertoken, "Appointment completed","Open your app to leave a review");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                updateDialog.dismiss();

                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);

                            }
                        },2000L);
                    }
                });

            }
        });
    }

    private void openCancelDialog(String documentid, String documentDate, String targetID) {

        if(userType.equalsIgnoreCase("Users")){

            UserStatusDialog.setContentView(R.layout.statuspopup);
            UserStatusDialog.show();
            yesbutton = UserStatusDialog.findViewById(R.id.YesHPButton);
            nobutton = UserStatusDialog.findViewById(R.id.NoHPButton);

            ///Toast.makeText(HomePage.this," id: "+ documentid,Toast.LENGTH_LONG).show();

            nobutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserStatusDialog.dismiss();
                }
            });

            yesbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    UserStatusDialog.dismiss();

                    Map<String, Object> updateStatus= new HashMap<>();
                    updateStatus.put("status","Cancelled" );

                    DocumentReference docref =   db.collection("appointmentsColl").document(UserId)
                            .collection("Date").document(documentDate)
                            .collection("appointmentsID").document(documentid);

                    docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            Map<String, Object> HistoryDetails = new HashMap<>();

                            HistoryDetails.put("name",documentSnapshot.get("name"));
                            HistoryDetails.put("price",documentSnapshot.get("price"));
                            HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                            HistoryDetails.put("date",documentSnapshot.get("date"));
                            HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                            HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                            HistoryDetails.put("status","Cancelled");
                            HistoryDetails.put("type",documentSnapshot.get("type"));
                            HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                            HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                            HistoryDetails.put("review",documentSnapshot.get("review"));

                            Map<String, Object> HistoryDate = new HashMap<>();

                            HistoryDate.put("date",documentSnapshot.get("date"));

                            db.collection("HistoryColl").document(UserId)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(UserId)
                                    .collection("Date").document(documentDate)
                                    .set(HistoryDate);

                            docref.delete();
                        }
                    });

                    DocumentReference docrefB =   db.collection("appointmentsColl").document(targetID)
                            .collection("Date").document(documentDate)
                            .collection("appointmentsID").document(documentid);

                    docrefB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            Map<String, Object> HistoryDetails = new HashMap<>();

                            HistoryDetails.put("name",documentSnapshot.get("name"));
                            HistoryDetails.put("price",documentSnapshot.get("price"));
                            HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                            HistoryDetails.put("date",documentSnapshot.get("date"));
                            HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                            HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                            HistoryDetails.put("status","Cancelled");
                            HistoryDetails.put("type",documentSnapshot.get("type"));
                            HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                            HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                            HistoryDetails.put("review",documentSnapshot.get("review"));

                            Map<String, Object> HistoryDate = new HashMap<>();

                            HistoryDate.put("date",documentSnapshot.get("date"));

                            db.collection("HistoryColl").document(targetID)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(targetID)
                                    .collection("Date").document(documentDate)
                                    .set(HistoryDate);

                            docrefB.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(targetID)
                                            .child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String usertoken=snapshot.getValue(String.class);
                                            sendNotifications(usertoken, "Appointment cancelled","Open your app to see the details");
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    });




                    updateDialog.setContentView(R.layout.updatepopup);
                    updateDialog.show();


                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateDialog.dismiss();

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);


                        }
                    },2000L);


                }
            });
        }else{

            UserStatusDialog.setContentView(R.layout.rejectstatuspopup);
            UserStatusDialog.show();
            yesbutton = UserStatusDialog.findViewById(R.id.BarberRejectButton);
            nobutton = UserStatusDialog.findViewById(R.id.BarberCancelRejectButton);

            nobutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserStatusDialog.dismiss();
                }
            });

            yesbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserStatusDialog.dismiss();

                    Map<String, Object> updateStatus= new HashMap<>();
                    updateStatus.put("status","Cancelled" );

                    DocumentReference docref =   db.collection("appointmentsColl").document(UserId)
                            .collection("Date").document(documentDate)
                            .collection("appointmentsID").document(documentid);

                    docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            Map<String, Object> HistoryDetails = new HashMap<>();

                            HistoryDetails.put("name",documentSnapshot.get("name"));
                            HistoryDetails.put("price",documentSnapshot.get("price"));
                            HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                            HistoryDetails.put("date",documentSnapshot.get("date"));
                            HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                            HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                            HistoryDetails.put("status","Rejected");
                            HistoryDetails.put("type",documentSnapshot.get("type"));
                            HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                            HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                            HistoryDetails.put("review",documentSnapshot.get("review"));

                            Map<String, Object> HistoryDate = new HashMap<>();

                            HistoryDate.put("date",documentSnapshot.get("date"));

                            db.collection("HistoryColl").document(UserId)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(UserId)
                                    .collection("Date").document(documentDate)
                                    .set(HistoryDate);

                            docref.delete();
                        }
                    });

                    DocumentReference docrefB =   db.collection("appointmentsColl").document(targetID)
                            .collection("Date").document(documentDate)
                            .collection("appointmentsID").document(documentid);

                    docrefB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            Map<String, Object> HistoryDetails = new HashMap<>();

                            HistoryDetails.put("name",documentSnapshot.get("name"));
                            HistoryDetails.put("price",documentSnapshot.get("price"));
                            HistoryDetails.put("time slot",documentSnapshot.get("time slot"));
                            HistoryDetails.put("date",documentSnapshot.get("date"));
                            HistoryDetails.put("barberID",documentSnapshot.get("barberID"));
                            HistoryDetails.put("customerID",documentSnapshot.get("customerID"));
                            HistoryDetails.put("status","Rejected");
                            HistoryDetails.put("type",documentSnapshot.get("type"));
                            HistoryDetails.put("customer name",documentSnapshot.get("customer name"));
                            HistoryDetails.put("shop name",documentSnapshot.get("shop name"));
                            HistoryDetails.put("review",documentSnapshot.get("review"));

                            Map<String, Object> HistoryDate = new HashMap<>();

                            HistoryDate.put("date",documentSnapshot.get("date"));

                            db.collection("HistoryColl").document(targetID)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(targetID)
                                    .collection("Date").document(documentDate)
                                    .set(HistoryDate);

                            docrefB.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(targetID)
                                            .child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String usertoken=snapshot.getValue(String.class);
                                            sendNotifications(usertoken, "Appointment rejected by barber",
                                                    "We are sorry for the inconveniences");
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            });
                        }
                    });

                    updateDialog.setContentView(R.layout.updatepopup);
                    updateDialog.show();

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updateDialog.dismiss();

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);

                        }
                    },2000L);
                }
            });
        }

    }

    private void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifications(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(HomePage.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }
}
