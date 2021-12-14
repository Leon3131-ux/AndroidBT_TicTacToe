package com.itensis.tictactoe.util;

public interface BTConstants {

    String uuid = "aeb97191-5cd4-4650-8955-58dfb1ba917c";
    String name = "ticTacToe";

    int MESSAGE_READ = 1;
    int MESSAGE_WRITE = 2;
    int MESSAGE_DEVICE_NAME = 3;
    int MESSAGE_STATE_CHANGE = 4;
    int MESSAGE_TOAST = 5;

    String DEVICE_NAME = "device_name";
    String TOAST_TEXT = "toast_text";

    int SERVICE_STATE_NONE = 6;
    int SERVICE_STATE_CONNECTING = 7;
    int SERVICE_STATE_CONNECTED = 8;
    int SERVICE_STATE_LISTENING = 9;

    int REQUEST_ENABLE_DISCOVERABLE = 10;

}
