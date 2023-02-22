package com.example.checkup_android;

import java.util.HashMap;
import java.util.Map;

public class SingleVars {

    private static SingleVars INSTANCE;
    Map<String, Integer> vars;

    public SingleVars() {
        vars = new HashMap<String, Integer>();
    };

    public void setVars(String var_key, Integer var_value) {
        this.vars.put(var_key, var_value);
    }

    public Integer getVars(String var_key){
        Integer a = 0;
        try {
            return this.vars.get(var_key);    
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
