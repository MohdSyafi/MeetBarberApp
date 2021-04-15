package com.example.MeetBarber;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HSRAdapter extends RecyclerView.Adapter<HSRAdapter.MyViewHolder> {

    private ArrayList<HSservice> HSserviceList;
    private Button addB,cancelB;
    public Context Mcontext;
    private Dialog dialog;
    private EditText serviceEdit,priceEdit;
    Profile profile;
    Booking booking;
    int from;
    private BookClickInterface bookClickInterface;
    private  Button bookButton;
    private String language;

    public HSRAdapter(Profile profile, ArrayList<HSservice> serviceList,int from){
        this.from = from;
        this.profile= profile;
        this.HSserviceList = serviceList;
    }

    public HSRAdapter(Booking booking, ArrayList<HSservice> serviceList, int from,BookClickInterface bookClickInterface,String language){
        this.bookClickInterface = bookClickInterface;
        this.from = from;
        this.booking= booking;
        this.HSserviceList = serviceList;
        this.language = language;
    }

    public HSRAdapter(Context mcontext) {
        this.Mcontext = mcontext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView priceTxt,serviceTxt;

        public MyViewHolder(final View view) {
            super(view);
            priceTxt = view.findViewById(R.id.priceTxt);
            serviceTxt = view.findViewById(R.id.serviceTxt);
        }

    }

    @NonNull
    @Override
    public HSRAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items,parent,false);
        itemView.setBackground(null);

        return new HSRAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HSRAdapter.MyViewHolder holder, final int position) {
        if(from == 0 ){

            if(language.equalsIgnoreCase("ms")){

                bookButton = holder.itemView.findViewById(R.id.bookButton);
                bookButton.setText("Tempah");
            }else{
                bookButton = holder.itemView.findViewById(R.id.bookButton);
                bookButton.setText("Book");
            }

            final String name = HSserviceList.get(position).getServicename();
            final String price = HSserviceList.get(position).getPrice();

            holder.serviceTxt.setText(name);
            holder.priceTxt.setText(price);
            holder.itemView.findViewById(R.id.deleteButton).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.editButton).setVisibility(View.GONE);
            bookButton = holder.itemView.findViewById(R.id.bookButton);

            bookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bookClickInterface.onHomeClick(position);
                }
            });

        }else if (from == 1){

            final String name = HSserviceList.get(position).getServicename();
            final String price = HSserviceList.get(position).getPrice();

            holder.serviceTxt.setText(name);
            holder.priceTxt.setText(price);
            holder.itemView.findViewById(R.id.deleteButton).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.editButton).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.bookButton).setVisibility(View.GONE);

        }

    }

    @Override
    public int getItemCount() {
        return  HSserviceList.size();
    }
}
