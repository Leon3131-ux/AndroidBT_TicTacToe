package com.itensis.tictactoe.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.itensis.tictactoe.R;
import com.itensis.tictactoe.service.BTService;

import java.util.Random;

public class BTGameActivity extends AppCompatActivity {

    private boolean isHost;
    private BTService btService;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_game);
        statusText = findViewById(R.id.statusText);

        if(isHost){
            boolean hasFirstTurn = broadCastFirstTurn();
            updateTurnStatus(hasFirstTurn);
        }
    }

    public void makeMove(View v){
        //TODO implement logic using view.id
    }

    private void checkGame(){
        if(checkHorizontal() || checkVertical() || checkDiagonal() || checkAntiDiagonal()){
            //TODO broadcast win
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