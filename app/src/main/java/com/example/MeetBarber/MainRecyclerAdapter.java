package com.example.MeetBarber;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.MainViewHolder> {

    private ArrayList<Section> sectionList;
    private HomePage homePage;
    private  History history;
    String clickable = "yes";
    private Context context;
    private String lang;

    public MainRecyclerAdapter(ArrayList<Section> sectionList, HomePage homePage) {
        this.sectionList = sectionList;
        this.homePage = homePage;


    }

    public MainRecyclerAdapter(ArrayList<Section> sectionList, History history) {
        this.sectionList = sectionList;
        this.history = history;
        clickable = "no";
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.section_row,parent,false);
        return new MainViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
            Section section = sectionList.get(position);
            String SectionName  = section.getSectionName();
            String tempp = SectionName.replace("/"," ");
            ArrayList<apnmtDetails> items = section.getSectionItem();
            holder.sectionName.setText(tempp);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);

        lang = sh.getString("Locale.Helper.Selected.Language","");

           if(section.getSectionDate().equals(LocalDate.now())){

               if(lang.equalsIgnoreCase("ms")){
                   holder.sectionName.setText("Hari ini");
               }else{
                   holder.sectionName.setText("Today");
               }

           }

            if(clickable.equalsIgnoreCase("no")){
                childRecyclerAdapter childRecyclerAdapter = new childRecyclerAdapter(items,history,clickable);
                holder.childRecyclerView.setAdapter(childRecyclerAdapter);
            }else{
                childRecyclerAdapter childRecyclerAdapter = new childRecyclerAdapter(items,homePage,clickable);
                holder.childRecyclerView.setAdapter(childRecyclerAdapter);
            }

    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder{

        TextView sectionName;
        RecyclerView childRecyclerView;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);

            sectionName = itemView.findViewById(R.id.HPdate);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
