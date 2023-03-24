package com.example.checkup_android;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
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

import java.util.ArrayList;

public class AdmLinkNFCTagActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;
    VarsSingleton vars = VarsSingleton.getInstance();
    Integer facility_id;
    ArrayList<String> plantsArrayForSpinner = new ArrayList<>();
//    ArrayList<String> paramsArrayForSpinner = new ArrayList<>();
    ArrayAdapter<String> spinner_adapter_plants;

    JSONArray plantsArrayForDB;

    TextView nfc_contents;
    Spinner spinner_plants;
    Boolean allowToAdd;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writingTagFilters;
    boolean writeMode;
    Tag myTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm_link_nfctag);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        urlAPIServer = settings.getString(PREF_URLAPI, "");
        facility_id = vars.getIntvars("facility_id");

        nfc_contents = findViewById(R.id.nfc_contents);
        spinner_plants = findViewById(R.id.spinner_plants);

        nfc_contents.setText("");
        allowToAdd = false;

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[] { tagDetected };

//      Get plants from db and send it to spinner
        if (plantsArrayForSpinner.size() == 0) {
            plantsArrayForSpinner.add("");
        }

        fillSpinnerAdapter();

    }

    private void fillSpinnerAdapter(){
        //Get free plants http://127.0.0.1:8000/plants/free?facility_id=1
        String queryAPI = urlAPIServer + "/plants/free?facility_id=" + facility_id;

        CheckupDataService checkupDataService = new CheckupDataService(AdmLinkNFCTagActivity.this);
        checkupDataService.getJSONArray(queryAPI, new CheckupDataService.GetJSONArrayListener(){
            @Override
            public void onResponse(JSONArray responseJSONArray) {
                try {
                    plantsArrayForDB = responseJSONArray;
                    plantsArrayForSpinner.clear();
                    spinner_adapter_plants.clear();
                    for (int i=0; i<plantsArrayForDB.length(); i++) {
                        JSONObject o = (JSONObject) plantsArrayForDB.get(i);
                        plantsArrayForSpinner.add(o.getString("name"));
                    }
                    spinner_adapter_plants.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(String message) {
                Toast.makeText(AdmLinkNFCTagActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readFromIntent(Intent intent) {
        nfc_contents.setText("");
        String action = intent.getAction();

//        Read NFC and get plant from DB
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            byte[] tagId = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String nfc_serial = Utils.byteArrayToHex(tagId);
//            String nfc_serial = "53C5D463200001";
            nfc_contents.setText(nfc_serial);

//            Get Plant By Nfc Serial
//            http://127.0.0.1:8000/nfc/get_plant?nfc_serial=53C5D463200001&facility_id=1
            String queryAPI = urlAPIServer + "/nfc/get_plant?nfc_serial=" + nfc_serial + "&facility_id=" + facility_id;

            CheckupDataService checkupDataService = new CheckupDataService( AdmLinkNFCTagActivity.this);
            checkupDataService.getJSONObject(queryAPI, new CheckupDataService.GetJSONObjectListener() {
                @Override
                public void onResponse(JSONObject responseJSONObject) {
                    try {
//                        JSONObject response = new JSONObject(result);
                        if (responseJSONObject.has("detail")) {
                            allowToAdd = true;
                        } else {
                            // alert message with plant_name
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdmLinkNFCTagActivity.this);
                            alertBuilder.setTitle(R.string.dialog_title)
                                    .setMessage(getString(R.string.dialog_nfc_for) + responseJSONObject.getString("plant_name"))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            allowToAdd = false;
                                            nfc_contents.setText("");
                                        }
                                    });
                            AlertDialog dialog = alertBuilder.create();
                            dialog.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onErrorResponse(String message) {
                    Toast.makeText(AdmLinkNFCTagActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onClickAdd(View v) throws JSONException {
        if (nfc_contents.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), R.string.alert_nfc_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (allowToAdd && !nfc_contents.getText().toString().equals("")) {

            String plant_id = Utils.getIdFromArray(plantsArrayForDB, spinner_plants.getSelectedItem().toString());

            if (plant_id != null) {
                // CREATE nfc_tag with plant
                // POST http://0.0.0.0:8000/nfc/
                String queryAPI = urlAPIServer + "/nfc/";

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("nfc_serial", nfc_contents.getText().toString());
                jsonParams.put("plant_id", plant_id);
                jsonParams.put("active", true);

                CheckupDataService checkupDataService = new CheckupDataService(AdmLinkNFCTagActivity.this);
                checkupDataService.postJSONObject("POST", queryAPI, jsonParams, new CheckupDataService.PostJSONObjectListener() {
                    @Override
                    public void onResponse(JSONObject responseJSONObject) {
                        if (responseJSONObject.has("id")){
                            nfc_contents.setText("");
                            fillSpinnerAdapter();
                            Toast.makeText(getApplicationContext(), R.string.alert_ndc_added, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onErrorResponse(String message) {
                        Toast.makeText(AdmLinkNFCTagActivity.this, R.string.alert_fail, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), R.string.alert_plant_not_found, Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        WriteModeOn();
        if (spinner_adapter_plants == null) {
            spinner_adapter_plants = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.plantsArrayForSpinner);
            spinner_adapter_plants.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_plants.setAdapter(spinner_adapter_plants);
        } else {
            spinner_adapter_plants.notifyDataSetChanged();
        }
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
}