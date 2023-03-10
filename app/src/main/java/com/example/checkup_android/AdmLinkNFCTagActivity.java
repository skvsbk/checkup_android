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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class AdmLinkNFCTagActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;
    SingleVars vars = SingleVars.getInstance();
    Integer facility_id;
    AsyncHttpClient httpClient = new AsyncHttpClient();
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
//        if (paramsArrayForSpinner.size() == 0) {
//            paramsArrayForSpinner.add("");
//        }
        //Get free plants http://127.0.0.1:8000/plants/free?facility_id=1
        String queryAPI = urlAPIServer + "/plants/free?facility_id=" + facility_id;
        httpClient.get(queryAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String (responseBody);
                try {
                    plantsArrayForDB = new JSONArray(result);
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
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
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
            httpClient.get(queryAPI, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    try {
                        JSONObject response = new JSONObject(result);
                        if (response.has("detail")) {
                            allowToAdd = true;
                        } else {
                            // alert message with plant_name
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdmLinkNFCTagActivity.this);
                            alertBuilder.setTitle(R.string.dialog_title)
                                    .setMessage(getString(R.string.dialog_nfc_for) + response.getString("plant_name"))
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
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
                }
            });
//            Attempt to get val_params (if exists)
//            String queryAPIparams = urlAPIServer + "/valparams/nfc_serial/" + nfc_serial;
//            httpClient.get(queryAPIparams, new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    String result = new String(responseBody);
//                    try {
//                        JSONObject response = new JSONObject(result);
//                        if (response.has("detail")) {
//                            nfc_params_name.setText("");
//                            nfc_params.setText("");
//                        } else {
//                            nfc_params_name.setText("????????:\nMin:\nMax:\n????.:");
//                            String textParams = response.getString("name") + "\n" +
//                                    response.getString("min_value") + "\n" +
//                                    response.getString("max_value") + "\n" +
//                                    response.getString("unit");
//                            nfc_params.setText(textParams);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    Toast.makeText(getApplicationContext(), "?????? ???????????????????? ?? ????????????????", Toast.LENGTH_SHORT).show();
//                }
//            });
// if
        }
    }

    public void onClickAdd(View v) {
        if (allowToAdd && !nfc_contents.equals("")) {

            String plant_id = Utils.getIdFromArray(plantsArrayForDB, spinner_plants.getSelectedItem().toString());

            if (plant_id != null) {
                // CREATE nfc_tag with plant
                // POST http://127.0.0.1:8000/nfc/?nfc_serial=1234567890&plant_id=23
                String queryAPI = urlAPIServer + "/nfc/?nfc_serial=" + nfc_contents.getText().toString() + "&plant_id=" + plant_id;
                httpClient.post(queryAPI, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(getApplicationContext(), R.string.alert_ndc_added, Toast.LENGTH_SHORT).show();
                        nfc_contents.setText("");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), R.string.alert_fail, Toast.LENGTH_SHORT).show();
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