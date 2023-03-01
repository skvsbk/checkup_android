package com.example.checkup_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    Button button_check_nfc, button_link_nfc, button_add_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        button_check_nfc = findViewById(R.id.button_check_nfc);
        button_link_nfc = findViewById(R.id.button_link_nfc);
        button_add_place = findViewById(R.id.button_add_place);

        button_check_nfc.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdmCheckNFCTagActivity.class);
            startActivity(intent);
        });

        button_link_nfc.setOnClickListener((View view) -> {
            Intent intent = new Intent(this, AdmLinkNFCTagActivity.class);
            startActivity(intent);
        });

        button_add_place.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdmAddPlaceActivity.class);
            startActivity(intent);
        });
    }
}

