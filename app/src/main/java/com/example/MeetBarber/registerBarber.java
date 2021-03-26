package com.example.MeetBarber;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class registerBarber extends AppCompatActivity {

    private EditText Rusername,Rshopename,Remail,Rpwd,RCpwd,Raddress, Rphone, Rpostcode, Rcity;
    private TextView EmailV, PwdV,CPwdV,PageTitle;
    private Button SignUp,EditProfileB;
    private ProgressBar PBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String UserId, role,dwnldUri;
    private int ImageCode = 2;
    private ImageView profile_image;
    StorageReference storageRef;
    Uri ImageUri;
    Boolean clickable = false;
    public static String imguri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.registerbarber);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        profile_image = (ImageView)findViewById(R.id.profile_imageBarber);
        Rusername = (EditText)findViewById(R.id.userNameB);
        Rshopename = (EditText)findViewById(R.id.ShopNameB);
        Remail =  (EditText)findViewById(R.id.emailB);
        Rpwd = (EditText)findViewById(R.id.pwdB);
        RCpwd = (EditText)findViewById(R.id.cpwdB);
        Raddress = (EditText)findViewById(R.id.addressB);
        Rphone = (EditText)findViewById(R.id.phoneB);
        Rpostcode = (EditText)findViewById(R.id.postcodeB);
        Rcity = (EditText)findViewById(R.id.cityB);
        SignUp = (Button)findViewById(R.id.signUpB);
        PBar = (ProgressBar)findViewById(R.id.progressBarB);
        EmailV = findViewById(R.id.EmailV);
        CPwdV = findViewById(R.id.CPwdV2);
        PwdV = findViewById(R.id.PwdV);
        PageTitle =findViewById(R.id.PageTitle);
        EditProfileB = findViewById(R.id.EditProfileB);

        EditProfileB.setVisibility(View.GONE);

        if(mAuth.getCurrentUser()!=null){

            Remail.setVisibility(View.GONE);
            Rpwd.setVisibility(View.GONE);
            RCpwd.setVisibility(View.GONE);
            EmailV.setVisibility(View.GONE);
            CPwdV.setVisibility(View.GONE);
            PwdV.setVisibility(View.GONE);
            PageTitle.setText("Edit");

            EditProfileB.setVisibility(View.VISIBLE);
            SignUp.setVisibility(View.GONE);
            UserId = mAuth.getCurrentUser().getUid();

            getProfile(UserId);
            getUserPic(UserId);

            EditProfileB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpdateProfile(UserId);

                    if(clickable == true){
                        updateimgtofirebase(ImageUri);
                        Toast.makeText(registerBarber.this,"uploaded",Toast.LENGTH_SHORT).show();
                    }else{
                        UpdateProfile(UserId);
                    }
                }
            });
        }


        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int phone;
                String email = Remail.getText().toString().trim();
                String pwd = Rpwd.getText().toString().trim();
                String Cpwd = RCpwd.getText().toString().trim();
                final String username = Rusername.getText().toString().trim();
                final String Shopename = Rshopename.getText().toString().trim();
                final String address = Raddress.getText().toString().trim();
                phone = Integer.parseInt(Rphone.getText().toString().trim());
                final String postcode = Rpostcode.getText().toString().trim();
                final String city = Rcity.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Remail.setError("Email is empty");
                    return;
                }
                if (TextUtils.isEmpty(pwd)){
                    Rpwd.setError("Password is empty");
                    return;
                }
                if(!Cpwd.equals(pwd)){
                    RCpwd.setError("Confirm password is incorrect");
                }
                if(pwd.length()<6){
                    Rpwd.setError("Password length must be more than 6 characters");
                    return;
                }
                if(TextUtils.isEmpty(username)){
                    Rusername.setError("Please insert your name");
                    return;
                }
                if(TextUtils.isEmpty(address)){
                    Raddress.setError("Address is empty");
                    return;
                }
                if(Rphone.getText().toString().trim().isEmpty()){
                    Rphone.setError("Phone number is empty");
                    return;
                }
                if(TextUtils.isEmpty(postcode)){
                    Rpostcode.setError("Postcode is empty");
                    return;
                }
                if(TextUtils.isEmpty(city)){
                    Rcity.setError("City is empty");
                    return;
                }if(clickable == false){
                    Toast.makeText(registerBarber.this," please choose choose your profile image ", Toast.LENGTH_LONG).show();
                    return;
                }

               PBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            if(clickable == true){

                                Toast.makeText(registerBarber.this,"User created succesfully",Toast.LENGTH_SHORT).show();

                                UserId = mAuth.getCurrentUser().getUid();
                                role = "Barber";

                                DocumentReference documentReference = db.collection("Barbers").document(UserId);

                                uploadimgtofirebase(ImageUri);

                                Map<String, Object> user = new HashMap<>();
                                user.put("username",username );
                                user.put("shopname",Shopename);
                                user.put("phone",phone);
                                user.put("address", address);
                                user.put("postcode",postcode);
                                user.put("city",city);
                                user.put("role",role);
                                user.put("email",email);
                                user.put("rating","unrated");

                                int h = Integer.parseInt(Rphone.getText().toString());
                                int j = Integer.parseInt(Rpostcode.getText().toString());

                                Map<String , Object > upphone = new HashMap<>();
                                upphone.put("phone", h);
                                upphone.put("postcode", j);

                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        documentReference.update(upphone);
                                        Toast.makeText(registerBarber.this,"User's data stored succesfully",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(registerBarber.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Intent i = new Intent(registerBarber.this, registerServices.class);
                                i.putExtra("EDIT","FALSE");
                                startActivity(i);

                            }else{
                                Toast.makeText(registerBarber.this," please choose choose your profile image ", Toast.LENGTH_LONG).show();
                            }

                        }else{

                            Toast.makeText(registerBarber.this,"Error " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickable = true;
                Intent OGintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(OGintent,ImageCode);
            }
        });


    }

    private void getUserPic(String id) {

        DocumentReference ref = db.collection("UriCollection").document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if(!document.get("uri").toString().equals("n/a")){
                                    Uri temp = Uri.parse(document.get("uri").toString());
                                    Picasso.get().load(temp).into(profile_image);
                                }else{
                                    profile_image.setImageResource(R.drawable.profileicon);
                                    Toast.makeText(registerBarber.this,"failed to load image",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                profile_image.setImageResource(R.drawable.profileicon);
                                Toast.makeText(registerBarber.this,"failed to load image",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            profile_image.setImageResource(R.drawable.profileicon);
                            Toast.makeText(registerBarber.this,"failed to load image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void UpdateProfile(String id) {

        String name,shopname,address,city;
        int phone,postcode;

        name = Rusername.getText().toString();
        shopname = Rshopename.getText().toString();
        address = Raddress.getText().toString();
        city = Rcity.getText().toString();
        phone = Integer.parseInt(Rphone.getText().toString());
        postcode = Integer.parseInt(Rpostcode.getText().toString());

        Map<String, Object> profile = new HashMap<>();
        profile.put("username",name );
        profile.put("shopname",shopname);
        profile.put("phone",phone);
        profile.put("address", address);
        profile.put("postcode",postcode);
        profile.put("city",city);

        DocumentReference ref = db.collection("Barbers").document(id);
        ref.update(profile);

        Intent i = new Intent(registerBarber.this, registerServices.class);
        i.putExtra("EDIT","TRUE");
        startActivity(i);
    }

    private void getProfile(String id) {

        DocumentReference ref = db.collection("Barbers").document(id);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                             Rusername.setText(document.getString("username"));

                             Rshopename.setText(document.getString("shopname"));

                             Rphone.setText(document.get("phone").toString());

                             Raddress.setText(document.getString("address"));

                             Rpostcode.setText(document.get("postcode").toString());

                             Rcity.setText(document.getString("city"));

                            } else {
                                Toast.makeText(registerBarber.this,"Failed to retrive user's profile",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(registerBarber.this,"User does not exist",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                ImageUri = data.getData();
                profile_image.setImageURI(ImageUri);
            }
        }
    }

    private void updateimgtofirebase(Uri ImageUri){

        final StorageReference fileRef = storageRef.child("Users/"+mAuth.getCurrentUser().getUid() +"/Profile.jpg");

        fileRef.delete();

        fileRef.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dwnldUri = uri.toString();
                        DocumentReference imagref = db.collection("UriCollection").document(UserId);
                        Map<String, Object> picuri = new HashMap<>();
                        picuri.put("uri",dwnldUri);
                        imagref.set(picuri);

                        Map<String, Object> user = new HashMap<>();
                        user.put("piclink",dwnldUri );

                        DocumentReference documentReference = db.collection("Barbers").document(UserId);
                        documentReference.update(user);
                    }
                });
                Toast.makeText(registerBarber.this,"Profile image uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(registerBarber.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadimgtofirebase(Uri ImageUri){
        final StorageReference fileRef = storageRef.child("Users/"+mAuth.getCurrentUser().getUid() +"/Profile.jpg");
        fileRef.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dwnldUri = uri.toString();
                        imguri = dwnldUri;
                        DocumentReference imagref = db.collection("UriCollection").document(UserId);
                        Map<String, Object> picuri = new HashMap<>();
                        picuri.put("uri",dwnldUri);
                        imagref.set(picuri);

                        Map<String, Object> user = new HashMap<>();
                        user.put("piclink",dwnldUri );

                        DocumentReference documentReference = db.collection("Barbers").document(UserId);
                        documentReference.update(user);
                    }
                });
                Toast.makeText(registerBarber.this,"Profile image uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(registerBarber.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
}
