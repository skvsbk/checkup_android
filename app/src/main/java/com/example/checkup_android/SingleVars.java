package com.example.checkup_android;

public class SingleVars {

    private static final SingleVars INSTANCE = new SingleVars();
    public SingleVars(){};

    public static SingleVars getInstance(){
        return INSTANCE;
    }

    String test = "";
//    SingleVars().getInstance().test = "value";
//    var = SingleVars().getInstance().test;
}
