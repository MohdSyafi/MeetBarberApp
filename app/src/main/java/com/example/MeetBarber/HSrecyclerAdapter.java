package com.example.MeetBarber;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HSrecyclerAdapter extends RecyclerView.Adapter<HSrecyclerAdapter.HSViewHolder> {

    private ArrayList<HSservice> HSserviceList;
    private Button addB,cancelB;
    public Context Mcontext;
    private Dialog dialog;
    private EditText serviceEdit,priceEdit;

    DocumentReference documentReference;
    registerServices registerServices;

    public HSrecyclerAdapter(registerServices registerServices,ArrayList<HSservice> HSserviceList){
        this.registerServices = registerServices;
        this.HSserviceList = HSserviceList;

    }

    public HSrecyclerAdapter(Context mcontext) {
        this.Mcontext = mcontext;
    }

    public class HSViewHolder extends RecyclerView.ViewHolder{
        private TextView priceTxt,serviceTxt;
        private Button bookB;

        public HSViewHolder(final View view) {
            super(view);
            priceTxt = view.findViewById(R.id.priceTxt);
            serviceTxt = view.findViewById(R.id.serviceTxt);
            addB = view.findViewById(R.id.editButton);
            bookB = view.findViewById(R.id.bookButton);

        }
    }

    @NonNull
    @Override
    public HSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items,parent,false);
        return new HSViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final HSViewHolder holder, final int position) {
        final String name = HSserviceList.get(position).getServicename();
        final String price = HSserviceList.get(position).getPrice();

        holder.bookB.setVisibility(View.GONE);
        holder.serviceTxt.setText(name);
        holder.priceTxt.setText(price);

        holder.itemView.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerServices.HSdeleteSelectedRow(position);

            }
        });

        holder.itemView.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(holder,name,price,position);
            }
        });

    }

    private void openDialog(@NonNull final HSrecyclerAdapter.HSViewHolder holder, String name , String price, final int position) {

        dialog = new Dialog(holder.priceTxt.getContext());
        dialog.setContentView(R.layout.popup);
        dialog.show();
        serviceEdit = dialog.findViewById(R.id.servicename);
        priceEdit = dialog.findViewById(R.id.serviceprice);
        serviceEdit.setText(name);
        priceEdit.setText(price);

        dialog.findViewById(R.id.saveBttn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.serviceTxt.setText(serviceEdit.getText().toString().trim());
                holder.priceTxt.setText(priceEdit.getText().toString().trim());

                Map<String, Object> services = new HashMap<>();
                services.put("name",serviceEdit.getText().toString().trim());
                services.put("price",priceEdit.getText().toString().trim());

                registerServices.HSeditRow(position,services);

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

    @Override
    public int getItemCount() {
        return HSserviceList.size();
    }
}
