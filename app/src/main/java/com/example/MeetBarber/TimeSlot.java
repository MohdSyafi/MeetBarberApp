package com.example.MeetBarber;

public class TimeSlot {
    private String timeslot;
    public boolean isSelected;
    public boolean isBooked;

    public TimeSlot(String timeslot, boolean isSelected, boolean isBooked) {
        this.timeslot = timeslot;
        this.isSelected = isSelected;
        this.isBooked = isBooked;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public void setBooked(boolean selection){
        this.isBooked = selection;
    }

    public boolean isBooked(){
        return isBooked;
    }


    public void setSelected(boolean selection){
        this.isSelected = selection;
    }

    public boolean isSelected(){
        return isSelected;
    }
}
