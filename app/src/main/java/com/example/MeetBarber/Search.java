package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class Search extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText searchField;
    private TextView pageTitle,nearbyButton,highstarButton;
    private TextView drawer_logout,drawer_language,drawer_history;
    private ImageView searchButton;
    private RecyclerView searchRecyclerview;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirestoreRecyclerAdapter<User, UsersViewHolder> adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String searchText,lang;
    private String UserId = firebaseAuth.getCurrentUser().getUid();
    private String CatChoice;
    private Context context;
    private Resources resources;
    private DrawerLayout drawerLayout;
    private Query sQuery;
    private CollectionReference sCollection =
            FirebaseFirestore.getInstance().collection("Barbers");
    static {
        FirebaseFirestore.setLoggingEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

        drawerLayout = findViewById(R.id.drawer_layout);
        searchField = findViewById(R.id.SearchField);
        searchButton = findViewById(R.id.SearchButton);
        searchRecyclerview = findViewById(R.id.SearchContainer);
        pageTitle = findViewById(R.id.SearchTitle);
        drawer_history = findViewById(R.id.drawer_history);
        drawer_language = findViewById(R.id.drawer_language);
        drawer_logout = findViewById(R.id.drawer_logout);
        nearbyButton = findViewById(R.id.NearbyButton);
        highstarButton = findViewById(R.id.highstarbutton);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        searchRecyclerview .setHasFixedSize(true);
        searchRecyclerview .setLayoutManager(manager);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.search);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.search:
                        return true;
                    case R.id.appointment:
                        startActivity(new Intent(getApplicationContext(),HomePage.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(),Profile.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        sQuery = sCollection.orderBy("shopname");
        firebaseUserSearch("search","search");

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            context = LocaleHelper.setLocale(Search.this, "ms");
            resources =  context.getResources();
            pageTitle.setText(resources.getString(R.string.page_title_Search));
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));

        }else{

            context = LocaleHelper.setLocale(Search.this, "en");
            resources =  context.getResources();
            pageTitle.setText(resources.getString(R.string.page_title_Search));
            drawer_logout.setText(resources.getString(R.string.sidebar_signout));
            drawer_language.setText(resources.getString(R.string.sidebar_language));
            drawer_history.setText(resources.getString(R.string.sidebar_history));
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText = searchField.getText().toString().toLowerCase();
                searchText.toLowerCase();
                String attribute;
                attribute = "keyword";

                String temp = searchText.toLowerCase();
                sQuery = sCollection.whereArrayContains("keyword",temp);
                firebaseUserSearch(searchText.toLowerCase(),attribute);
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchText = searchField.getText().toString();
                String attribute;
                attribute = "keyword";

                String temp = searchText.toLowerCase();
                sQuery = sCollection.whereArrayContains("keyword",temp);
                firebaseUserSearch(searchText,attribute);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        nearbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String KW = "nearby";
                checkUserType(KW);

            }
        });

        highstarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sQuery = sCollection.whereArrayContains("keyword","5.0");
                firebaseUserSearch("5.0","keyword");
            }
        });
    }

    private void checkUserType(String KW) {
        DocumentReference docRef = db.collection("Users").document(UserId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    String userType = "Users";

                    if(KW.equalsIgnoreCase("nearby")){
                        getCity(userType);
                    }

                } else {
                   String userType = "Barbers";

                    if(KW.equalsIgnoreCase("nearby")){
                        getCity(userType);
                    }
                }
            }
        });

    }

    private void getCity(String userType) {

        DocumentReference ref = db.collection(userType).document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                String kw = document.getString("city");
                                String attribute;
                                attribute = "keyword";
                                String temp = kw.toLowerCase();
                                sQuery = sCollection.whereArrayContains("keyword",temp);
                                firebaseUserSearch(kw,attribute);
                            } else {
                               Toast.makeText(Search.this,"No nearby barber could be found",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Search.this,"No nearby barber could be found",Toast.LENGTH_LONG).show();
                        }
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
                context = LocaleHelper.setLocale(Search.this, "ms");
                resources = context.getResources();
                recreate();
            }
        });
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context = LocaleHelper.setLocale(Search.this, "en");
                resources = context.getResources();
                languageDialog.dismiss();
                recreate();
            }
        });

    }
    public void ClickSideBarSearch(View view){
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

    private void signOut(Search search) {
        Dialog signoutDialog = new Dialog(search);
        Button yesbutton,nobutton;

        signoutDialog.setContentView(R.layout.signoutpopup);
        signoutDialog.show();
        yesbutton = signoutDialog.findViewById(R.id.YesSignOutButton);
        nobutton = signoutDialog.findViewById(R.id.NoSignOutButton);

        yesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.firebaseAuth.signOut();
                Intent a = new Intent(search,Login.class);
                search.startActivity(a);
                search.finishAffinity();
            }
        });
        nobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signoutDialog.dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn() && adapter != null) {
            adapter.startListening();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
       }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }


    @NonNull
    private void firebaseUserSearch(String searchText,String call){



        FirestoreRecyclerOptions<User> options =
                new FirestoreRecyclerOptions.Builder<User>()
                        .setQuery(sQuery, User.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new FirestoreRecyclerAdapter<User, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.searchlist, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, final int position, @NonNull User model) {
                holder.setName(model.getShopname());
                holder.setBarberName(model.getUsername());
                holder.setPic(model.getPiclink());
                holder.setAddress(model.getAddress() + ", " + model.getPostcode().toString() + ", " + model.getCity());
                holder.setRating(model.getRating());
                holder.setUnavailableRating(model.getRating());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String documentId = getSnapshots().getSnapshot(position).getId();
                        Intent i = new Intent(Search.this, Booking.class);
                        i.putExtra("BarberID",documentId);
                        startActivity(i);
                    }
                });
            }
        };

        if (isSignedIn()) {
            adapter.startListening();
        }
        searchRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        CatChoice = adapterView.getItemAtPosition(i).toString();
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        private View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setBarberName(String Barbername){
            TextView BarberNameView = (TextView) mView.findViewById(R.id.barberNameS);
            BarberNameView.setText(Barbername);
        }
        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.shopNameS);
            userNameView.setText(name);
        }
        public void setPic(String link){
            ImageView imageView = (ImageView)mView.findViewById(R.id.imageS);
            Picasso.get().load(link).into(imageView);
        }
        public void setAddress(String address){
            TextView addressView = (TextView) mView.findViewById(R.id.shopAddressS);
            addressView.setText(address);
        }

        public void setRating(String rating){
            RatingBar SearchRatingBar = (RatingBar)mView.findViewById(R.id.searchratingbar);
            if(rating.equalsIgnoreCase("unrated")){
                float i = 0;
                SearchRatingBar.setRating(i);
            }else{
                float i = Float.parseFloat(rating);
                SearchRatingBar.setRating(i);
            }

        }

        public void setUnavailableRating(String rating){
            TextView ratingStatus = (TextView)mView.findViewById(R.id.ratingstatusS);
            if(rating.equalsIgnoreCase("unrated")){
                ratingStatus.setVisibility(View.VISIBLE);
            }else{
                ratingStatus.setVisibility(View.GONE);
            }

        }

    }

}