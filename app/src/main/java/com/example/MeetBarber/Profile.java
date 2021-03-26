package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    private ArrayList<services> serviceList;
    private ArrayList<HSservice> HSserviceList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private String UserId,lang;
    private TextView profileName,profileEmail,profilePhone,profileAddress,titleNS,titleHS,profileShopName,pageTitle;
    private TextView drawer_logout,drawer_language,drawer_history;
    private ImageView profilepic;
    private RecyclerView recyclerView,HSrecyclerview;
    private Button profileEditButton;
    private int from = 1;
    private DrawerLayout drawerLayout;
    private Context context;
    private Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(),Search.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.appointment:
                        startActivity(new Intent(getApplicationContext(),HomePage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        return true;
                }
                return false;
            }
        });

        HSserviceList = new ArrayList<>();
        serviceList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        UserId = mAuth.getCurrentUser().getUid();

        profileShopName = findViewById(R.id.profileShopeName);
        HSrecyclerview = findViewById(R.id.profileContainerHS);
        recyclerView = findViewById(R.id.profileContainerNS);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileAddress = findViewById(R.id.profileAddress);
        profilePhone = findViewById(R.id.profilePhone);
        profilepic = findViewById(R.id.profilePic);
        titleHS =  findViewById(R.id.titleHS);
        titleNS = findViewById(R.id.TitleNS);
        profileEditButton =findViewById(R.id.profileEditButton);
        pageTitle = findViewById(R.id.ProfileTitle);
        drawer_history = findViewById(R.id.drawer_history);
        drawer_language = findViewById(R.id.drawer_language);
        drawer_logout = findViewById(R.id.drawer_logout);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            context = LocaleHelper.setLocale(Profile.this, "ms");
            resources =  context.getResources();
            pageTitle.setText(resources.getString(R.string.page_title_profile));
            overridePendingTransition(0, 0);
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));
            titleNS.setText(resources.getString(R.string.normal_services_title));
            titleHS.setText(resources.getString(R.string.home_services_title));

        }else{

            context = LocaleHelper.setLocale(Profile.this, "en");
            resources =  context.getResources();
            pageTitle.setText(resources.getString(R.string.page_title_profile));
            overridePendingTransition(0, 0);
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));
            titleNS.setText(resources.getString(R.string.normal_services_title));
            titleHS.setText(resources.getString(R.string.home_services_title));
        }

        ///determining current user is barber or normal user
        DocumentReference docRef = db.collection("Users").document(UserId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    setprofileview("Users");
                    getFirebaseProfile("Users");
                    getProfileImage();
                } else {
                    setprofileview("Barbers");
                    getFirebaseProfile("Barbers");
                    getProfileImage();
                }
            }
        });

        /// go to profile edit screen
        profileEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference docRef = db.collection("Users").document(UserId);
                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.exists()) {
                            Intent i = new Intent(Profile.this, register.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(Profile.this, registerBarber.class);
                            startActivity(i);
                            finish();
                        }
                    }
                });
            }
        });



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
                context = LocaleHelper.setLocale(Profile.this, "ms");
                resources = context.getResources();
                recreate();
            }
        });
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context = LocaleHelper.setLocale(Profile.this, "en");
                resources = context.getResources();
                languageDialog.dismiss();
                recreate();
            }
        });

    }
    public void ClickSideBarProfile(View view){
        openDrawer(drawerLayout);
    }

    private void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){

        closeDrawer(drawerLayout);

    }

    public void ClickHistory(View view){
        Intent a = new Intent(this,History.class);
        this.startActivity(a);
    }

    private void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickSignOut(View view){
        signOut(this);
    }

    private void signOut(Profile profile) {
        Dialog signoutDialog = new Dialog(profile);
        Button yesbutton,nobutton;
        signoutDialog.setContentView(R.layout.signoutpopup);
        signoutDialog.show();
        yesbutton = signoutDialog.findViewById(R.id.YesSignOutButton);
        nobutton = signoutDialog.findViewById(R.id.NoSignOutButton);

        yesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile.mAuth.signOut();
                Intent a = new Intent(profile,Login.class);
                profile.startActivity(a);
                profile.finishAffinity();
            }
        });
        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signoutDialog.dismiss();
            }
        });
    }

    ////set separate view for normal users and barbers
    private void setprofileview(String coll) {
        DocumentReference ref = db.collection(coll).document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document!= null) {

                                String tempp = document.get("role").toString();

                                if(tempp.equalsIgnoreCase("Barber")){
                                    profileShopName.setText(document.getString("shopname"));
                                    setNSRAdapter();
                                    setHSRAdapter();
                                    getFirestoredata();
                                    getHSFirestoredata();

                                }else{

                                    HSrecyclerview.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.GONE);
                                    titleHS.setVisibility(View.GONE);
                                    titleNS.setVisibility(View.GONE);
                                    profileShopName.setText(null);
                                }

                            } else {
                                HSrecyclerview.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                titleHS.setVisibility(View.GONE);
                                titleNS.setVisibility(View.GONE);
                                profileShopName.setText(null);
                            }
                        } else {
                            profileShopName.setText(null);
                            HSrecyclerview.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            titleHS.setVisibility(View.GONE);
                            titleNS.setVisibility(View.GONE);

                        }
                    }
                });
    }

    ////set adapter for home services recyclerview
    private void setHSRAdapter() {
        HSRAdapter adapter= new HSRAdapter(Profile.this,HSserviceList,from);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        HSrecyclerview.setLayoutManager(layoutManager);
        HSrecyclerview.setItemAnimator(new DefaultItemAnimator());
        HSrecyclerview.setAdapter(adapter);
    }

    ////set adapter for normal services recyclerview
    private void setNSRAdapter() {
        NSRAdapter adapter= new NSRAdapter(Profile.this,serviceList,from);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    /// get list of home services from firestore
    private void getHSFirestoredata() {
        if(HSserviceList.size()>0)
            HSserviceList.clear();

        Query query = db.collection("servicesCollection").document(UserId).collection("Homeservices");

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
                Toast.makeText(Profile.this,"error: ",Toast.LENGTH_SHORT);
            }
        });

    }

   /// get listt of normal services from firestore
    private void getFirestoredata(){

        if(serviceList.size()>0)
            serviceList.clear();

        Query query = db.collection("servicesCollection").document(UserId).collection("services");

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
                Toast.makeText(Profile.this,"error: ",Toast.LENGTH_SHORT);
            }
        });

    }

    /// get user profile image from firestore
    private void getProfileImage() {
        DocumentReference ref = db.collection("UriCollection").document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (!document.get("uri").toString().equals("n/a")){
                                    Uri temp = Uri.parse(document.get("uri").toString());
                                    Picasso.get().load(temp).into(profilepic);

                                }else{
                                    profilepic.setImageResource(R.drawable.profileicon);
                                    Toast.makeText(Profile.this,"failed to load image",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                profilepic.setImageResource(R.drawable.profileicon);
                                Toast.makeText(Profile.this,"failed to load image",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            profilepic.setImageResource(R.drawable.profileicon);
                            Toast.makeText(Profile.this,"failed to load image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /// get user profile details from firestore
    private void getFirebaseProfile(String coll) {
        DocumentReference ref = db.collection(coll).document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                               profileName.setText(document.getString("username"));
                               profileEmail.setText(mAuth.getCurrentUser().getEmail());
                               String temp = document.get("phone").toString();
                               profilePhone.setText(temp);
                               String Fulladdress = document.getString("address") + ", "
                                       + document.get("postcode").toString() + ", "
                                       + document.getString("city");
                               profileAddress.setText(Fulladdress);

                            } else {
                                profileName.setText("not available");
                                profileAddress.setText("not available");
                            }
                        } else {
                            profileName.setText("not available");
                            profileAddress.setText("not available");
                        }
                    }
                });

    }

}