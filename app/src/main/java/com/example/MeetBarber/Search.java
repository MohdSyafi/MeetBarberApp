package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class Search extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText searchField;
    private Button searchButton;
    private String[] categoryarray;
    private RecyclerView searchRecyclerview;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirestoreRecyclerAdapter<User, UsersViewHolder> adapter;
    private String searchText;
    private String CatChoice;
    private Spinner category;
    private DrawerLayout drawerLayout;
    static {
        FirebaseFirestore.setLoggingEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);

        categoryarray = new String[]{"Shope Name","Barber","City","Address"};

        drawerLayout = findViewById(R.id.drawer_layout);
        searchField = findViewById(R.id.SearchField);
        searchButton = findViewById(R.id.SearchButton);
        searchRecyclerview = findViewById(R.id.SearchContainer);
        category = findViewById(R.id.catspinner);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        searchRecyclerview .setHasFixedSize(true);
        searchRecyclerview .setLayoutManager(manager);

        setSpinnerAdapter();

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


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchText = searchField.getText().toString();

                int temp = category.getSelectedItemPosition();
                String spinnerposition = String.valueOf(temp);
                CatChoice = category.getItemAtPosition(temp).toString();
                String attribute;

                attribute = new String();

                if(CatChoice.equalsIgnoreCase("Shope Name")){
                    attribute = "shopname";
                }else if(CatChoice.equalsIgnoreCase("Barber")){
                    attribute = "username";
                }else if(CatChoice.equalsIgnoreCase("City")){
                    attribute = "city";
                }else if(CatChoice.equalsIgnoreCase("Address")){
                    attribute = "address";
                }

                firebaseUserSearch(searchText,attribute);
                Toast.makeText(Search.this,"choice : " + CatChoice,Toast.LENGTH_LONG).show();
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

    private void setSpinnerAdapter() {
        ArrayAdapter spinneradapter =new ArrayAdapter(this,android.R.layout.simple_spinner_item,categoryarray);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(spinneradapter);
        category.setOnItemSelectedListener(this);
    }

    @NonNull
    private void firebaseUserSearch(String searchText,String category){

        CollectionReference sCollection =
                FirebaseFirestore.getInstance().collection("Barbers");

        Query sQuery = sCollection.orderBy(category)
                .startAt(searchText).endAt(searchText + "\uf8ff");

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