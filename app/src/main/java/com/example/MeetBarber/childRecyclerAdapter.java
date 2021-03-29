package com.example.MeetBarber;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    public void onBindViewHolder(@NonNull childViewHolder holder, int position) {

        holder.HPtimeTV.setText(items.get(position).getServiceTime());
        holder.HPstatusTV.setText(items.get(position).getServicestatus());
        holder.HPservicepriceTV.setText(items.get(position).getServicePrice());
        holder.HPservicenameTV.setText(items.get(position).getServiceName());
        holder.HPBarbershopTV.setText(items.get(position).getBarbershop());
        holder.HPcustomernameTV.setText(items.get(position).getCustomerName());

        String temp = items.get(position).getServiceDate();
        temp2 = temp.replaceAll("/" ,"");


        if(items.get(position).getServiceType().equalsIgnoreCase("Normal")){
            holder.HPtypeTV.setVisibility(View.GONE);
        }

        if(items.get(position).getServicestatus().equalsIgnoreCase("On hold")){
            holder.HPstatusTV.setTextColor(Color.parseColor("#FFEB3B"));
        }else if (items.get(position).getServicestatus().equalsIgnoreCase("Accepted")){
            holder.HPstatusTV.setTextColor(Color.parseColor("#4CAF50"));
        }

        if(items.get(position).getCurrentusertype().equalsIgnoreCase("Users")){
            if(items.get(position).getServicestatus().equalsIgnoreCase("Completed")){
                if(items.get(position).getReview().equalsIgnoreCase("no")){
                    holder.ReviewTV.setVisibility(View.VISIBLE);
                }else{
                    holder.ReviewTV.setVisibility(View.GONE);
                }
            }
        }else{
            holder.ReviewTV.setVisibility(View.GONE);
        }

        Log.i("checkcancel",items.toString());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class childViewHolder extends RecyclerView.ViewHolder{

        TextView HPBarbershopTV,HPservicenameTV,HPservicepriceTV,HPtimeTV,HPstatusTV,HPcustomernameTV,HPtypeTV,ReviewTV;


        public childViewHolder(@NonNull View itemView) {
            super(itemView);

            HPBarbershopTV = itemView.findViewById(R.id.HPBarberName);
            HPservicenameTV =itemView.findViewById(R.id.HPServiceName);
            HPservicepriceTV = itemView.findViewById(R.id.HPServicePrice);
            HPstatusTV = itemView.findViewById(R.id.HPstatusbutton);
            HPtimeTV = itemView.findViewById(R.id.HPtime);
            HPcustomernameTV = itemView.findViewById(R.id.HPcustomerName);
            HPtypeTV = itemView.findViewById(R.id.HPType);
            ReviewTV = itemView.findViewById(R.id.ReviewActivityButton);

            ReviewTV.setVisibility(View.GONE);



            if(clickable.equalsIgnoreCase("no")){

                HPstatusTV.setClickable(false);

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

                HPstatusTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        documentid = items.get(getAdapterPosition()).getDocumentID();

                        String usertype = items.get(getAdapterPosition()).getCurrentusertype();

                        if(usertype.equalsIgnoreCase("Users")){
                            String barberID, finish;
                            finish = "no";
                            barberID = items.get(getAdapterPosition()).getBarberName();
                            statusClickInterface.onStatusClick(getAdapterPosition(),documentid,temp2,barberID,finish);
                            Log.i("checkcancel",documentid);

                        }else{

                            if (items.get(getAdapterPosition()).getServicestatus().equalsIgnoreCase("Accepted")){
                                String customerID,finish;
                                finish = "yes";
                                customerID = items.get(getAdapterPosition()).getCustomerID();
                                statusClickInterface.onStatusClick(getAdapterPosition(),documentid,temp2,customerID,finish);
                            }else{
                                String customerID,finish;
                                finish = "no";
                                customerID = items.get(getAdapterPosition()).getCustomerID();
                                statusClickInterface.onStatusClick(getAdapterPosition(),documentid,temp2,customerID,finish);
                            }

                        }

                    }
                });
            }



        }
    }
}
