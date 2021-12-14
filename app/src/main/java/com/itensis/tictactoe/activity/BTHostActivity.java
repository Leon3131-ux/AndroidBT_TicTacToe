package com.itensis.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
    private BTService btService;
    private boolean bound;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == BTConstants.MESSAGE_STATE_CHANGE){
                if(msg.arg1 == BTConstants.SERVICE_STATE_CONNECTED){
                    Intent gameIntent = new Intent(BTHostActivity.this, BTGameActivity.class);
                    startActivity(gameIntent);
                }
                if(msg.arg1 == BTConstants.SERVICE_STATE_NONE){
                    statusText.setText("Disconnected");
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

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BTService.BTServiceBinder binder = (BTService.BTServiceBinder) service;
            btService = binder.getService();
            btService.setHandler(handler);
            btService.openServerSocket();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btService.cancelThreads();
        unbindService(connection);
        bound = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_host);
        statusText = findViewById(R.id.hostStatusText);
        enableDiscoverability();
        Intent btServiceIntent = new Intent(this, BTService.class);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
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