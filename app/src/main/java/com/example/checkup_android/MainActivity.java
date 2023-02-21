package com.example.checkup_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    private static final String PREF_LOGIN = "Login";
    private static final String PREF_FACILITY = "Facility";
    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    TextView editTextPersonName, editTextPassword,textMsg;
    Spinner spinner_facitities;
//    String[] facilities = getFacility();
    ArrayList<String> facilities = new ArrayList<String>();
    ArrayAdapter<String> spinner_adapter;

    String urlAPIServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPersonName = findViewById(R.id.editTextPersonName);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinner_facitities = (Spinner) findViewById(R.id.spinner_facitities);
        textMsg = findViewById(R.id.textView4);

        //Get saved login
        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String loginName = settings.getString(PREF_LOGIN, "");
        editTextPersonName.setText(loginName);
        urlAPIServer = settings.getString(PREF_URLAPI, "");

        //Get saved facility and choose it in spinner
//        getFacility1();
        if (facilities.size() == 0) {
            facilities.add("None");
        }

        String queryAPI = urlAPIServer + "/facilities/";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(queryAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String (responseBody);
                try {
                    JSONArray array = new JSONArray(result);
                    facilities.clear();
                    spinner_adapter.clear();
                    for (int i=0; i<array.length(); i++) {
                        JSONObject o = (JSONObject) array.get(i);
                        facilities.add(o.getString("name"));
                    }
                    textMsg.setText(facilities.get(0));

                    //Обновление выпадающего списка
                    spinner_adapter.notifyDataSetChanged();

                    //Курсор на сохраненную позицию
                    String facilityName = settings.getString(PREF_FACILITY, "");
                    int position = spinner_adapter.getPosition(facilityName);
                    spinner_facitities.setSelection(position);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "Нет соединения с сервером", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
//        CrimeLab crimeLab = CrimeLab.get(getActivity());
//        List<Crime> crimes = crimeLab.getCrimes();
        if (spinner_adapter == null) {
            spinner_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.facilities);
            spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_facitities.setAdapter(spinner_adapter);
        } else {
//            spinner_adapter.clear();
//            spinner_adapter.addAll(this.facilities);
//            spinner_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.facilities);
            spinner_adapter.notifyDataSetChanged();

        }
    }

    public void onClickSetup(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    public void onClickEnter(View v) {
        if (editTextPersonName.length() == 0) {
            String toastText = "Введите имя пользователя";
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
            return;
        }

        getUser(editTextPersonName.getText().toString());

        // Save login and facility
        String loginName = editTextPersonName.getText().toString();
        String facilityName = spinner_facitities.getSelectedItem().toString();
        prefEditor = settings.edit();
        prefEditor.putString(PREF_LOGIN, loginName);
        prefEditor.putString(PREF_FACILITY, facilityName);
        prefEditor.apply();
    }

    public static String generate_password_hash(String st) {
        MessageDigest messageDigest;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    private void getUser(String login_name) {
        // Authentication: get from db by username, compare password hash, return json dict {name, role}
        // token?

        //Get saved url
        String queryAPI = urlAPIServer + "/users/" + login_name;

//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
//            queryAPI, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            try {
//                                if (response.getString("detail").equals("User not found")) {
//                                    Toast.makeText(getApplicationContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show();
//                                }
//                            } catch (Exception e) {
//    //                                Toast.makeText(getApplicationContext(), "?????", Toast.LENGTH_SHORT).show();
//                            }
//
//                            if (response.getString("role_name").equals("admin")) {
//                                String hashedPass;
//                                String receivedPass;
//
//                                receivedPass = response.getString("password");
//                                hashedPass = generate_password_hash(editTextPassword.getText().toString());
//
//                                if (receivedPass.equals(hashedPass)) {
//                                    startAdminActivity();
//                                } else {
//                                    Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_SHORT).show();
//                                }
//
//                            } else if (response.getString("role_name").equals("user_mobapp")) {
//                                startRouteActivity();
//                            } else  if (response.getString("role_name").equals("user_webapp")) {
//                                Toast.makeText(getApplicationContext(), "Неподходящая роль", Toast.LENGTH_SHORT).show();
//                            }
//
//                        } catch (Exception e) {
//                            Toast.makeText(getApplicationContext(), "Нет связи с API сервером", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getApplicationContext(), "Нет соединения с сервером", Toast.LENGTH_SHORT).show();
//
//                    }
//            });
//        RequestQueue requestQueue = newRequestQueue(this);
//        requestQueue.add(jsonObjectRequest);
    }

    public void startAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    public void startRouteActivity() {
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    private String[] getFacility() {
        return new String[] {"Пушкин", "Металлострой"};
    }

    private void getFacility1() {
        // Get all facilities name from db
        String queryAPI = urlAPIServer + "/facilities/";


    }

}