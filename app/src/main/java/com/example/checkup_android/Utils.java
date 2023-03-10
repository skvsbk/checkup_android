package com.example.checkup_android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {


    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    public static String getIdFromArray(JSONArray arrayForDB, String name) {
        String id = null;
        for (int i=0; i<arrayForDB.length(); i++){
            JSONObject obj = null;
            try {
                obj = arrayForDB.getJSONObject(i);
                if (obj.getString("name").equals(name)) {
                    id = obj.getString("id");
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return id;
    }
}
