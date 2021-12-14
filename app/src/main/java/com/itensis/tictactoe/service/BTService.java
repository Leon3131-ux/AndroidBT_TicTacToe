package com.itensis.tictactoe.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import com.itensis.tictactoe.util.BTConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BTService extends Service {

    private Handler handler;
    private final BluetoothAdapter btAdapter= BluetoothAdapter.getDefaultAdapter();
    private int currentState = BTConstants.SERVICE_STATE_NONE;
    private AcceptThread acceptThread = null;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;
    private final IBinder binder = new BTServiceBinder();

    public class BTServiceBinder extends Binder {
        public BTService getService() {
            return BTService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public void openServerSocket(){
        cancelThreads();

        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void openConnection(BluetoothDevice device){
        cancelThreads();

        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private synchronized void connected(BluetoothSocket btSocket, BluetoothDevice device){
        cancelThreads();

        connectedThread = new ConnectedThread(btSocket);
        connectedThread.start();

        Message message = handler.obtainMessage(BTConstants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BTConstants.DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private synchronized void updateState(){
        handler.obtainMessage(BTConstants.MESSAGE_STATE_CHANGE, currentState, -1).sendToTarget();
    }

    public void write(byte[] buffer){

        ConnectedThread copy;

        synchronized (this){
            if(currentState != BTConstants.SERVICE_STATE_CONNECTED){
                return;
            }
            copy = connectedThread;
        }
        copy.write(buffer);
    }

    private void connectionLost(){
        //TODO implement logic
    }

    private void connectionFailed(){
        //TODO implement logic
    }

    private synchronized void sendToast(String text){
        Message message = handler.obtainMessage(BTConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTConstants.TOAST_TEXT, text);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private synchronized void cancelThreads(){
        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }

        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }

        if(connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        currentState = BTConstants.SERVICE_STATE_NONE;
        updateState();
    }

    private class AcceptThread extends Thread{

        private BluetoothServerSocket btServerSocket = null;

        public AcceptThread(){
            try{
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(BTConstants.name, UUID.fromString(BTConstants.uuid));
            }catch (IOException e){
                sendToast("Could not listen for connections");
            }
            currentState = BTConstants.SERVICE_STATE_LISTENING;
            updateState();
        }

        public void run(){
            BluetoothSocket btSocket;

            while (currentState != BTConstants.SERVICE_STATE_CONNECTED){
                try {
                    btSocket = btServerSocket.accept();
                }catch (IOException e){
                    sendToast("Could not accept connection");
                    break;
                }

                if(btSocket != null){
                    synchronized (BTService.this){
                        switch (currentState){
                            case BTConstants.SERVICE_STATE_CONNECTING:
                            case BTConstants.SERVICE_STATE_LISTENING:
                                connected(btSocket, btSocket.getRemoteDevice());
                                break;
                            case BTConstants.SERVICE_STATE_CONNECTED:
                            case BTConstants.SERVICE_STATE_NONE:
                                cancel();
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            if(btServerSocket != null){
                try {
                    btServerSocket.close();
                } catch (IOException e) {
                    sendToast("Could not close connection listener");
                }
            }
        }

    }

    private class ConnectThread extends Thread{

        private BluetoothSocket btSocket = null;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmp = null;

            this.device = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(BTConstants.uuid));
            }catch (IOException e){
                connectionFailed();
            }

            btSocket = tmp;

            currentState = BTConstants.SERVICE_STATE_CONNECTING;
            updateState();
        }

        public void run(){
            try {
                btSocket.connect();
            }catch (IOException e){
                connectionFailed();
            }

            synchronized (BTService.this) {
                connectThread = null;
            }

            connected(btSocket, device);
        }

        public void cancel(){
            if(btSocket != null){
                try {
                    btSocket.close();
                } catch (IOException e) {
                    sendToast("Could not close connector");
                }
            }
        }

    }

    private class ConnectedThread extends Thread{

        private final BluetoothSocket btSocket;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public ConnectedThread(BluetoothSocket btSocket){
            this.btSocket = btSocket;

            try {
                inputStream = btSocket.getInputStream();
                outputStream = btSocket.getOutputStream();
            }catch (IOException e){
                sendToast("Could not open streams");
            }

            currentState = BTConstants.SERVICE_STATE_CONNECTED;
            updateState();
        }

        public void run(){

            byte[] buffer = new byte[1024];
            int bytes;

            while (currentState == BTConstants.SERVICE_STATE_CONNECTED){

                try {
                    bytes = inputStream.read(buffer);

                    handler.obtainMessage(BTConstants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer){
            try {
                outputStream.write(buffer);

                handler.obtainMessage(BTConstants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            }catch (IOException e){
                sendToast("Could not send message");
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                btSocket.close();
            }catch (IOException e){
                sendToast("Could not close connection");
            }
        }
    }

}
