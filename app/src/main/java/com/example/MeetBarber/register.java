package com.example.MeetBarber;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class register  extends AppCompatActivity  {

    private DatabaseReference databasereference;
    private EditText Rusername,Remail,Rpwd,RCpwd,Raddress, Rphone, Rpostcode, Rcity;
    private TextView EmailV,CPwdV,PwdV,PageTitle;
    private TextView pwd_tv,cpwd_tv,name_tv, contact_tv, address_tv,postcode_tv,city_tv,pagetitle_tv;
    private Button SignUp,EditProfileB;
    private ProgressBar PBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String UserId, role, FBuri,lang;
    private int ImageCode = 2;
    ///private ImageView profile_image;
    private StorageReference storageRef;
    private CircleImageView profile_image;
    private  Uri ImageUri;
    registerBarber registerbarber;
    private Dialog updateDialog;
    private Handler mHandler;
    boolean clicked = false;
    private Resources resources;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.register);

        updateDialog = new Dialog(this);
        mHandler = new Handler();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        profile_image = findViewById(R.id.profile_image);
        Rusername = (EditText)findViewById(R.id.UserName);
        Remail =  (EditText)findViewById(R.id.email);
        Rpwd = (EditText)findViewById(R.id.pwd);
        RCpwd = (EditText)findViewById(R.id.Cpwd);
        Raddress = (EditText)findViewById(R.id.address);
        Rphone = (EditText)findViewById(R.id.phone);
        Rpostcode = (EditText)findViewById(R.id.Postcode);
        Rcity = (EditText)findViewById(R.id.city);
        SignUp = (Button)findViewById(R.id.SignUp);
        PBar = (ProgressBar)findViewById(R.id.progressBar);
        EmailV = findViewById(R.id.EmailTV);
        CPwdV = findViewById(R.id.CPwdTV);
        PwdV = findViewById(R.id.PwdTV);
        PageTitle = findViewById(R.id.TitleRU);
        EditProfileB = findViewById(R.id.EditProfileBR);
        pwd_tv = findViewById(R.id.PwdTV);
        cpwd_tv = findViewById(R.id.CPwdTV);
        name_tv = findViewById(R.id.NameTV);
        contact_tv= findViewById(R.id.PhoneTV);
        address_tv = findViewById(R.id.AddressTV);
        postcode_tv = findViewById(R.id.PostcodeTV);
        city_tv = findViewById(R.id.CityTV);
        pagetitle_tv = findViewById(R.id.TitleRU);

        EditProfileB.setVisibility(View.GONE);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            context = LocaleHelper.setLocale(register.this, "ms");
            resources =  context.getResources();
            pagetitle_tv.setText(resources.getString(R.string.page_title_register));
            pwd_tv.setText(resources.getString(R.string.title_password));
            cpwd_tv.setText(resources.getString(R.string.title_confirm_password));
            name_tv.setText(resources.getString(R.string.title_name));
            contact_tv.setText(resources.getString(R.string.title_contact));
            address_tv.setText(resources.getString(R.string.title_address));
            postcode_tv.setText(resources.getString(R.string.title_postcode));
            city_tv.setText(resources.getString(R.string.title_city));
            SignUp.setText(resources.getString(R.string.SignUp_button));
            EditProfileB.setText(resources.getString(R.string.Confirm_button));


        }else{

            context = LocaleHelper.setLocale(register.this, "en");
            resources =  context.getResources();
            pagetitle_tv.setText(resources.getString(R.string.page_title_register));
            pwd_tv.setText(resources.getString(R.string.title_password));
            cpwd_tv.setText(resources.getString(R.string.title_confirm_password));
            name_tv.setText(resources.getString(R.string.title_name));
            contact_tv.setText(resources.getString(R.string.title_contact));
            address_tv.setText(resources.getString(R.string.title_address));
            postcode_tv.setText(resources.getString(R.string.title_postcode));
            city_tv.setText(resources.getString(R.string.title_city));
            SignUp.setText(resources.getString(R.string.SignUp_button));
            EditProfileB.setText(resources.getString(R.string.Confirm_button));
        }

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

                    if(clicked){

                        updateimgtofirebase(ImageUri);

                        String name,address,city,id;
                        int phone,postcode;

                        id = mAuth.getCurrentUser().getUid();
                        name = Rusername.getText().toString();
                        address = Raddress.getText().toString();
                        city = Rcity.getText().toString();
                        phone = Integer.parseInt(Rphone.getText().toString());
                        postcode = Integer.parseInt(Rpostcode.getText().toString());

                        Map<String, Object> userdata = new HashMap<>();
                        userdata.put("username",name );
                        userdata.put("phone",phone);
                        userdata.put("address", address);
                        userdata.put("postcode",postcode);
                        userdata.put("city",city);

                        DocumentReference ref = db.collection("Users").document(id);
                        ref.update(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Intent i = new Intent(register.this, MainActivity.class);
                                startActivity(i);
                                finishAffinity();

                            }
                        });

                    }else{

                        String name,address,city,id;
                        int phone,postcode;

                        id = mAuth.getCurrentUser().getUid();
                        name = Rusername.getText().toString();
                        address = Raddress.getText().toString();
                        city = Rcity.getText().toString();
                        phone = Integer.parseInt(Rphone.getText().toString());
                        postcode = Integer.parseInt(Rpostcode.getText().toString());

                        Map<String, Object> userdata = new HashMap<>();
                        userdata.put("username",name );
                        userdata.put("phone",phone);
                        userdata.put("address", address);
                        userdata.put("postcode",postcode);
                        userdata.put("city",city);

                        DocumentReference ref = db.collection("Users").document(id);
                        ref.update(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent i = new Intent(register.this, MainActivity.class);
                                startActivity(i);
                                finishAffinity();
                            }
                        });
                    }
                }
            });
        }

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = Remail.getText().toString().trim();
                String pwd = Rpwd.getText().toString().trim();
                String Cpwd = RCpwd.getText().toString().trim();
                final String username = Rusername.getText().toString().trim();
                final String address = Raddress.getText().toString().trim();
                final String phone = Rphone.getText().toString().trim();
                final String postcode = Rpostcode.getText().toString().trim();
                final String city = Rcity.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    Remail.setError("Email is empty");
                    return;
                }else if (TextUtils.isEmpty(pwd)){
                    Rpwd.setError("Password is empty");
                    return;
                }else if(!Cpwd.equals(pwd)){
                    RCpwd.setError("Confirm password is incorrect");
                }else if(pwd.length()<6){
                    Rpwd.setError("Password length must be more than 6 characters");
                    return;
                }else if(TextUtils.isEmpty(username)){
                    Rusername.setError("Please insert your name");
                    return;
                }else if(TextUtils.isEmpty(address)){
                    Raddress.setError("Address is empty");
                    return;
                }else if(TextUtils.isEmpty(phone)){
                    Rphone.setError("Phone number is empty");
                    return;
                }else if(TextUtils.isEmpty(postcode)){
                    Rpostcode.setError("Postcode is empty");
                    return;
                }else if(TextUtils.isEmpty(city)){
                    Rcity.setError("City is empty");
                    return;
                }else if(clicked == false){
                    Toast.makeText(register.this," please choose choose your profile image ", Toast.LENGTH_LONG).show();
                    return;
                }else if(!isPwdValid(pwd)){
                    Rpwd.setError("Your password must be 8 char, with combination of uppercase, lowercase letter and digits");
                }else{
                    PBar.setVisibility(View.VISIBLE);

                    mAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                if(clicked == true) {
                                    uploadimgtofirebase(ImageUri);

                                    Toast.makeText(register.this,"User created succesfully",Toast.LENGTH_SHORT).show();

                                    UserId = mAuth.getCurrentUser().getUid();
                                    role = "User";
                                    String shopname = "n/a";
                                    DocumentReference documentReference = db.collection("Users").document(UserId);

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username",username );
                                    user.put("shopname",shopname);
                                    user.put("phone",phone);
                                    user.put("address", address);
                                    user.put("postcode",postcode);
                                    user.put("city",city);
                                    user.put("role",role);
                                    user.put("email",email);

                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(register.this,"User's data stored succesfully",Toast.LENGTH_SHORT).show();
                                            finish();
                                            Intent i = new Intent(register.this, HomePage.class);
                                            startActivity(i);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(register.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }else{
                                    PBar.setVisibility(View.GONE);
                                    Toast.makeText(register.this," please choose choose your profile image ", Toast.LENGTH_LONG).show();
                                }

                            }else{
                                PBar.setVisibility(View.GONE);
                                Toast.makeText(register.this,"Error " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    PBar.setVisibility(View.GONE);
                }


            }

        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked =true;
                Intent OGintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(OGintent,ImageCode);

            }
        });
    }

    private boolean isPwdValid(String pwd) {

        boolean status = false;
        char [] array = pwd.toCharArray();
        int lower=0, upper=0, digits=0;

        if (pwd.length() > 7)
            status = true;

        for ( int i = 0;  i < array.length; i++) {
            if(Character.isDigit(array[i]))
                digits++;
            if(Character.isLowerCase(array[i]))
                lower++;
            if(Character.isUpperCase(array[i]))
                upper++;
        }

        if ( !(lower  > 0 ))
            status = false;

        if ( !(upper  > 0 ))
            status = false;

        if ( !(digits > 0 ))
            status = false;

        return status;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageCode){
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
                        FBuri = uri.toString();
                        DocumentReference imagref = db.collection("UriCollection").document(UserId);
                        Map<String, Object> picuri = new HashMap<>();
                        picuri.put("uri",FBuri);
                        imagref.set(picuri);

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(register.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void UpdateProfile(String id) {

        String name,address,city;
        int phone,postcode;

        name = Rusername.getText().toString();
        address = Raddress.getText().toString();
        city = Rcity.getText().toString();
        phone = Integer.parseInt(Rphone.getText().toString());
        postcode = Integer.parseInt(Rpostcode.getText().toString());

        Map<String, Object> userdata = new HashMap<>();
        userdata.put("username",name );
        userdata.put("phone",phone);
        userdata.put("address", address);
        userdata.put("postcode",postcode);
        userdata.put("city",city);

        DocumentReference ref = db.collection("Users").document(id);
        ref.update(userdata);


    }

    private void getProfile(String id) {

        DocumentReference ref = db.collection("Users").document(id);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                Rusername.setText(document.getString("username"));

                                Rphone.setText(document.get("phone").toString());

                                Raddress.setText(document.getString("address"));

                                Rpostcode.setText(document.get("postcode").toString());

                                Rcity.setText(document.getString("city"));

                            } else {
                                Toast.makeText(register.this,"Failed to retrive user's profile",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(register.this,"User does not exist",Toast.LENGTH_LONG).show();
                        }
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
                                    ImageUri = temp;
                                }else{
                                    profile_image.setImageResource(R.drawable.profileicon);
                                    Toast.makeText(register.this,"failed to load image",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                profile_image.setImageResource(R.drawable.profileicon);
                                Toast.makeText(register.this,"failed to load image",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            profile_image.setImageResource(R.drawable.profileicon);
                            Toast.makeText(register.this,"failed to load image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void uploadimgtofirebase(Uri ImageUri){
        final StorageReference fileRef = storageRef.child("Users/"+mAuth.getCurrentUser().getUid() +"/Profile.jpg");
        fileRef.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(register.this,"Profile image uploaded",Toast.LENGTH_SHORT).show();

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FBuri = uri.toString();
                        DocumentReference imagref = db.collection("UriCollection").document(UserId);
                        Map<String, Object> picuri = new HashMap<>();
                        picuri.put("uri",FBuri);
                        imagref.set(picuri);

                        Map<String, Object> user = new HashMap<>();
                        user.put("piclink",FBuri );

                        DocumentReference documentReference = db.collection("Users").document(UserId);
                        documentReference.update(user);
                    }
                });
                Toast.makeText(register.this,"Profile image uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(register.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

}
