package com.example.checkup_android;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class CheckupDataService {

    Context context;

    public CheckupDataService(Context context) {
        this.context = context;
    }

    public interface GetJSONArrayListener {
        void onResponse(JSONArray responseJSONArray);
        void onErrorResponse(String message);
    }

    public void getJSONArray(String urlAPI, GetJSONArrayListener getJSONArrayListener){

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlAPI, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                getJSONArrayListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getJSONArrayListener.onErrorResponse(String.valueOf(R.string.alert_fail));
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface GetJSONObjectListener {
        void onResponse(JSONObject responseJSONObject);
        void onErrorResponse(String message);
}
    public void getJSONObject(String urlAPI, CheckupDataService.GetJSONObjectListener getJSONObjectListener){

        JsonObjectRequest request = new JsonObjectRequest(urlAPI, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                getJSONObjectListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getJSONObjectListener.onErrorResponse(String.valueOf(R.string.alert_fail));
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface PostJSONObjectListener {
        void onResponse(JSONObject responseJSONObject);
        void onErrorResponse(String message);
    }

    public void postJSONObject(String method, String urlAPI, JSONObject params, CheckupDataService.PostJSONObjectListener postJSONObjectListener){

        int requestMethod;
        if (method.equals("POST")) {
            requestMethod = Request.Method.POST;
        } else if (method.equals("PUT")) {
            requestMethod = Request.Method.PUT;
                } else {
                    return;
                }

        JsonObjectRequest request = new JsonObjectRequest(requestMethod, urlAPI, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                postJSONObjectListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                postJSONObjectListener.onErrorResponse(String.valueOf(R.string.alert_fail));
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }
}