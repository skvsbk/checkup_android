package com.example.checkup_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    Button button_check_nfc, button_link_nfc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        button_check_nfc = findViewById(R.id.button_check_nfc);
        button_link_nfc = findViewById(R.id.button_link_nfc);

//        Intent intent = new Intent(this, LinkNFCTagActivity.class);
//        button_check_nfc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                startActivity(intent);
//
//            }
//        });

//        public void onClickCheckNFC(View v) {
//            Intent intent = new Intent(this, CheckNFCTagActivity.class);
//            startActivity(intent);
//        }



    }
    public void onClickLinkNFC (View v){
        Intent intent = new Intent(this, LinkNFCTagActivity.class);
        startActivity(intent);
    }

    public void onClickCheckNFC (View v){
        Intent intent = new Intent(this, CheckNFCTagActivity.class);
        startActivity(intent);
    }
}

