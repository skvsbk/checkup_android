package com.example.checkup_android;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

    TextView textView21, textView19;
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

        fillRecyclerView();
        setRecyclerView();

        // NFC
        textView21 = findViewById(R.id.textView21);
        textView19 = findViewById(R.id.textView19);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        readFromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
                        checksList.add(new Checks(plant_name, plant_id, nfc_serial, null, val_name, val_min, val_max, unit_name));
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
            textView21.setText(nfc_serial);

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
        // Collapse current
        Checks checkCurrent = checksList.get(position);
        checkCurrent.setExpandable(false);
        checkCurrent.setSend(true);
        checksAdapter.notifyItemChanged(position);

        int nextPos = position+1;
        // Expand next
        if (nextPos < checksList.size()) {
            checksList.get(nextPos).setExpandable(true);
            vars.setIntVars("current_position", nextPos);
//                        vars.setStrVars("current_nfc", nfcLinkedTxt.getText().toString());
            checksAdapter.notifyItemChanged(nextPos);

        } else {
        // put time_finish to checkup_headers table and open RoutersAvtivity
            Toast.makeText(getApplicationContext(), "Обход завершен", Toast.LENGTH_SHORT).show();
        }

    }
}