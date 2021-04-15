package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class History extends AppCompatActivity implements reviewClickInterface{

    private RecyclerView HistoryRecyclerView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Section> sectionList = new ArrayList<>();
    private ArrayList<apnmtDetails> apnmntList = new ArrayList<>();
    private MainRecyclerAdapter HistorymainRecyclerAdapter = new MainRecyclerAdapter(sectionList,this);
    private Section section = new Section();
    private String UserId,userType;
    private Button backButton;
    private Context context;
    private Resources resources;
    private String lang;
    private TextView pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_history);

        backButton = findViewById(R.id.HistoryBackButton);
        HistoryRecyclerView = findViewById(R.id.HistoryMainContainer);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        UserId = mAuth.getCurrentUser().getUid();
        pageTitle = findViewById(R.id.HistoryTitle);

        LinearLayoutManager manager = new LinearLayoutManager(History.this);
        HistoryRecyclerView.setLayoutManager(manager);
        HistoryRecyclerView.setAdapter(HistorymainRecyclerAdapter);
        initdata();
        checkUserType();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            context = LocaleHelper.setLocale(History.this, "ms");
            resources =  context.getResources();
            pageTitle.setText(resources.getString(R.string.sidebar_history));
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

    private void initdata() {

        db.collection("HistoryColl").document(UserId)
                .collection("Date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                            db.collection("HistoryColl").document(UserId)
                                    .collection("Date").document(queryDocumentSnapshot .getId())
                                    .collection("appointmentsID")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            apnmntList = new ArrayList();
                                            for (DocumentSnapshot querysnapshot: task.getResult()){
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
                                                ///adding appointmnets into an arraylist
                                                apnmntList.add(details);
                                                ///saving the value of the section title and the appointments arraylist inside one object
                                                String tempdate = queryDocumentSnapshot .getString("date");

                                                DateTimeFormatter df = new DateTimeFormatterBuilder()
                                                        // case insensitive to parse JAN and FEB
                                                        .parseCaseInsensitive()
                                                        // add pattern
                                                        .appendPattern("dd/MMM/yyyy")
                                                        // create formatter (use English Locale to parse month names)
                                                        .toFormatter(Locale.ENGLISH);
                                                LocalDate dateTime = LocalDate.parse(tempdate, df);

                                                section = new Section(queryDocumentSnapshot.getString("date"),dateTime,apnmntList);
                                            }

                                            Log.i("SectionList",sectionList.toString());

                                            if(section.getSectionItem() == null){
                                                section.setSectionName(null);


                                            }else{
                                                ////initializing a new array list with the section's objects
                                                sectionList.add(section);
                                                ///initializes the main recyclerview

                                                Set<Section> set = new HashSet<>(sectionList);
                                                sectionList.clear();
                                                sectionList.addAll(set);

                                                Collections.sort(sectionList, new Comparator<Section>() {
                                                    public int compare(Section o1, Section o2) {
                                                        return o1.getSectionDate().compareTo(o2.getSectionDate());
                                                    }
                                                });

                                                HistorymainRecyclerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    @Override
    public void onReviewClick(int position, String documentid,String documentDate,String barberid,String customerid) {
        Intent a = new Intent(this, Review.class);
        Bundle extras = new Bundle();

        extras.putString("DETAILDOCUNENTID",documentid);
        extras.putString("DETAIILDOCUMENTDATE",documentDate);
        extras.putString("DETAILBARBERID",barberid);
        extras.putString("DETAILCUSTOMERID",customerid);

        a.putExtras(extras);
        this.startActivity(a);
    }

    @Override
    public void onDetailClick(int position, String documentid,String documentDate,String barberid,String customerid) {
        Intent a = new Intent(this,Detail.class);
        Bundle extras = new Bundle();

        extras.putString("DETAILDOCUNENTID",documentid);
        extras.putString("DETAIILDOCUMENTDATE",documentDate);
        extras.putString("DETAILBARBERID",barberid);
        extras.putString("DETAILCUSTOMERID",customerid);
        extras.putString("DETAILCOLLECTION","HistoryColl");

        a.putExtras(extras);
        this.startActivity(a);
    }
}