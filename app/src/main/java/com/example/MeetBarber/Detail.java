package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout DetailCEmailLayout,DetailCContactLayout,DetailCAddressLayout,DetailBEmailLayout,DetailBContactLayout,DetailBAddressLayout;

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

        DetailCEmailLayout = findViewById(R.id.DetailCEmailLayout);
        DetailCContactLayout = findViewById(R.id.DetailCContactLayout);
        DetailCAddressLayout = findViewById(R.id.DetailCAddresslayout);
        DetailBEmailLayout = findViewById(R.id.DetailBEmailLayout);
        DetailBContactLayout = findViewById(R.id.DetailBContactLayout);
        DetailBAddressLayout = findViewById(R.id.DetailBAddressLayout);
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

        DetailCContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = "+60"+customercontact.getText().toString();
                String text = "Hello " + customername.getText().toString();

                boolean installed = isAppInstalled("com.whatsapp");

                if(installed){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+num+"&text="+text));
                    startActivity(intent);
                }else{
                    Toast.makeText(Detail.this,"Whatsapp is not installed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        DetailBContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = "+60"+barbercontact.getText().toString();
                String text = "Hello " + barbershopname.getText().toString();

                boolean installed = isAppInstalled("com.whatsapp");

                if(installed){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+num+"&text="+text));
                    startActivity(intent);
                }else{
                    Toast.makeText(Detail.this,"Whatsapp is not installed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        DetailCEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" +  customeremail.getText().toString()));
                intent.putExtra(Intent.EXTRA_TEXT, "Hello "+ customername.getText().toString());
                startActivity(intent);
            }
        });

        DetailBEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" +  barberemail.getText().toString()));
                intent.putExtra(Intent.EXTRA_TEXT, "Hello "+ barbershopname.getText().toString());
                startActivity(intent);
            }
        });

        DetailBAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = barberaddress.getText().toString();

                Intent a = new Intent(Intent.ACTION_VIEW);
                a.setData(Uri.parse("geo:0,0?q="+address));
                Intent chooser = Intent.createChooser(a,"Launch Map");
                startActivity(chooser);
            }
        });

        DetailCAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = customeraddress.getText().toString();

                Intent a = new Intent(Intent.ACTION_VIEW);
                a.setData(Uri.parse("geo:0,0?q="+address));
                Intent chooser = Intent.createChooser(a,"Launch Map");
                startActivity(chooser);
            }
        });
    }

    private boolean isAppInstalled(String s) {
        PackageManager packageManager = getPackageManager();
        boolean is_installed;

        try {
            packageManager.getPackageInfo(s,PackageManager.GET_ACTIVITIES);
            is_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            is_installed = false;
            e.printStackTrace();
        }

        return is_installed;
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