package com.example.MeetBarber;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.ArrayList;

public class Section implements Comparable<Section> {

    private String SectionName;
    private LocalDate SectionDate;
    private ArrayList<apnmtDetails> SectionItem;

    public Section(String sectionName, LocalDate sectionDate, ArrayList<apnmtDetails> sectionItem) {
        SectionName = sectionName;
        SectionDate = sectionDate;
        SectionItem = sectionItem;
    }

    public Section() {

    }

    public LocalDate getSectionDate() {
        return SectionDate;
    }

    public void setSectionDate(LocalDate sectionDate) {
        SectionDate = sectionDate;
    }

    public String getSectionName() {
        return SectionName;
    }

    public void setSectionName(String sectionName) {
        SectionName = sectionName;
    }

    public ArrayList<apnmtDetails> getSectionItem() {
        return SectionItem;
    }

    public void setSectionItem(ArrayList<apnmtDetails> sectionItem) {
        SectionItem = sectionItem;
    }

    @Override
    public String toString() {
        return "Section{" +
                "SectionName='" + SectionName + '\'' +
                ", SectionItem=" + SectionItem +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int compareTo(Section o) {
        if (getSectionDate() == null || o.getSectionDate() == null)
            return 0;
        return getSectionDate().compareTo(o.getSectionDate());
    }
    }

