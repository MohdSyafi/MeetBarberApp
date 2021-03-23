package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class Detail extends AppCompatActivity {

    private ImageView DetailBackButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String DocID, DocDate, BarberID, CustomerID, UserId, userType, collection;
    private TextView serviceName, servicePrice, serviceDate, serviceStatus, serviceType, serviceTime;
    private  TextView barbershopname, barberemail, barbercontact, barberaddress;
    private  TextView customername, customeremail,customercontact, customeraddress;
    private CircleImageView barberprofileImage, customerprofileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().hide();

        DetailBackButton = findViewById(R.id.DetailBackBttn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        UserId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        Bundle details = intent.getExtras();
        DocID = details.getString("DETAILDOCUNENTID");
        DocDate = details.getString("DETAIILDOCUMENTDATE");
        BarberID = details.getString("DETAILBARBERID");
        CustomerID = details.getString("DETAILCUSTOMERID");
        collection = details.getString("DETAILCOLLECTION");

        serviceName = findViewById(R.id.DetailServiceName);
        serviceDate = findViewById(R.id.detailServiceDate);
        servicePrice = findViewById(R.id.detailServicePrice);
        serviceStatus = findViewById(R.id.detailServiceStatus);
        serviceTime = findViewById(R.id.detailServiceTime);
        serviceType = findViewById(R.id.detailServiceType);
        customerprofileImage = findViewById(R.id.DetailCustomerPic);
        barberprofileImage = findViewById(R.id.DetailBarberPic);
        barbershopname = findViewById(R.id.detailBarberName);
        barberemail = findViewById(R.id.detailBarberEmail);
        barbercontact = findViewById(R.id.detailBarberPhone);
        barberaddress = findViewById(R.id.detailBarberAddress);
        customername = findViewById(R.id.detailCustomerName);
        customeremail = findViewById(R.id.detailCustomerEmail);
        customercontact = findViewById(R.id.detailCustomerPhone);
        customeraddress = findViewById(R.id.detailCustomerAddress);

        getServiceDetail();
        getBarberDetail();
        getCustomerDetail();

        DetailBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void getCustomerDetail() {
        String key  = "username";
        String coll = "Users";
        getprofileImage(CustomerID,key,coll,customerprofileImage);
        getprofileDetail(CustomerID,key,coll,customername, customeremail, customercontact, customeraddress);
    }

    private void getBarberDetail() {
        String key  = "shopname";
        String coll = "Barbers";
        getprofileImage(BarberID,key,coll,barberprofileImage);
        getprofileDetail(BarberID,key,coll,barbershopname, barberemail, barbercontact,barberaddress);
    }

    private void getprofileDetail(String id, String key, String coll, TextView name,TextView email, TextView contact, TextView address) {
        DocumentReference docRef = db.collection(coll).document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                name.setText(documentSnapshot.getString(key));
                email.setText(documentSnapshot.getString("email"));
                contact.setText(documentSnapshot.get("phone").toString());
                String Fulladdress =documentSnapshot.getString("address") + ", "
                        + documentSnapshot.get("postcode").toString() + ", "
                        + documentSnapshot.getString("city");
                address.setText(Fulladdress);
            }
        });
    }

    private void getprofileImage(String id, String key, String coll, CircleImageView imageView) {

        DocumentReference docRef = db.collection("UriCollection").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Uri temp = Uri.parse(documentSnapshot.get("uri").toString());
                Picasso.get().load(temp).into(imageView);
            }
        });
    }

    private void getServiceDetail() {

        String temp = DocDate.replaceAll("/" ,"");

        DocumentReference docref =  db.collection(collection).document(UserId)
                .collection("Date").document(temp)
                .collection("appointmentsID").document(DocID);

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            serviceName.setText(documentSnapshot.get("name").toString());
                            serviceDate.setText(documentSnapshot.get("date").toString());
                            servicePrice.setText("RM" + documentSnapshot.get("price").toString());
                            serviceStatus.setText(documentSnapshot.get("status").toString());
                            serviceTime.setText(documentSnapshot.get("time slot").toString());
                            serviceType.setText(documentSnapshot.get("type").toString());

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error : " + e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }
}