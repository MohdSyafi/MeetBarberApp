package com.example.MeetBarber;

import java.util.ArrayList;

public class Section implements Comparable<Section> {

    private String SectionName;
    private ArrayList<apnmtDetails> SectionItem;

    public Section(String sectionName, ArrayList<apnmtDetails> sectionItem) {
        SectionName = sectionName;
        SectionItem = sectionItem;
    }

    public Section() {

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

    @Override
    public int compareTo(Section section) {
        return getSectionName().compareTo(section.getSectionName());
    }
}
