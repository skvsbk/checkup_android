package com.example.checkup_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    private static final String PREF_LOGIN = "Login";
    private static final String PREF_FACILITY = "Facility";

    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    TextView editTextPersonName, editTextPassword;

    Spinner spinner_facitities;
    ArrayList<String> facilitiesArray = new ArrayList<String>();
    ArrayAdapter<String> spinner_adapter;

    String urlAPIServer;

    //For transfer user_id, role_id, facility_id to another activities
    VarsSingleton vars = VarsSingleton.getInstance();

    NfcAdapter nfcAdapter;


//    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1 - debug, 0 - prod
        vars.setIntVars("debug", 0);

        // Uncomment it for real device
        if (vars.getIntvars("debug") == 0){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter == null) {
                AlertDialog.Builder builderAlert = new AlertDialog.Builder(MainActivity.this);
                builderAlert.setTitle("Ошибка")
                        .setMessage("Это устройство не поддерживает NFC")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = builderAlert.create();
                dialog.show();
            }
        }

        editTextPersonName = findViewById(R.id.editTextPersonName);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinner_facitities = (Spinner) findViewById(R.id.spinner_facitities);

        //Get saved login
        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String loginName = settings.getString(PREF_LOGIN, "");
        editTextPersonName.setText(loginName);
        urlAPIServer = settings.getString(PREF_URLAPI, "");

        //Get facilities from db and send it to spinner
        if (facilitiesArray.size() == 0) {
            facilitiesArray.add("None");
        }

        String queryAPI = urlAPIServer + "/facilities/";
        CheckupDataService checkupDataService = new CheckupDataService(MainActivity.this);
        checkupDataService.getJSONArray(queryAPI, new CheckupDataService.GetJSONArrayListener() {
            @Override
            public void onResponse(JSONArray responseJSONArray) {
                try {
                    facilitiesArray.clear();
                    spinner_adapter.clear();
                    for (int i = 0; i < responseJSONArray.length(); i++) {
                        JSONObject o = (JSONObject) responseJSONArray.get(i);
                        facilitiesArray.add(o.getString("name"));
                    }
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
            public void onErrorResponse(String message) {
                Toast.makeText(MainActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (spinner_adapter == null) {
            spinner_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.facilitiesArray);
            spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_facitities.setAdapter(spinner_adapter);
        } else {
//            spinner_adapter.clear();
//            spinner_adapter.addAll(this.facilities);
            spinner_adapter.notifyDataSetChanged();
        }
    }

    public void onClickSetup(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    public void onClickEnter(View v) {
        if (editTextPersonName.length() == 0) {
            Toast.makeText(this, R.string.alert_enter_login, Toast.LENGTH_LONG).show();
            return;
        }

        //Get user from db, check password
        getUser(editTextPersonName.getText().toString());

        // Save login and facility
        String loginName = editTextPersonName.getText().toString();
        String facilityName = spinner_facitities.getSelectedItem().toString();
        prefEditor = settings.edit();
        prefEditor.putString(PREF_LOGIN, loginName);
        prefEditor.putString(PREF_FACILITY, facilityName);
        prefEditor.apply();

        // Save to SingleVars
        getFacilityId(spinner_facitities.getSelectedItem().toString());

    }

    private static String generate_password_hash(String st) {
        MessageDigest messageDigest;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // Ошибка, если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0".concat(md5Hex);
        }

        return md5Hex;
    }

    private static Boolean comparePasswords(String db_passwd, String app_passwd) {
        String hashedAppPasswd;
        hashedAppPasswd = generate_password_hash(app_passwd);
        return db_passwd.equals(hashedAppPasswd);
    }

    private void getFacilityId(String facility_name){

        String queryAPI = urlAPIServer + "/facilities/" + facility_name;
        CheckupDataService checkupDataService = new CheckupDataService( MainActivity.this);
        checkupDataService.getJSONObject(queryAPI, new CheckupDataService.GetJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                try {
                    vars.setIntVars("facility_id", Integer.parseInt(responseJSONObject.getString("id")));
                    vars.setStrVars("facility_name", facility_name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(MainActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRoleId(String role_name){
        String queryAPI = urlAPIServer + "/roles/" + role_name;
        CheckupDataService checkupDataService = new CheckupDataService( MainActivity.this);
        checkupDataService.getJSONObject(queryAPI, new CheckupDataService.GetJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                try {
                    vars.setIntVars("role_id", Integer.parseInt(responseJSONObject.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(MainActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUser(String login_name) {
        // Authentication: get from db by username, compare password hash, return json dict {name, role}
        // token?
//      http://127.0.0.1:8000/users/user_m
        String queryAPI = urlAPIServer + "/users/" + login_name;
        CheckupDataService checkupDataService = new CheckupDataService( MainActivity.this);
        checkupDataService.getJSONObject(queryAPI, new CheckupDataService.GetJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
//                 /////   Анализ "detail": "User not found" - прервать с ошибкой
//                  if (response.getString("detail").equals("User not found")) {
//                      Toast.makeText(MainActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
//                  }
//                  /////
                try {
                    String receivedPass = responseJSONObject.getString("password");
                    if (responseJSONObject.getString("role_name").equals("user_webapp")) {
                        Toast.makeText(MainActivity.this, R.string.alert_wrong_role, Toast.LENGTH_SHORT).show();
                    } else if (responseJSONObject.getString("role_name").equals("admin")) {
                        if (comparePasswords(receivedPass, editTextPassword.getText().toString())) {
                            //?? still a question
                            vars.setIntVars("user_id", Integer.parseInt(responseJSONObject.getString("id")));
                            vars.setStrVars("user_name", responseJSONObject.getString("name"));
                            getRoleId(responseJSONObject.getString("role_name"));
                            // Open admin activity
                            startAdminActivity();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.alert_bad_passwd, Toast.LENGTH_SHORT).show();
                        }
                    } else if (responseJSONObject.getString("role_name").equals("user_mobapp")) {
                        if (comparePasswords(receivedPass, editTextPassword.getText().toString())) {
                            vars.setIntVars("user_id", Integer.parseInt(responseJSONObject.getString("id")));
                            vars.setStrVars("user_name", responseJSONObject.getString("name"));
                            getRoleId(responseJSONObject.getString("role_name"));
                            startRouteActivity();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.alert_bad_passwd, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, getString(R.string.alert_error_getting_data) + e, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(MainActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    public void startRouteActivity() {
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

}