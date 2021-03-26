package com.example.MeetBarber;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends AppCompatActivity implements StatusClickInterface{

private Button SignoutButton,addServicesB, yesbutton,nobutton,acceptbutton,rejectbutton;
private FirebaseAuth mAuth;
private FirebaseFirestore db;
public FirebaseStorage storageRef;
public String UserId,placeholder,userType,lang;
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
                                                    section = new Section(queryDocumentSnapshot .getString("date"),apnmntList);
                                                }
                                            }

                                            ///check if date exist but there is no appointment
                                            if(section.getSectionItem() == null){

                                                section.setSectionName(null);

                                           }else{
                                                ////initializing a new array list with the section's objects
                                                sectionList.add(section);

                                                Set<Section> set = new HashSet<>(sectionList);
                                                sectionList.clear();
                                                sectionList.addAll(set);

                                                Log.i("checkSectionList", sectionList.toString());
                                                ///notify main recyclerview
                                                Log.i("listen", "set ");
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

    private void UpdateToken() {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    ///handle users click on status button
    @Override
    public void onStatusClick(int position,String documentId,String documentDate,String targetID, String finish) {

        ///direct user to different dialog according to their type
        if(userType.equalsIgnoreCase("Users")){

            ///for customers
            openUserDialog(documentId,documentDate, targetID);
        }else{

            ///for Barbers
            openBarberDialog(documentId,documentDate,targetID,finish);
        }
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

    private void openBarberDialog(String documentid,String documentDate, String customerID, String finish){

        ////if appointments status is "accepted"
        if(finish.equalsIgnoreCase("yes")){

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
                    DocumentReference docrefB =   db.collection("appointmentsColl").document(customerID)
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

                            db.collection("HistoryColl").document(customerID)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(customerID)
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
                                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(customerID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
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
        }else{

            ///intialize dialog for appointments that has not been accepted
            UserStatusDialog.setContentView(R.layout.barberstatuspopup);
            UserStatusDialog.show();
            acceptbutton = UserStatusDialog.findViewById(R.id.AcceptHPbutton);
            rejectbutton = UserStatusDialog.findViewById(R.id.RejectHPbutton);

            ///barber accept the appointments
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
                    DocumentReference docrefB =   db.collection("appointmentsColl").document(customerID)
                            .collection("Date").document(documentDate)
                            .collection("appointmentsID").document(documentid);

                    docrefB.update(updateStatus);

                    ///notify the acceptence of appointments
                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(customerID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
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

            ///barber reject the appointment
            rejectbutton.setOnClickListener(new View.OnClickListener() {
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

                    DocumentReference docrefB =   db.collection("appointmentsColl").document(customerID)
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

                            db.collection("HistoryColl").document(customerID)
                                    .collection("Date").document(documentDate)
                                    .collection("appointmentsID").document(documentid)
                                    .set(HistoryDetails);

                            db.collection("HistoryColl").document(customerID)
                                    .collection("Date").document(documentDate)
                                    .set(HistoryDate);

                            docrefB.delete();
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(customerID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String usertoken=snapshot.getValue(String.class);
                            sendNotifications(usertoken, "Appointment rejected by barber","We are sorry for the inconveniences");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

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

    private void openUserDialog(String documentid,String documentDate,String barberID) {

        UserStatusDialog.setContentView(R.layout.statuspopup);
        UserStatusDialog.show();
        yesbutton = UserStatusDialog.findViewById(R.id.YesHPButton);
        nobutton = UserStatusDialog.findViewById(R.id.NoHPButton);

        Toast.makeText(HomePage.this," id: "+ documentid,Toast.LENGTH_LONG).show();

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

                DocumentReference docrefB =   db.collection("appointmentsColl").document(barberID)
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

                        db.collection("HistoryColl").document(barberID)
                                .collection("Date").document(documentDate)
                                .collection("appointmentsID").document(documentid)
                                .set(HistoryDetails);

                        db.collection("HistoryColl").document(barberID)
                                .collection("Date").document(documentDate)
                                .set(HistoryDate);

                        docrefB.delete();
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("Tokens").child(barberID).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String usertoken=snapshot.getValue(String.class);
                        sendNotifications(usertoken, "Appointment cancelled","Open your app to see the details");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
