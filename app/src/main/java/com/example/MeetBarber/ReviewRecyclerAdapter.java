package com.example.MeetBarber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.ReviewViewHolder>{

    private ArrayList<ReviewDetails> reviewDetailsList;

    public ReviewRecyclerAdapter(ArrayList<ReviewDetails> reviewDetailsList) {

        this.reviewDetailsList = reviewDetailsList;

    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.reviewlist,parent,false);
        return new ReviewRecyclerAdapter.ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {

        ReviewDetails reviewDetails = reviewDetailsList.get(position);
        String customername  = reviewDetails.getReviewCustomerName();
        String date = reviewDetails.getReviewDate();
        String comm = reviewDetails.getReviewComments();
        String uri = reviewDetails.getReviewImageUrl();
        float i = Float.parseFloat(reviewDetails.getRecviewRatingValue());

        holder.customername.setText(customername);
        holder.Date.setText(date);
        holder.comments.setText(comm);
        Picasso.get().load(uri).into(holder.img);
        holder.bar.setRating(i);
    }

    @Override
    public int getItemCount() {
        return reviewDetailsList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        TextView customername, Date, comments;
        RatingBar bar;
        CircleImageView img;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            bar = itemView.findViewById(R.id.reviewlistratingbar);
            customername = itemView.findViewById(R.id.reviewlistcustomername);
            Date = itemView.findViewById(R.id.reviewlistDate);
            comments = itemView.findViewById(R.id.reviewlistComments);
            img = itemView.findViewById(R.id.reviewlistimage);


        }
    }

}
