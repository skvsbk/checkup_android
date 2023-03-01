package com.example.checkup_android;

import java.util.HashMap;
import java.util.Map;

public class SingleVars {

    private static SingleVars INSTANCE;
    Map<String, Integer> intVars;
    Map<String, String> strVars;

    public SingleVars() {

        intVars = new HashMap<String, Integer>();
        strVars = new HashMap<String, String>();
    };

    public void setStrVars(String var_key, String var_value) {
        this.strVars.put(var_key, var_value);
    }

    public String getStrVars(String var_key){
//        Integer a = 0;
        try {
            return this.strVars.get(var_key);
        } catch (Exception e){
            return "";
        }
    }

    public void setIntVars(String var_key, Integer var_value) {
        this.intVars.put(var_key, var_value);
    }

    public Integer getIntvars(String var_key){
//        Integer a = 0;
        try {
            return this.intVars.get(var_key);
        } catch (Exception e){
            return 0;
        }
    }

    public static SingleVars getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new SingleVars();
        }
            return INSTANCE;
    }
}
