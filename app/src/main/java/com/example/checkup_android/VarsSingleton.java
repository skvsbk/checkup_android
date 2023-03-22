package com.example.checkup_android;

import java.util.HashMap;
import java.util.Map;

public class VarsSingleton {

    private static VarsSingleton INSTANCE;
    Map<String, Integer> intVars;
    Map<String, String> strVars;

    public VarsSingleton() {

        intVars = new HashMap<String, Integer>();
        strVars = new HashMap<String, String>();
    }

    ;

    public void setStrVars(String var_key, String var_value) {
        this.strVars.put(var_key, var_value);
    }

    public String getStrVars(String var_key) {
        try {
            return this.strVars.get(var_key);
        } catch (Exception e) {
            return "";
        }
    }

    public void setIntVars(String var_key, Integer var_value) {
        this.intVars.put(var_key, var_value);
    }

    public Integer getIntvars(String var_key) {
        try {
            return this.intVars.get(var_key);
        } catch (Exception e) {
            return 0;
        }
    }

    public static VarsSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VarsSingleton();
        }
        return INSTANCE;
    }
}
