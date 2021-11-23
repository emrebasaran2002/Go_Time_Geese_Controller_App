package uw.gotimegeese.controllerapp.start_activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import uw.gotimegeese.controllerapp.R;

public class BluetoothDisabledView extends FrameLayout {

    public BluetoothDisabledView(@NonNull Context context) {
        super(context);
        View.inflate(context, R.layout.bluetooth_disabled_view, this);
        Button button = findViewById(R.id.bluetooth_disabled_view_button);
        button.setOnClickListener(v -> {
            getContext().startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        });
    }
}
