package com.itensis.tictactoe.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itensis.tictactoe.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BTClientActivity extends AppCompatActivity {

    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_ENABLE_DISCOVERABILITY = 2;
    private final static int REQUEST_ENABLE_LOCATION = 3;
    private TextView statusText;
    private ListView devicesList;
    private final List<BluetoothDevice> foundDevices = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!foundDevices.contains(device)){
                    foundDevices.add(device);
                    List<String> names = foundDevices
                            .stream()
                            .map(BluetoothDevice::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            BTClientActivity.this,
                            android.R.layout.simple_expandable_list_item_1,
                            names
                    );
                    devicesList.setAdapter(adapter);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_client);
        statusText = findViewById(R.id.clientStatusText);
        devicesList = findViewById(R.id.deviceListView);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        enableLocation();
        setUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                statusText.setText("Bluetooth enabled");
                if(btAdapter.startDiscovery()){
                    statusText.setText("Discovering");
                }
            }
        }
    }

    private void setUp(){
        if(btAdapter != null){
            if(!btAdapter.isEnabled()){
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBt, REQUEST_ENABLE_BT);
            }else {
                if(btAdapter.startDiscovery()){
                    statusText.setText("Discovering");
                }
            }
        }
    }

    private void enableLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLE_LOCATION);
        }
    }

}