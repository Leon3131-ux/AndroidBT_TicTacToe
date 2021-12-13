package com.itensis.tictactoe.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.itensis.tictactoe.R;
import com.itensis.tictactoe.util.BTConstants;

import java.io.IOException;
import java.util.UUID;

public class BTHostActivity extends AppCompatActivity {

    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView statusText;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_DISCOVERABILITY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_host);
        statusText = findViewById(R.id.hostStatusText);
        setUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                enableDiscoverability();
            }
        }
        if(requestCode == REQUEST_ENABLE_DISCOVERABILITY){
            if(resultCode != RESULT_CANCELED){
                statusText.setText("Device discoverable as: " + btAdapter.getName());
            }
        }

    }

    private void setUp(){
        if(btAdapter != null){
            if(!btAdapter.isEnabled()){
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBt, REQUEST_ENABLE_BT);
            }else {
                enableDiscoverability();
            }
        }
    }

    private void enableDiscoverability(){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABILITY);
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tempServerSocket = null;
            try {
                tempServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(BTConstants.name, UUID.fromString(BTConstants.uuid));
            } catch (IOException e) {
                //TODO error handling
                statusText.setText("Error");
            }
            serverSocket = tempServerSocket;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    //TODO error handling
                    statusText.setText("Error");
                }

                if (socket != null) {
//                    manageMyConnectedSocket(socket);
                    statusText.setText("Connected");
                    cancel();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                //TODO error handling
                statusText.setText("Error");
            }
        }

    }

}