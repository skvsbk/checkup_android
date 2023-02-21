package com.example.checkup_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {
    private static final String PREFS_FILE = "Settings";
    private static final String PREF_URLAPI = "UrlAPI";

    EditText editTextAPIServer;
    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        editTextAPIServer = findViewById(R.id.editTextAPIServer);

        //Get saved url
        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        String urlAPIServer = settings.getString(PREF_URLAPI, "");
        editTextAPIServer.setText(urlAPIServer);
    }

    public void onClickSave(View v) {
        //save url
        String urlAPIServer = editTextAPIServer.getText().toString();
        prefEditor = settings.edit();
        prefEditor.putString(PREF_URLAPI, urlAPIServer);
        prefEditor.apply();

        //open MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}