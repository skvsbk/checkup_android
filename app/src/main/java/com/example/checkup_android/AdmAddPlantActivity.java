package com.example.checkup_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdmAddPlantActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;

    ArrayList<Plants> plants = new ArrayList<>();
    TextView facility_name, editTextPlants;
    VarsSingleton vars = VarsSingleton.getInstance();
    Integer facility_id;

    RecyclerView recyclerView;
    PlantsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm_add_plant);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        urlAPIServer = settings.getString(PREF_URLAPI, "");

        facility_name = findViewById(R.id.textView_facility_name);
        editTextPlants = findViewById(R.id.editTextPlants);

        facility_name.setText(vars.getStrVars("facility_name"));
        facility_id = vars.getIntvars("facility_id");

        // начальная инициализация списка
        fillListView();
        recyclerView = findViewById(R.id.list);
        // создаем адаптер
        adapter = new PlantsAdapter(this, plants);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

    private void fillListView() {
//      'http://127.0.0.1:8000/plants/?facility_id=1'
        String queryAPI = urlAPIServer + "/plants/?facility_id=" + facility_id;

        CheckupDataService checkupDataService = new CheckupDataService(AdmAddPlantActivity.this);
        checkupDataService.getJSONArray(queryAPI, new CheckupDataService.GetJSONArrayListener() {
            @Override
            public void onResponse(JSONArray responseJSONArray) {
                try {
                    String nfc_serial = null;
                    Integer setIcon = null;
                    for (int i = 0; i<responseJSONArray.length(); i++){
                        JSONObject o = responseJSONArray.getJSONObject(i);
                        if (o.getString("nfc_serial").equals("null")) {
                            nfc_serial = "";
                        } else {
                            nfc_serial = o.getString("nfc_serial");
                        }
                        if (o.getString("active").equals("true")) {
                            setIcon = R.drawable.check_mark_icon;
                        } else if (o.getString("active").equals("false")){
                            setIcon = R.drawable.cross_icon;
                        } else {
                            setIcon = 0;
                        }
                        plants.add(new Plants(o.getString("plant_name"), nfc_serial, setIcon));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(AdmAddPlantActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickAdd(View v) {
        if (editTextPlants.getText().toString().equals("")) {
            return;
        }

//      GET http://127.0.0.1:8000/plants/name/1.004?facility_id=1
        String queryAPI = urlAPIServer + "/plants/name/" + editTextPlants.getText() + "?facility_id=" + facility_id;

        CheckupDataService checkupDataService = new CheckupDataService(AdmAddPlantActivity.this);
        checkupDataService.getJSONObject(queryAPI, new CheckupDataService.GetJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                // if plant_name is free - create new plant
                if (responseJSONObject.has("detail")) {
                    addPlant();
                } else {
                    Toast.makeText(AdmAddPlantActivity.this, R.string.alert_unique_name, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPlant() {
//      POST 'http://127.0.0.1:8000/plants/'
        String queryAPI = urlAPIServer + "/plants/";

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", editTextPlants.getText());
            jsonParams.put("facility_id", facility_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CheckupDataService checkupDataService = new CheckupDataService(AdmAddPlantActivity.this);
        checkupDataService.postJSONObject(queryAPI, jsonParams, new CheckupDataService.PostJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                editTextPlants.setText("");
                Toast.makeText(getApplicationContext(), R.string.alert_item_sucсess_added, Toast.LENGTH_SHORT).show();
                plants.clear();
                fillListView();
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }
}