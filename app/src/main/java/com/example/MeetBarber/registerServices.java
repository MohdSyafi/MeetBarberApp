package com.example.MeetBarber;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class registerServices extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ArrayList<services> serviceList;
    private ArrayList<HSservice> HSserviceList;
    private String[] durationsarray;
    private RecyclerView recyclerView,HSrecyclerview;
    private Button HSAddservBttn,AddservBttn, ConfirmButton;
    private Dialog dialog;
    private EditText serviceEdit,priceEdit,startTimeET,endTimeET;
    private FirebaseFirestore db ;
    private DocumentReference documentReference;
    private FirebaseAuth mAuth;
    private String UserId,StartTimeS,EndTimeS,OperationHours,choice;
    public String spinnerposition;
    private  String edit = "FALSE";
    private Spinner durations;
    private int TPDhour,TPDminute;
    public static String currentCont = null;
    private TextView header1,header2,header3,header4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.registerservices);

        Intent intent = getIntent();
        edit = intent.getExtras().getString("EDIT");

        durationsarray = new String[]{"30 minutes","45 minutes","1 hour","1 hour 30 minutes","2 hour"};

        HSserviceList = new ArrayList<>();
        serviceList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        UserId = mAuth.getCurrentUser().getUid();

        StartTimeS =new String();
        EndTimeS = new String();
        OperationHours = new String();
        choice = new String();

        HSrecyclerview =findViewById(R.id.HScontainer);
        recyclerView = findViewById(R.id.container);
        AddservBttn = findViewById(R.id.addbutton);
        HSAddservBttn = findViewById(R.id.addbuttonHS);
        durations = findViewById(R.id.durationSpinner);
        startTimeET = findViewById(R.id.startTimeET);
        endTimeET = findViewById(R.id.endTimeET);
        ConfirmButton = findViewById(R.id.ConfirmButton);
        header1 = findViewById(R.id.BOheader);
        header2 = findViewById(R.id.BOheader2);
        header3 = findViewById(R.id.BOheader3);
        header4 = findViewById(R.id.BOheader4);

        startTimeET.setInputType(InputType.TYPE_NULL);
        endTimeET.setInputType(InputType.TYPE_NULL);

        if(edit.equalsIgnoreCase("TRUE")){
            startTimeET.setVisibility(View.GONE);
            endTimeET.setVisibility(View.GONE);
            durations.setVisibility(View.GONE);
            header1.setVisibility(View.GONE);
            header2.setVisibility(View.GONE);
            header3.setVisibility(View.GONE);
            header4.setVisibility(View.GONE);

        }else{
            startTimeET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openTimePickerDialog(startTimeET);

                }
            });

            endTimeET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openTimePickerDialog(endTimeET);

                }
            });
        }

        ConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTimeS  =  startTimeET.getText().toString();
                EndTimeS = endTimeET.getText().toString();
                long difference_In_Minutes = 0L;
                long difference_In_Hours =0L;
                long difference_In_Time=0L;
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");


                try {
                    Date d1 = sdf.parse(StartTimeS);
                    Date d2 = sdf.parse(EndTimeS);

                    difference_In_Time
                            = d2.getTime() - d1.getTime();

                     difference_In_Minutes
                            = (difference_In_Time
                            / (1000 * 60))
                            % 60;

                    difference_In_Hours
                            = (difference_In_Time
                            / (1000 * 60 * 60))
                            % 24;

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(difference_In_Hours<=0){

                    endTimeET.setError("Invalid Operation Hours");
                    startTimeET.setError("Invalid Operation Hours");

                }else{
                    endTimeET.setError(null);
                    startTimeET.setError(null);

                    int temp = durations.getSelectedItemPosition();
                    spinnerposition = String.valueOf(temp);
                    choice = durations.getItemAtPosition(temp).toString();

                    Map<String, Object> BussOperations = new HashMap<>();
                    BussOperations.put("spinnerposition",spinnerposition);
                    BussOperations.put("Duration",choice);
                    BussOperations.put("StartHours",StartTimeS);
                    BussOperations.put("EndHours",EndTimeS);

                    DocumentReference ref = db.collection("OperationDetailsCollection").document(UserId);
                    ref.set(BussOperations);

                    Intent i = new Intent(registerServices.this, HomePage.class);
                    startActivity(i);
                }


            }
        });

        AddservBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(registerServices.this);
                dialog.setContentView(R.layout.popup);
                dialog.show();
                serviceEdit = dialog.findViewById(R.id.servicename);
                priceEdit = dialog.findViewById(R.id.serviceprice);

                dialog.findViewById(R.id.saveBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String name =serviceEdit.getText().toString().trim();
                        String price = priceEdit.getText().toString().trim();

                        String serviceid;

                        documentReference = db.collection("servicesCollection").document(UserId)
                                .collection("services").document();
                        serviceid = documentReference.getId();

                        serviceList.add(new services(serviceid,name,price));

                        Map<String, Object> services = new HashMap<>();
                        services.put("name",name );
                        services.put("price",price);

                        db.collection("servicesCollection").document(UserId).collection("services")
                                .add(services)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        ///.makeText(registerServices.this,"data added "+ documentReference.getId(),Toast.LENGTH_SHORT);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(registerServices.this,"fail to add data ",Toast.LENGTH_SHORT);
                                    }
                                });
                        getFirestoredata();
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancelBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                Window window = dialog.getWindow();
                window.setLayout(Constraints.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            }
        });

        HSAddservBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(registerServices.this);
                dialog.setContentView(R.layout.popup);
                dialog.show();
                serviceEdit = dialog.findViewById(R.id.servicename);
                priceEdit = dialog.findViewById(R.id.serviceprice);

                dialog.findViewById(R.id.saveBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String name =serviceEdit.getText().toString().trim();
                        String price = priceEdit.getText().toString().trim();

                        String serviceid;

                        documentReference = db.collection("servicesCollection").document(UserId)
                                .collection("Homeservices").document();
                        serviceid = documentReference.getId();

                        HSserviceList.add(new HSservice(serviceid,name,price));

                        Map<String, Object> services = new HashMap<>();
                        services.put("name",name );
                        services.put("price",price);

                        db.collection("servicesCollection").document(UserId).collection("Homeservices")
                                .add(services)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(registerServices.this,"data added "+ documentReference.getId(),Toast.LENGTH_SHORT);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(registerServices.this,"fail to add data ",Toast.LENGTH_SHORT);
                                    }
                                });
                        getHSFirestoredata();
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancelBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                Window window = dialog.getWindow();
                window.setLayout(Constraints.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            }
        });
        setAdapter();
        setHSAdapter();
        getFirestoredata();
        getHSFirestoredata();
        getSpinnerValue();
        setSpinnerAdapter();
        getdurationdata();
        setcurrentTime();
    }



    private void getdurationdata() {
        DocumentReference ref = db.collection("OperationDetailsCollection").document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {

                                setdurationview( document.getString("StartHours"), document.getString("EndHours"));
                            } else {
                                setcurrentTime();
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                            setcurrentTime();
                        }
                    }
                });
    }

    private void setdurationview(String ST,String ET) {
        if(ST!=null||ET!=null){
            startTimeET.setText(ST);
            endTimeET.setText(ET);
        }else{
            setcurrentTime();
        }

    }

    private void setcurrentTime() {
        SimpleDateFormat sdf =  new SimpleDateFormat("hh:mm");
        Date dateobj = new Date();
        startTimeET.setText(sdf.format(dateobj));
        endTimeET.setText(sdf.format(dateobj));
    }

    private void setSpinnerAdapter() {
        ArrayAdapter spinneradapter =new ArrayAdapter(this,android.R.layout.simple_spinner_item,durationsarray);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durations.setAdapter(spinneradapter);
        durations.setOnItemSelectedListener(this);
    }

    private void setspinnerposition(String temp){
        spinnerposition = temp;
        if(temp!= null)
        durations.setSelection(Integer.parseInt(temp));
        else setSpinnerAdapter();
    }

    private void getSpinnerValue() {

        DocumentReference ref = db.collection("OperationDetailsCollection").document(UserId);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                setspinnerposition(document.getString("spinnerposition"));
                               ///Toast.makeText(registerServices.this,"pos "+document.getString("spinnerposition"),Toast.LENGTH_SHORT).show();
                            } else {
                                setSpinnerAdapter();
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                            setSpinnerAdapter();
                        }
                    }
                });

    }

    private void openTimePickerDialog(final EditText TimeView) {
        final Calendar cldr = Calendar.getInstance();
        int hour = cldr.get(Calendar.HOUR_OF_DAY);
        int minutes = cldr.get(Calendar.MINUTE);

        TimePickerDialog picker = new TimePickerDialog(registerServices.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                        TPDhour = sHour;
                        TPDminute = sMinute;
                        String time = TPDhour + ":" + TPDminute;
                        SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");
                        try {
                            Date date = f24Hours.parse(time);
                            SimpleDateFormat f12Hours = new SimpleDateFormat("HH:mm");
                            TimeView.setText(f12Hours.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                },12,0,true);
                picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                picker.updateTime(TPDhour,TPDminute);
                picker.show();
    }

    private void setAdapter() {
        recyclerAdapter adapter= new recyclerAdapter(registerServices.this,serviceList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

    private void setHSAdapter() {
        HSrecyclerAdapter HSadapter= new HSrecyclerAdapter(registerServices.this,HSserviceList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        HSrecyclerview.setLayoutManager(layoutManager);
        HSrecyclerview.setItemAnimator(new DefaultItemAnimator());
        HSrecyclerview.setAdapter(HSadapter);

    }

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
                        setHSAdapter();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(registerServices.this,"error: ",Toast.LENGTH_SHORT);
            }
        });

    }

    public void getFirestoredata(){

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
                        setAdapter();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(registerServices.this,"error: ",Toast.LENGTH_SHORT);
            }
        });

    }

    public void HSdeleteSelectedRow(int position) {

        DocumentReference reference = db.collection("servicesCollection").document(UserId)
                .collection("Homeservices").document(HSserviceList.get(position).getUserId());

        String id  = reference.getId();

        db.collection("servicesCollection").document(UserId)
                .collection("Homeservices").document(id)
                .delete();
        getHSFirestoredata();
    }

    public void deleteSelectedRow(int position) {

        DocumentReference reference = db.collection("servicesCollection").document(UserId)
                .collection("services").document(serviceList.get(position).getUserId());

        String id  = reference.getId();

        db.collection("servicesCollection").document(UserId)
                .collection("services").document(id)
                .delete();
        getFirestoredata();
    }

    public void editRow(int position, Map<String, Object> services){

        DocumentReference reference = db.collection("servicesCollection").document(UserId)
                .collection("services").document(serviceList.get(position).getUserId());

        String id  = reference.getId();

        db.collection("servicesCollection").document(UserId)
                .collection("services").document(id)
                .update(services);
        getFirestoredata();
    }

    public void HSeditRow(int position, Map<String, Object> services){

        DocumentReference reference = db.collection("servicesCollection").document(UserId)
                .collection("Homeservices").document(HSserviceList.get(position).getUserId());

        String id  = reference.getId();

        db.collection("servicesCollection").document(UserId)
                .collection("Homeservices").document(id)
                .update(services);
        getHSFirestoredata();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        choice = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
