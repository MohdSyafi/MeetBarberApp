package com.example.MeetBarber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Review extends AppCompatActivity {

  private String barberID, customerID,appointmentID,rating,DocDate;
  private TextView ReviewShopName;
  private RatingBar ratingBar;
  private EditText comment;
  private Button ReviewSubmitButton;
  private CircleImageView ReviewProfilePic;
  private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        getSupportActionBar().hide();

        barberID = getIntent().getStringExtra("DETAILBARBERID");
        customerID = getIntent().getStringExtra("DETAILCUSTOMERID");
        appointmentID = getIntent().getStringExtra("DETAILDOCUNENTID");
        DocDate = getIntent().getStringExtra("DETAIILDOCUMENTDATE");

        ReviewShopName = findViewById(R.id.ReviewShopName);
        ratingBar = findViewById(R.id.ratingBar);
        comment = findViewById(R.id.ReviewComment);
        ReviewSubmitButton = findViewById(R.id.ReviewSubmitbutton);
        ReviewProfilePic = findViewById(R.id.ReviewprofilePic);

        getBarberProfile(barberID);
        getBarberProfilePic(barberID);

        ReviewSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                float i  = ratingBar.getRating();
                rating = String.valueOf(i);

                Map<String, Object> ReviewDetails = new HashMap<>();

                ReviewDetails.put("BarberID",barberID);
                ReviewDetails.put("CustomerID",customerID);
                ReviewDetails.put("appointmentID",appointmentID);
                ReviewDetails.put("rating",rating);
                ReviewDetails.put("comment",comment.getText().toString());
                ReviewDetails.put("date",DocDate);

                DocumentReference ref = db.collection("ReviewCollection").document(barberID)
                        .collection("Reviews").document(appointmentID);
                ref.set(ReviewDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String temp = DocDate.replaceAll("/" ,"");

                        Map<String, Object> updateReviewStatus = new HashMap<>();
                        updateReviewStatus.put("review","yes");

                        db.collection("HistoryColl").document(barberID)
                                .collection("Date").document(temp)
                                .collection("appointmentsID").document(appointmentID)
                                .update(updateReviewStatus);

                        db.collection("HistoryColl").document(customerID)
                                .collection("Date").document(temp)
                                .collection("appointmentsID").document(appointmentID)
                                .update(updateReviewStatus);

                        db.collection("Barbers").document(barberID).get()
                                 .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                DocumentSnapshot doc = task.getResult();

                                if(doc.getString("rating").equalsIgnoreCase("unrated")){

                                    double j = ratingBar.getRating();

                                    rating = String.valueOf(roundToHalf(j));

                                    Map<String, Object> updateRating = new HashMap<>();
                                    updateRating.put("rating",rating);

                                    db.collection("Barbers").document(barberID)
                                            .update(updateRating);

                                }else{

                                    double i = Double.parseDouble(doc.getString("rating"));
                                    double j = ratingBar.getRating();
                                    double k = (i + j)/2;

                                    rating = String.valueOf(roundToHalf(k));

                                    Map<String, Object> updateRating = new HashMap<>();
                                    updateRating.put("rating",rating);

                                    db.collection("Barbers").document(barberID)
                                            .update(updateRating);
                                }
                            }
                        });


                        Intent a = new Intent(Review.this,HomePage.class);
                        a .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        finishAffinity();
                        Review.this.startActivity(a);
                    }
                });
            }
        });
    }

    public static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    private void getBarberProfilePic(String barberID) {
        DocumentReference ref = db.collection("UriCollection").document(barberID);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (!document.get("uri").toString().equals("n/a")){
                                    Uri temp = Uri.parse(document.get("uri").toString());
                                    Picasso.get().load(temp).into(ReviewProfilePic);

                                }else{
                                    ReviewProfilePic.setImageResource(R.drawable.profileicon);
                                    Toast.makeText(Review.this,"failed to load image",Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                ReviewProfilePic.setImageResource(R.drawable.profileicon);
                                Toast.makeText(Review.this,"failed to load image",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ReviewProfilePic.setImageResource(R.drawable.profileicon);
                            Toast.makeText(Review.this,"failed to load image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getBarberProfile(String barberID) {

        DocumentReference ref = db.collection("Barbers").document(barberID);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                ReviewShopName.setText(document.getString("shopname"));
                            } else {
                                ReviewShopName.setText("not available");
                            }
                        } else {
                            ReviewShopName.setText("not available");

                        }
                    }
                });
    }

    public void ReviewBackClick(View view){
        onBackPressed();
    }
}