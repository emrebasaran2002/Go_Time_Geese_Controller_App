package uw.gotimegeese.controllerapp.start_activity;

import static uw.gotimegeese.controllerapp.Constants.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Set;

import uw.gotimegeese.controllerapp.R;
import uw.gotimegeese.controllerapp.game_activity.GameActivity;

public class BluetoothAvailableView extends FrameLayout {

    private final BluetoothAdapter bluetoothAdapter;

    private final Handler handler;

    private final TextView textView;
    private final ScrollView scrollView;
    private final RadioGroup radioGroup;
    private final Button connectButton, refreshButton;

    public BluetoothAvailableView(@NonNull Context context) {
        super(context);

        handler = new Handler(Looper.getMainLooper());
        bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        View.inflate(context, R.layout.bluetooth_available_view, this);
        textView = findViewById(R.id.bluetooth_available_view_textView);
        scrollView = findViewById(R.id.bluetooth_available_view_scrollView);
        radioGroup = findViewById(R.id.bluetooth_available_view_radioGroup);
        connectButton = findViewById(R.id.bluetooth_available_view_connect_button);
        refreshButton = findViewById(R.id.bluetooth_available_view_refresh_button);

        refreshButton.setOnClickListener(v -> {
            refresh();
            Toast.makeText(context, R.string.refresh_complete_toast, Toast.LENGTH_SHORT).show();
        });

        connectButton.setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId != -1) {
                View checkedView = radioGroup.findViewById(checkedId);
                BluetoothDevice device = (BluetoothDevice) checkedView.getTag();
                connectToDevice(device);
            }
        });

        // When the checked item is updated, we enable (or disable if the selection was cleared)
        // the connect button.
        radioGroup.setOnCheckedChangeListener((rg, checkedId) -> {
            connectButton.setEnabled(checkedId != -1);
        });

        refresh();
    }

    private void refresh() {
        // Remove all the items in the RadioGroup. Start fresh :)
        radioGroup.removeAllViews();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            textView.setText(R.string.bluetooth_available_no_devices_message);
            scrollView.setVisibility(View.GONE);
        } else {
            // Add a RadioButton for every device. Keep a reference to the relevant BluetoothDevice
            // object in each RadioButton.
            for (BluetoothDevice device : pairedDevices) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(device.getName());
                radioButton.setTag(device);
                radioGroup.addView(radioButton);
            }

            textView.setText(R.string.bluetooth_available_select_game_message);
            scrollView.setVisibility(VISIBLE);
        }

        // Disable the connect button regardless of what happened as the user's selection will
        // be cleared as a result of the refresh.
        connectButton.setEnabled(false);
    }

    /**
     * Attempts to connect to the specified device as a client using {@code Constants.SERVER_UUID}.
     * A {@code ProgressDialog} will be shown during the connection process to prevent the user
     * from interacting with the UI.
     * If successful, the present activity will be finished and {@code GameActivity} will be
     * launched. Otherwise, a toast message will be shown to the user explaining what went wrong.
     */
    private void connectToDevice(@NonNull BluetoothDevice device) {
        // First, show a ProgressDialog to prevent the user from interacting with the UI.
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getContext()
                .getString(R.string.connecting_to_device_dialog_message, device.getName()));
        progressDialog.show();

        new Thread(() -> {
            boolean connectionSuccessful = false;
            BluetoothSocket socket = null;

            try {
                // Connect to the target device.
                socket = device.createRfcommSocketToServiceRecord(SERVER_UUID);
                socket.connect();

                // Read one byte through the socket's input stream. The information we receive here
                // will allow us to determine if we were accepted into the game.
                int readResult;
                InputStream in = socket.getInputStream();
                do {
                    readResult = in.read();
                } while (readResult == -1);

                if (readResult == SERVER_GAME_FULL_RESPONSE) {
                    handler.post(() -> {
                        Toast.makeText(getContext(), R.string.game_is_full_toast,
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    connectionSuccessful = true;
                    GameActivity.serverSocket = socket;
                    GameActivity.playerNumber = readResult;
                    handler.post(() -> {
                        getContext().startActivity(new Intent(getContext(), GameActivity.class));
                        ((Activity) getContext()).finish();
                    });
                }
            } catch (IOException ex) {
                Log.e("Bluetooth", Log.getStackTraceString(ex));
                handler.post(() -> {
                    Toast.makeText(getContext(), R.string.unable_to_connect_to_device_toast,
                            Toast.LENGTH_SHORT).show();
                });
            }

            // If the connection was unsuccessful, close the socket.
            if (!connectionSuccessful && socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }

            // We directly close the ProgressDialog here instead of posting a runnable on the
            // handler because the documentation states dismiss() can be called from any thread.
            progressDialog.dismiss();
        }).start();
    }
}
