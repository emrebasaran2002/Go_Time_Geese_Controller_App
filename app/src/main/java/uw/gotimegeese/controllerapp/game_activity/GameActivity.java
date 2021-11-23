package uw.gotimegeese.controllerapp.game_activity;

import static uw.gotimegeese.controllerapp.Constants.*;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;

import uw.gotimegeese.controllerapp.R;
import uw.gotimegeese.controllerapp.start_activity.StartActivity;

public class GameActivity extends AppCompatActivity {

    public static BluetoothSocket serverSocket;
    public static int playerNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_layout);

        // Display the user's player number and color.
        TextView textView = findViewById(R.id.game_activity_player_textView);
        TypedArray colorArr = getResources().obtainTypedArray(R.array.player_colors);
        textView.setTextColor(colorArr.getColor(playerNumber - 1, 0));
        colorArr.recycle();
        textView.setText(getString(R.string.game_activity_player_text, playerNumber));

        // Register a listener to relay changes in the user's directional input to the server.
        DPadView dPadView = findViewById(R.id.game_activity_dPadView);
        dPadView.setListener(direction -> {
            Log.d("GameActivity", "Direction: " + direction);
            int data = CLIENT_DIR_NEUTRAL;
            if (direction == 0) {
                data = CLIENT_DIR_DOWN;
            } else if (direction == 1) {
                data = CLIENT_DIR_RIGHT;
            } else if (direction == 2) {
                data = CLIENT_DIR_UP;
            } else if (direction == 3) {
                data = CLIENT_DIR_LEFT;
            }
            sendToServer(data);
        });

        // Register a listener for the exit button.
        View exitBtn = findViewById(R.id.game_activity_exit_btn);
        exitBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        });

        // Register a listener for the pause/resume button.
        View pauseResumeBtn = findViewById(R.id.game_activity_pause_resume_btn);
        pauseResumeBtn.setOnClickListener(v -> {
            sendToServer(CLIENT_ACTION_PAUSE_RESUME);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enable "sticky immersive" fullscreen mode. We do this here instead of in onCreate()
        // so that the fullscreen mode is re-enabled if the user switches out of and then back
        // to our app.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void sendToServer(int data) {
        try {
            OutputStream out = serverSocket.getOutputStream();
            out.write(data);
        } catch (IOException ignored) {}
    }

    @Override
    protected void onDestroy() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        super.onDestroy();
    }
}
