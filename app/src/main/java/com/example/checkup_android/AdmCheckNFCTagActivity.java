package com.example.checkup_android;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AdmCheckNFCTagActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";
    SharedPreferences settings;
    String urlAPIServer;
    AsyncHttpClient httpClient = new AsyncHttpClient();

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writingTagFilters;
    boolean writeMode;
    Tag myTag;

    TextView nfc_contents;
    TextView nfc_plant;
    TextView nfc_params;
    TextView nfc_params_name;
    TextView nfc_facility;
    Button nfc_button;



//    SingleVars singleVars = SingleVars.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adm_check_nfctag);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        urlAPIServer = settings.getString(PREF_URLAPI, "");

        nfc_contents =  (TextView) findViewById(R.id.nfc_contents);
        nfc_facility =  (TextView) findViewById(R.id.nfc_facility);
        nfc_plant =  (TextView) findViewById(R.id.nfc_plant);
        nfc_params =  (TextView) findViewById(R.id.nfc_params);
        nfc_params_name =  (TextView) findViewById(R.id.nfc_params_name);
        nfc_button = (Button) findViewById(R.id.nfc_button);

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[] { tagDetected };

        nfc_button.setOnClickListener(view -> {
            clearTextView();
        });
    }

    private void readFromIntent(Intent intent) {
        clearTextView();
        String action = intent.getAction();

//        Read NFC and get plant from DB
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            byte[] tagId = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String nfc_serial = Utils.byteArrayToHex(tagId);
//            String nfc_serial = "53E9DC63200001";
            nfc_contents.setText(nfc_serial);

//            Get Plant By Nfc Serial
            String queryAPIplants = urlAPIServer + "/plants/nfc_serial/" + nfc_serial;
            httpClient.get(queryAPIplants, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    try {
                        JSONObject response = new JSONObject(result);
                        if (response.has("detail")) {
                            nfc_plant.setText("");
                            nfc_facility.setText("");
                        } else {
                            nfc_plant.setText(response.getString("name"));
                            nfc_facility.setText(response.getString("facility"));
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
                String queryAPIparams = urlAPIServer + "/valparams/nfc_serial/" + nfc_serial;
                httpClient.get(queryAPIparams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        try {
                            JSONObject response = new JSONObject(result);
                            if (response.has("detail")) {
                                nfc_params_name.setText("");
                                nfc_params.setText("");
                            } else {
                                nfc_params_name.setText("Наим:\nMin:\nMax:\nЕд.:");
                                String textParams = response.getString("name") + "\n" +
                                        response.getString("min_value") + "\n" +
                                        response.getString("max_value") + "\n" +
                                        response.getString("unit");
                                nfc_params.setText(textParams);
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
        }
    }

    private void clearTextView() {
        nfc_contents.setText("");
        nfc_plant.setText("");
        nfc_params.setText("");
        nfc_params_name.setText("");
        nfc_facility.setText("");
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
}