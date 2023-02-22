package com.example.checkup_android;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CheckNFCTagActivity extends AppCompatActivity {
    public static final String Error_Detected = "No NFC Tag Detected";
    public static final String Write_Sucsess = "Text Written Successfull";
    public static final String Write_Error = "Error during Writing, Try Again!";

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] writingTagFilters;
    boolean writeMode;
    Tag myTag;
    TextView nfc_contents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_nfctag);

        nfc_contents =  (TextView) findViewById(R.id.nfc_contents);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[] { tagDetected };

    }
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();


//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // skv
//            nfc_contents.setText(String.valueOf(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())));
//            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {

            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

                Toast.makeText(this,"NFC on resume working",Toast.LENGTH_LONG).show();
                byte[] tagId = getIntent().getByteArrayExtra(NfcAdapter.EXTRA_ID);
//                Log.i("EHEHEHEHEHE",tagId + "");
                String text = byteArrayToHex(tagId);
                nfc_contents.setText(text);
            }
//        }

    }

    private String byteArrayToHex(byte[] a) {
//    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }

    // Enable Write
    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
    }

    // Disable Write
    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}