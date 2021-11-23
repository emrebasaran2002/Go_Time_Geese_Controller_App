package uw.gotimegeese.controllerapp.start_activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private BroadcastReceiver bluetoothReceiver;

    private FrameLayout frameLayout;

    private boolean isBluetoothEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare the main FrameLayout we will be populating with UI elements depending on the
        // activity's state.
        frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15,
                getResources().getDisplayMetrics()));
        frameLayout.setPadding(padding, padding, padding, padding);
        setContentView(frameLayout);

        bluetoothReceiver = new BluetoothReceiver();
        registerReceiver(bluetoothReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        isBluetoothEnabled = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE))
                .getAdapter().isEnabled();
        refreshUI();
    }

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Safeguard against unexpected intents as per the documentation recommends.
            if (intent == null ||
                    !BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                Log.w("MainActivity", "Unexpected Intent received: " + intent);
                return;
            }

            // Refresh the UI only if the Bluetooth availability actually changed.
            int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (bluetoothState == BluetoothAdapter.STATE_ON && !isBluetoothEnabled) {
                isBluetoothEnabled = true;
                refreshUI();
            } else if (bluetoothState != BluetoothAdapter.STATE_ON && isBluetoothEnabled) {
                isBluetoothEnabled = false;
                refreshUI();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }

    private void refreshUI() {
        frameLayout.removeAllViews();
        View view = isBluetoothEnabled
                ? new BluetoothAvailableView(this)
                : new BluetoothDisabledView(this);
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(lp);
        frameLayout.addView(view);
    }
}


