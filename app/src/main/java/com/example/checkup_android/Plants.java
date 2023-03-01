package com.example.checkup_android;

public class Plants {
    private String plant_name;
    private String nfc_serial;
    private int icon;


    public Plants(String plant_name, String nfc_serial, int icon) { //, int flag){

        this.plant_name = plant_name;
        this.nfc_serial = nfc_serial;
        this.icon = icon;

    }

    public String getPlant_name() {
        return this.plant_name;
    }

    public void setPlant_name(String plant_name) {
        this.plant_name = plant_name;
    }

    public String getNfc_serial() {
        return this.nfc_serial;
    }

    public void setNfc_serial(String nfc_serial) {
        this.nfc_serial = nfc_serial;
    }

    public int getIconResource() {
        return this.icon;
    }

    public void setIconResource(int iconResource) {
        this.icon = iconResource;
    }


}
