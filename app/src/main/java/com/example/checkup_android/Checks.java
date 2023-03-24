package com.example.checkup_android;
// for RecyclerView


public class Checks {
    private String nfc_serial, nfc_read, plant_name, val_name, unit_name, note;
    private Integer plant_id;
    private Float val_min, val_max, val_fact;
    private boolean expandable, send;

    public Checks(String plant_name, Integer plant_id, String nfc_serial, String nfc_read,
                  String val_name, Float val_min, Float val_max, Float val_fact, String unit_name, String note) {
        this.nfc_serial = nfc_serial;
        this.nfc_read = nfc_read;
        this.plant_id = plant_id;
        this.plant_name = plant_name;
        this.val_name = val_name;
        this.val_min = val_min;
        this.val_max = val_max;
        this.val_fact = val_fact;
        this.unit_name = unit_name;
        this.note = note;


        this.expandable = false;
        this.send = false;
    }

    public String getNfc_serial() {
        return nfc_serial;
    }

    public void setNfc_serial(String nfc_serial) {
        this.nfc_serial = nfc_serial;
    }

    public String getPlant_name() {
        return plant_name;
    }

    public void setPlant_name(String plant_name) {
        this.plant_name = plant_name;
    }

    public String getVal_name() {
        return val_name;
    }

    public void setVal_name(String val_name) {
        this.val_name = val_name;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public Integer getPlant_id() {
        return plant_id;
    }

    public void setPlant_id(Integer plant_id) {
        this.plant_id = plant_id;
    }

    public Float getVal_min() {
        return val_min;
    }

    public void setVal_min(Float val_min) {
        this.val_min = val_min;
    }

    public Float getVal_max() {
        return val_max;
    }

    public void setVal_max(Float val_max) {
        this.val_max = val_max;
    }

    public String getNfc_read() {
        return nfc_read;
    }

    public void setNfc_read(String nfc_read) {
        this.nfc_read = nfc_read;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Float getVal_fact() {
        return val_fact;
    }

    public void setVal_fact(Float val_fact) {
        this.val_fact = val_fact;
    }
}
