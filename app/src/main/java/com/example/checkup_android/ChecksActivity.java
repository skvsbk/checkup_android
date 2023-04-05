package com.example.checkup_android;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChecksActivity extends AppCompatActivity implements ChecksAdapter.SendButtonListener {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;

    RecyclerView recyclerView;
    List<Checks> checksList;
    ChecksAdapter checksAdapter;

    VarsSingleton vars;

    TextView textViewProgress, textViewRoute;
    ProgressBar progressBar;
    Integer routeCounter = 0;
    Integer routesTotal;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writingTagFilters;
    boolean writeMode;
    Tag myTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checks);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        urlAPIServer = settings.getString(PREF_URLAPI, "");

        vars = VarsSingleton.getInstance();
        recyclerView = findViewById(R.id.recyclerView_Checks);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewRoute = findViewById(R.id.textViewRoute);
        progressBar = findViewById(R.id.progressBar);

        textViewRoute.setText(vars.getStrVars("route_name"));
        fillRecyclerView();
        setRecyclerView();

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[] { tagDetected };
    }

    private void setRecyclerView() {
        checksAdapter = new ChecksAdapter(checksList, this);
        recyclerView.setAdapter(checksAdapter);
    }

    private void fillRecyclerView() {
        checksList = new ArrayList<>();
        // http://0.0.0.0:8000/rolutelinks/2
//        {
//            "id": 14,
//                "order": 10,
//                "nfc_serial": "53E9DC63200001",
//                "plant_name": "ЛОС",
//                "plant_description": "Локальные очистные сооружения",
//                "plant_description_params": "Общее состояние помещения и систем",
//                "plant_id": 50,
//                "val_name": "состояние помещения и систем",
//                "val_min": 14,
//                "val_max": 20,
//                "unit_name": "°C"
//        }
        String route_id = vars.getIntvars("route_id").toString();
        String queryAPI = urlAPIServer + "/rolutelinks/" + route_id;
        CheckupDataService checkupDataService = new CheckupDataService(ChecksActivity.this);
        checkupDataService.getJSONArray(queryAPI, new CheckupDataService.GetJSONArrayListener() {
            @Override
            public void onResponse(JSONArray responseJSONArray) {
                try {
                    for (int i = 0; i < responseJSONArray.length(); i++) {
                        JSONObject o = responseJSONArray.getJSONObject(i);
                        String plant_name = o.getString("plant_name");
                        String plant_descr = o.getString("plant_description");
                        String plant_descr_params = o.getString("plant_description_params");

                        Integer plant_id = o.getInt("plant_id");
                        String nfc_serial = o.getString("nfc_serial");
                        String val_name = null;
                        Float val_min = null;
                        Float val_max = null;
                        String unit_name = null;
                        if (!o.getString("val_name").equals("null")) {
                            val_name = o.getString("val_name");
                            val_min = Float.valueOf(o.getString("val_min"));
                            val_max = Float.valueOf(o.getString("val_max"));
                            unit_name = o.getString("unit_name");
                        }
                        checksList.add(new Checks(plant_name, plant_descr, plant_descr_params, plant_id, nfc_serial, null,
                                val_name, val_min, val_max, null, unit_name, null));
                        routesTotal = checksList.size();
                        String textProgress = "Прогресс: " + routeCounter.toString() + "/" + routesTotal.toString();
                        textViewProgress.setText(textProgress);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checksAdapter.notifyDataSetChanged();
            }

            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(ChecksActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed(){
        // alert message
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ChecksActivity.this);
        alertBuilder.setTitle(R.string.dialog_title)
                .setMessage(getString(R.string.dialog_checkup_text))
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> starRouteActivity())
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> Toast.makeText(getApplicationContext(), R.string.dialog_checkup_continue, Toast.LENGTH_LONG).show());
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void starRouteActivity() {
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
            }
        }
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
//        Read NFC and get plant from DB
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            byte[] tagId = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String nfc_serial = Utils.byteArrayToHex(tagId);
//            String nfc_serial = "53E9DC63200001";

            int currentPosition = vars.getIntvars("current_position");
            Checks currentCheck = checksList.get(currentPosition);
            currentCheck.setNfc_read(nfc_serial);
            checksAdapter.notifyItemChanged(currentPosition);
         }
    }

    @Override
//    public void onResume() {
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }

    // Enable Write
    private void WriteModeOn() {
        if (nfcAdapter != null) {
            writeMode = true;
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    // Disable Write
    private void WriteModeOff() {
        if (nfcAdapter != null) {
            writeMode = false;
            nfcAdapter.disableForegroundDispatch(this);
        }
    }


    @Override
    public void onSendButtonClick(int position) {
//        http://0.0.0.0:8000/checkup_details/
//        {
//            "header_id": 287,
//                "nfc_serial": "string",
//                "plant_id": 0,
//                "plant_name": "string",
//                "plant_description": "string",
//                "plant_description_params": "string",
//                "time_check": "2023-04-05T13:02:24.983Z",
//                "val_name": "string",
//                "val_min": 0,
//                "val_max": 0,
//                "unit_name": "string",
//                "val_fact": 0,
//                "note": "string"
//        }
        String queryAPI = urlAPIServer + "/checkup_details/";
        Checks currentItem = checksList.get(position);
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("header_id", vars.getIntvars("header_id"));
            jsonParams.put("nfc_serial", currentItem.getNfc_serial());
            jsonParams.put("plant_id", currentItem.getPlant_id());
            jsonParams.put("plant_name", currentItem.getPlant_name());
            jsonParams.put("plant_description", currentItem.getPlant_descr());
            jsonParams.put("plant_description_params", currentItem.getPlant_descr_params());
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jsonParams.put("time_check", formatForDateNow.format(dateNow));
            jsonParams.put("val_name", currentItem.getVal_name());
            jsonParams.put("val_min", currentItem.getVal_min());
            jsonParams.put("val_max", currentItem.getVal_max());
            jsonParams.put("unit_name", currentItem.getUnit_name());
            if (currentItem.getVal_name() != null && currentItem.getVal_fact() == null){
                Toast.makeText(ChecksActivity.this, "Введите значение параметра !", Toast.LENGTH_LONG).show();
                return;
            } else {
                jsonParams.put("val_fact", currentItem.getVal_fact());
            }
            jsonParams.put("note", currentItem.getNote());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CheckupDataService checkupDataService = new CheckupDataService(ChecksActivity.this);
        checkupDataService.postJSONObject("POST", queryAPI, jsonParams, new CheckupDataService.PostJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                // Collapse current
                Checks checkCurrent = checksList.get(position);
                checkCurrent.setExpandable(false);
                checkCurrent.setSend(true);
                checksAdapter.notifyItemChanged(position);

                Integer nextPos = position+1;
                // Expand next
                if (nextPos < checksList.size()) {
                    checksList.get(nextPos).setExpandable(true);
                    vars.setIntVars("current_position", nextPos);
//                        vars.setStrVars("current_nfc", nfcLinkedTxt.getText().toString());
                    checksAdapter.notifyItemChanged(nextPos);
                    int progress = 100 * nextPos / routesTotal;
                    progressBar.setProgress(progress);
                    String textProgress = "Прогресс: " + nextPos.toString() + "/" + routesTotal.toString();
                    textViewProgress.setText(textProgress);

                } else {
                    // Finish. Put checkup_headers time_finish and compleete
                    // Put time_finish to checkup_headers table and open RoutersAvtivity

                    putCheckupHeader();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(ChecksActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startRouteActivity(){
        Intent intent = new Intent(this, RouteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void putCheckupHeader(){
//        http://0.0.0.0:8000/checkup_headers/253
//        {
//            "time_finish": "2023-03-24T05:28:59.653Z",
//            "is_complete": true
//        }

        String queryAPI = urlAPIServer + "/checkup_headers/" + vars.getIntvars("header_id");

        JSONObject jsonParams = new JSONObject();
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            jsonParams.put("is_complete", true);
            jsonParams.put("time_finish", formatForDateNow.format(dateNow));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CheckupDataService checkupDataService = new CheckupDataService(ChecksActivity.this);
        checkupDataService.postJSONObject("PUT", queryAPI, jsonParams, new CheckupDataService.PostJSONObjectListener() {
            @Override
            public void onResponse(JSONObject responseJSONObject) {
                try {
                    if (responseJSONObject.get("detail").equals("success")) {
                        String textProgress = "Прогресс: " + routesTotal.toString() + "/" + routesTotal.toString();
                        textViewProgress.setText(textProgress);
                        progressBar.setProgress(100);
                        Toast.makeText(getApplicationContext(), "Обход завершен", Toast.LENGTH_SHORT).show();
                        startRouteActivity();
                    } else {
                        Toast.makeText(ChecksActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(ChecksActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });

    }
}