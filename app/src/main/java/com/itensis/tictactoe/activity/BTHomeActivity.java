package com.itensis.tictactoe.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.itensis.tictactoe.R;

public class BTHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_home);
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    public void hostGame(View v){
        Intent hostIntent = new Intent(BTHomeActivity.this, BTHostActivity.class);
        startActivity(hostIntent);
    }

    public void joinGame(View v){
        Intent clientIntent = new Intent(BTHomeActivity.this, BTClientActivity.class);
        startActivity(clientIntent);
    }

}