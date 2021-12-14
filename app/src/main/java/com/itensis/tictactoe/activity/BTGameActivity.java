package com.itensis.tictactoe.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itensis.tictactoe.R;
import com.itensis.tictactoe.service.BTService;
import com.itensis.tictactoe.util.BTConstants;
import com.itensis.tictactoe.util.GameConstants;

import java.util.Random;

public class BTGameActivity extends AppCompatActivity {

    private boolean isHost;
    private BTService btService;
    private TextView statusText;
    private boolean bound;

    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == BTConstants.MESSAGE_READ){
                if(msg.arg1 == GameConstants.MESSAGE_MOVE){
                    int buttonId = getResources().getIdentifier(msg.getData().getString(GameConstants.MOVE), "id", getPackageName());
                    Button button = findViewById(buttonId);

                    button.setText("O");
                }
                if(msg.arg1 == GameConstants.MESSAGE_WIN){
                    statusText.setText("Other player won");
                }
            }
            if(msg.what == BTConstants.MESSAGE_STATE_CHANGE){
                if(msg.arg1 == BTConstants.SERVICE_STATE_NONE){
                    statusText.setText("Other player disconnected");
                }
            }
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BTService.BTServiceBinder binder = (BTService.BTServiceBinder) service;
            btService = binder.getService();
            btService.setHandler(handler);
            if(isHost){
                boolean hasFirstTurn = broadCastFirstTurn();
                updateTurnStatus(hasFirstTurn);
            }
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
        setContentView(R.layout.activity_bluetooth_game);
        statusText = findViewById(R.id.gameStatusText);

        Intent btServiceIntent = new Intent(this, BTService.class);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    public void makeMove(View v){
        if(bound){
            Button button = findViewById(v.getId());
            if(button.getText().equals("")){
                String buttonId = getResources().getResourceName(v.getId());
                String message = GameConstants.MESSAGE_MOVE + "." + buttonId.substring(buttonId.indexOf("/") + 1);
                btService.write(message.getBytes());
                button.setText("X");
                checkGame();
            }
        }else{
            Toast.makeText(BTGameActivity.this, "Illegal move", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGame(){
        if(checkHorizontal() || checkVertical() || checkDiagonal() || checkAntiDiagonal()){
            String message = GameConstants.MESSAGE_WIN + ".";
            btService.write(message.getBytes());
        }
    }

    private boolean broadCastFirstTurn(){
        boolean hostHasFirstTurn = new Random().nextBoolean();
        //TODO broadcast first turn
        return hostHasFirstTurn;
    }

    private boolean checkHorizontal(){
        for(int y = 1; y <= 3; y++){
            boolean playerOwnsRow = true;
            for(int x = 1; x <= 3; x++){
                if (playerOwnsButton(x, y)) continue;
                playerOwnsRow = false;
            }
            if(playerOwnsRow) return true;
        }
        return false;
    }

    private boolean checkVertical(){
        for(int x = 1; x <= 3; x++){
            boolean playerOwnsRow = true;
            for(int y = 1; y <= 3; y++){
                if (playerOwnsButton(x, y)) continue;
                playerOwnsRow = false;
            }
            if(playerOwnsRow) return true;
        }
        return false;
    }

    private boolean checkDiagonal(){
        int x = 1;
        int y = 1;

        for(int i = 1; i <= 3; i++){
            if (playerOwnsButton(x, y)) {
                x++;
                y++;
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean checkAntiDiagonal(){
        int x = 3;
        int y = 1;

        for(int i = 1; i <= 3; i++){
            if (playerOwnsButton(x, y)) {
                x--;
                y++;
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean playerOwnsButton(int x, int y){
        int buttonId = getResources().getIdentifier("x" + x + "y" + y, "id", getPackageName());
        Button button = findViewById(buttonId);

        return button.getText().equals("X");
    }

    private void updateTurnStatus(boolean hasTurn){
        if(hasTurn){
            statusText.setText("It's your turn");
        }else {
            statusText.setText("It's not your turn");
        }
    }

}