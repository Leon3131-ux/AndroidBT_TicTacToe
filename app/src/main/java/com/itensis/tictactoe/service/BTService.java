package com.itensis.tictactoe.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.itensis.tictactoe.util.BTConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BTService {

    private final Handler handler;
    private final BluetoothAdapter btAdapter;
    private int currentState;
    private AcceptThread acceptThread = null;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;

    public BTService(Handler handler){
        this.handler = handler;
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.currentState = BTConstants.SERVICE_STATE_NONE;
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
            this.device = device;

            try {
                btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(BTConstants.uuid));
            }catch (IOException e){
                connectionFailed();
            }

            currentState = BTConstants.SERVICE_STATE_CONNECTING;
            updateState();
        }

        public void run(){
            try {
                btSocket.connect();
            }catch (IOException e){
                connectionFailed();
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
