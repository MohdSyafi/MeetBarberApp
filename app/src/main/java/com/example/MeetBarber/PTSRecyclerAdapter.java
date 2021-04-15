package com.example.MeetBarber;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class PTSRecyclerAdapter extends RecyclerView.Adapter<PTSRecyclerAdapter.PTSViewHolder> {

    private ArrayList<TimeSlot> timeSlotList;
    LayoutInflater inflater;
    private BookClickInterface bookClickInterface;
    TimeSlot timeSlot;

    public PTSRecyclerAdapter(ArrayList<TimeSlot> timeSlotList, Context context,BookClickInterface bookClickInterface) {
        this.timeSlotList = timeSlotList;
        this.inflater = LayoutInflater.from(context);
        this.bookClickInterface = bookClickInterface;
    }

    @NonNull
    @Override
    public PTSRecyclerAdapter.PTSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_grid_layout,parent,false);
        return new PTSRecyclerAdapter.PTSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PTSRecyclerAdapter.PTSViewHolder holder, int position) {
        holder.TimeSlotTV.setText(timeSlotList.get(position).getTimeslot());

        if(timeSlotList.get(position).isSelected()){

            if(timeSlotList.get(position).isBooked()){
                holder.mView.setBackgroundResource(R.color.colorRed);
                holder.TimeSlotTV.setTextColor(Color.parseColor("#000000"));
                holder.cardView.setClickable(false);
            }else{
                holder.mView.setBackgroundResource(R.color.colorYellow);
                holder.TimeSlotTV.setTextColor(Color.parseColor("#000000"));
            }

        }else{

            if(timeSlotList.get(position).isBooked()){
                holder.mView.setBackgroundResource(R.color.colorRed);
                holder.TimeSlotTV.setTextColor(Color.parseColor("#000000"));
                holder.cardView.setClickable(false);
            }else{
                holder.mView.setBackgroundResource(R.drawable.customborder);
                holder.TimeSlotTV.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }

    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    class PTSViewHolder extends RecyclerView.ViewHolder {

        TextView TimeSlotTV;
        View mView;
        CardView cardView;

        public PTSViewHolder(@NonNull View itemView) {
            super(itemView);

            TimeSlotTV = itemView.findViewById(R.id.PTSTimeSlot);
            mView = itemView.findViewById(R.id.CGLayout);
            cardView = itemView.findViewById(R.id.PTSCardView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!timeSlotList.get(getAdapterPosition()).isBooked()){
                        bookClickInterface.onItemClick(getAdapterPosition());
                    }

                    for(int i=0; i<timeSlotList.size();i++)
                    {
                        timeSlotList.get(i).setSelected(false);
                    }
                    timeSlotList.get(getAdapterPosition()).setSelected(true);
                    notifyDataSetChanged();

                }
            });
        }
    }
}
