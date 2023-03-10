package com.example.checkup_android;
// for RecyclerView


public class Checkups {
    private String route_name;
    private String date;
    private String facility_name;

    public Checkups(String textView_date, String textView_facility, String textView_route) {

        this.route_name = textView_route;
        this.date = textView_date;
        this.facility_name = textView_facility;

    }

    public String getRouteName() {
        return this.route_name;
    }

    public void setRouteName(String textView_route) {
        this.route_name = textView_route;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String textView_date) {
        this.date = textView_date;
    }

    public String getFacilityName() {
        return this.facility_name;
    }

    public void setFacilityName(String textView_facility) {
        this.facility_name = textView_facility;
    }

}
