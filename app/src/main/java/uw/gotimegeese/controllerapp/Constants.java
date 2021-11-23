package uw.gotimegeese.controllerapp;

import java.util.UUID;

public class Constants {

    // The UUID for the server.
    public static final UUID SERVER_UUID = UUID.fromString("2cafd5f6-ea6c-44c4-99bf-5629bdbcab1d");

    // This is the response we expect to receive from the server if the game we're trying to join
    // is already full.
    public static final int SERVER_GAME_FULL_RESPONSE = 255;

    public static final int CLIENT_DIR_UP = 1;
    public static final int CLIENT_DIR_DOWN = 2;
    public static final int CLIENT_DIR_LEFT = 3;
    public static final int CLIENT_DIR_RIGHT = 4;
    public static final int CLIENT_DIR_NEUTRAL = 5;

    public static final int CLIENT_ACTION_PAUSE_RESUME = 200;
}
