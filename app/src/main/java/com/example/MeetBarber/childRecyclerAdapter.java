package com.example.MeetBarber;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class childRecyclerAdapter extends RecyclerView.Adapter<childRecyclerAdapter.childViewHolder> {

    ArrayList<apnmtDetails> items;
    private StatusClickInterface statusClickInterface;
    private reviewClickInterface reviewClickInterface;
    private Button statusButton;
    private String documentid,barberID;
    String temp2,clickable;
    private Context context;
    private String lang;


    public childRecyclerAdapter(ArrayList<apnmtDetails> items, StatusClickInterface statusClickInterface, String clickable) {
        this.items = items;
        this.statusClickInterface = statusClickInterface;
        this.clickable = clickable;
    }

    public childRecyclerAdapter(ArrayList<apnmtDetails> items, reviewClickInterface reviewClickInterface, String clickable) {
        this.items = items;
        this.reviewClickInterface = reviewClickInterface;
        this.clickable = clickable;
    }

    @NonNull
    @Override
    public childViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.child_row,parent,false);
        return new childViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    @Override
    public void onBindViewHolder(@NonNull childViewHolder holder, int position) {

        holder.HPtimeTV.setText(items.get(position).getServiceTime());
        holder.HPservicepriceTV.setText(items.get(position).getServicePrice());
        holder.HPservicenameTV.setText(items.get(position).getServiceName());
        holder.HPBarbershopTV.setText(items.get(position).getBarbershop());
        holder.HPcustomernameTV.setText(items.get(position).getCustomerName());
        holder.HPStatusTV.setText(items.get(position).getServicestatus());

        String temp = items.get(position).getServiceDate();
        temp2 = temp.replaceAll("/" ,"");

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);

        lang = sh.getString("Locale.Helper.Selected.Language","");

        if(lang.equalsIgnoreCase("ms")){

            holder.TimeTitle.setText("Masa:");
            holder.CustTitle .setText("Pelanggan:");
            holder.TypeTitle.setText("Jenis:");
            holder.AccptBttn.setText("Terima");
            holder.CMPBtn.setText("Selesai");
            holder.ReviewBttn.setText("Komen");

            if(items.get(position).getServicestatus().equalsIgnoreCase("On hold")){
                holder.HPStatusTV.setText("Belum Diterima");
                holder.completeLL.setVisibility(View.GONE);
            }else if (items.get(position).getServicestatus().equalsIgnoreCase("Accepted")){
                holder.acceptLL.setVisibility(View.GONE);
                holder.HPStatusTV.setText("Diterima");
            }else if(items.get(position).getServicestatus().equalsIgnoreCase("Completed")) {
                holder.HPStatusTV.setText("Selesai");
            }else if(items.get(position).getServicestatus().equalsIgnoreCase("Cancelled")) {
                holder.HPStatusTV.setText("Dibatalkan");
            }else if(items.get(position).getServicestatus().equalsIgnoreCase("Rejected")) {
                holder.HPStatusTV.setText("Ditolak");
            }

        }

        if(clickable.equalsIgnoreCase("no")){
            holder.acceptLL.setVisibility(View.GONE);
            holder.completeLL.setVisibility(View.GONE);
            holder.cancelLL.setVisibility(View.GONE);
        }

        if(items.get(position).getServiceType().equalsIgnoreCase("Normal")){
            holder.HPtypeTV.setVisibility(View.GONE);
            holder.serviceTypeLL.setVisibility(View.GONE);
        }else{

            if(lang.equalsIgnoreCase("ms")){
                holder.HPtypeTV.setText("Servis Rumah");
            }else{
                holder.HPtypeTV.setText("Home Service");
            }
        }

        if(items.get(position).getServicestatus().equalsIgnoreCase("On hold")){
            holder.completeLL.setVisibility(View.GONE);
        }

        if (items.get(position).getServicestatus().equalsIgnoreCase("Accepted")){
            holder.acceptLL.setVisibility(View.GONE);
        }

        if(items.get(position).getCurrentusertype().equalsIgnoreCase("Users")){
            holder.acceptLL.setVisibility(View.GONE);
            holder.completeLL.setVisibility(View.GONE);
            if(lang.equalsIgnoreCase("ms")){
                holder.CancelBttn.setText("Batal");
            }
            if(items.get(position).getServicestatus().equalsIgnoreCase("Completed")){
                if(items.get(position).getReview().equalsIgnoreCase("no")){
                    holder.ReviewTV.setVisibility(View.VISIBLE);
                    holder.ClickReviewLL.setVisibility(View.VISIBLE);
                }else{
                    holder.ReviewTV.setVisibility(View.GONE);
                    holder.ClickReviewLL.setVisibility(View.GONE);
                }
            }
        }else{
            if(lang.equalsIgnoreCase("ms")){
                holder.CancelBttn.setText("Batal");
            }else{
                holder.HPCancelTextTV.setText("Reject");

            }
            holder.ReviewTV.setVisibility(View.GONE);
            holder.ClickReviewLL.setVisibility(View.GONE);
        }

        Log.i("checkcancel",items.toString());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class childViewHolder extends RecyclerView.ViewHolder{

        TextView HPBarbershopTV,HPservicenameTV,HPservicepriceTV,
                HPtimeTV,HPstatusTV,HPcustomernameTV,HPtypeTV,ReviewTV,HPStatusTV,HPCancelTextTV;
        TextView CMPBtn,AccptBttn, CancelBttn, ReviewBttn;
        LinearLayout acceptLL,cancelLL,completeLL,serviceTypeLL,ClickReviewLL;
        TextView TimeTitle,CustTitle,TypeTitle;

        public childViewHolder(@NonNull View itemView) {
            super(itemView);

            CMPBtn = itemView.findViewById(R.id.HPCompleteBttn);
            AccptBttn = itemView.findViewById(R.id.HPAcceptBttn);
            ReviewBttn = itemView.findViewById(R.id.ReviewActivityButton);
            CancelBttn = itemView.findViewById(R.id.HPCancelTextTV);
            HPBarbershopTV = itemView.findViewById(R.id.HPBarberName);
            HPservicenameTV =itemView.findViewById(R.id.HPServiceName);
            HPservicepriceTV = itemView.findViewById(R.id.HPServicePrice);
            HPtimeTV = itemView.findViewById(R.id.HPtime);
            HPcustomernameTV = itemView.findViewById(R.id.HPcustomerName);
            HPtypeTV = itemView.findViewById(R.id.HPType);
            ReviewTV = itemView.findViewById(R.id.ReviewActivityButton);
            acceptLL = itemView.findViewById(R.id.AppAcceptLL);
            cancelLL = itemView.findViewById(R.id.AppCancelLL);
            completeLL = itemView.findViewById(R.id.AppCompleteLL);
            HPStatusTV = itemView.findViewById(R.id.HPStatusTV);
            HPCancelTextTV = itemView.findViewById(R.id.HPCancelTextTV);
            serviceTypeLL = itemView.findViewById(R.id.ServicetypeLL);
            ClickReviewLL = itemView.findViewById(R.id.ClickReviewLL);
            TimeTitle = itemView.findViewById(R.id.HPTimeTitle);
            CustTitle = itemView.findViewById(R.id.HPCustTitle);
            TypeTitle = itemView.findViewById(R.id.HPTypeTitle);

            ReviewTV.setVisibility(View.GONE);
            ClickReviewLL.setVisibility(View.GONE);

            if(clickable.equalsIgnoreCase("no")){

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String docid,docdate,bid,cid;
                        docid = items.get(getAdapterPosition()).getDocumentID();
                        docdate = items.get(getAdapterPosition()).getServiceDate();
                        bid = items.get(getAdapterPosition()).getBarberName();
                        cid = items.get(getAdapterPosition()).getCustomerID();
                        reviewClickInterface.onDetailClick(getAdapterPosition(),docid,docdate,bid,cid);
                    }
                });

                ReviewTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                     String docid,docdate,bid,cid;
                     docid = items.get(getAdapterPosition()).getDocumentID();
                     docdate = items.get(getAdapterPosition()).getServiceDate();
                     bid = items.get(getAdapterPosition()).getBarberName();
                     cid = items.get(getAdapterPosition()).getCustomerID();
                     barberID = items.get(getAdapterPosition()).getBarberName();
                     reviewClickInterface.onReviewClick(getAdapterPosition(),docid,docdate,bid,cid);
                    }
                });

            }else{

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String docid,docdate,bid,cid;
                        docid = items.get(getAdapterPosition()).getDocumentID();
                        docdate = items.get(getAdapterPosition()).getServiceDate();
                        bid = items.get(getAdapterPosition()).getBarberName();
                        cid = items.get(getAdapterPosition()).getCustomerID();
                        statusClickInterface.onDetailClick(getAdapterPosition(),docid,docdate,bid,cid);
                    }
                });

                cancelLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        documentid = items.get(getAdapterPosition()).getDocumentID();

                        String usertype = items.get(getAdapterPosition()).getCurrentusertype();

                        if(usertype.equalsIgnoreCase("Users")){
                            String barberID, finish;
                            finish = "no";
                            barberID = items.get(getAdapterPosition()).getBarberName();
                            statusClickInterface.onCancelClick(getAdapterPosition(),documentid,temp2,barberID,finish);
                            Log.i("checkcancel",documentid);

                        }else{
                            String customerID,finish;
                            finish = "no";
                            customerID = items.get(getAdapterPosition()).getCustomerID();
                            statusClickInterface.onCancelClick(getAdapterPosition(),documentid,temp2,customerID,finish);
                        }
                    }
                });

                completeLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        documentid = items.get(getAdapterPosition()).getDocumentID();

                        String usertype = items.get(getAdapterPosition()).getCurrentusertype();

                        String customerID,finish;
                        finish = "yes";
                        customerID = items.get(getAdapterPosition()).getCustomerID();
                        statusClickInterface.onCompleteClick(getAdapterPosition(),documentid,temp2,customerID,finish);
                    }
                });

                acceptLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        documentid = items.get(getAdapterPosition()).getDocumentID();

                        String usertype = items.get(getAdapterPosition()).getCurrentusertype();

                        String customerID,finish;
                        finish = "no";
                        customerID = items.get(getAdapterPosition()).getCustomerID();
                        statusClickInterface.onAcceptClick(getAdapterPosition(),documentid,temp2,customerID,finish);
                    }
                });

            }



        }
    }
}
