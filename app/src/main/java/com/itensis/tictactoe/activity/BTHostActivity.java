package com.itensis.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.itensis.tictactoe.R;
import com.itensis.tictactoe.service.BTService;
import com.itensis.tictactoe.util.BTConstants;

import java.io.IOException;
import java.util.UUID;

public class BTHostActivity extends AppCompatActivity {

    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView statusText;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == BTConstants.MESSAGE_STATE_CHANGE){
                if(msg.arg1 == BTConstants.SERVICE_STATE_CONNECTED){
                    statusText.setText("Connected");
                }
            }
            if(msg.what == BTConstants.MESSAGE_DEVICE_NAME){
                statusText.setText("Connected to: " + msg.getData().getString(BTConstants.DEVICE_NAME));
            }

            if(msg.what == BTConstants.MESSAGE_TOAST){
                Toast.makeText(BTHostActivity.this, msg.getData().getString(BTConstants.TOAST_TEXT), Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_host);
        statusText = findViewById(R.id.hostStatusText);
        enableDiscoverability();
        BTService btService = new BTService(handler);
        btService.openServerSocket();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BTConstants.REQUEST_ENABLE_DISCOVERABLE){
            if(resultCode != RESULT_CANCELED){
                statusText.setText("Device discoverable as: " + btAdapter.getName() + "\n" + btAdapter.getAddress());
            }
        }

    }

    private void enableDiscoverability(){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, BTConstants.REQUEST_ENABLE_DISCOVERABLE);
    }

}