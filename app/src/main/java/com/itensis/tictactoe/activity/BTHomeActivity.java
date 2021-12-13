package com.itensis.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.itensis.tictactoe.R;

public class BTHomeActivity extends AppCompatActivity {

    private boolean hasRequiredPermissions = false;
    private boolean btEnabled = false;
    private static final int REQUEST_ENABLE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_home);
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_ENABLE_LOCATION);
        enableBluetooth();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_ENABLE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                hasRequiredPermissions = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == RESULT_OK){
                btEnabled = true;
            }
        }
    }

    private void enableBluetooth(){
        if(btAdapter != null){
            if(!btAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);

            }
            btEnabled = true;
        }
    }

    public void hostGame(View v){
        if(hasRequiredPermissions && btEnabled){
            Intent hostIntent = new Intent(BTHomeActivity.this, BTHostActivity.class);
            startActivity(hostIntent);
        }
    }

    public void joinGame(View v){
        if(hasRequiredPermissions && btEnabled){
            Intent clientIntent = new Intent(BTHomeActivity.this, BTClientActivity.class);
            startActivity(clientIntent);
        }
    }

}