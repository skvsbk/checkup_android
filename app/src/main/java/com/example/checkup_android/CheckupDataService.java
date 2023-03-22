package com.example.checkup_android;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

public class CheckupDataService {

//    JSONArray routeLinksArray;
    Context context;

    public CheckupDataService(Context context) {
        this.context = context;
    }

    public interface GetRouteLinksListener {
        void onResponse(JSONArray routeLinksArray);
        void onErrorResponse(String message);
    }

    public void getRouteLinks(String urlAPIServer, String roite_id, GetRouteLinksListener getRouteLinksListener){

//        http://0.0.0.0:8000/rolutelinks/2
        String queryAPIroutes = urlAPIServer + "/rolutelinks/" + roite_id;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, queryAPIroutes, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                getRouteLinksListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getRouteLinksListener.onErrorResponse(String.valueOf(R.string.alert_fail));
            }
        });
        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }
}
