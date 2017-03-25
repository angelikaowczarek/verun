package com.github.angelikaowczarek.verun.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angelikaowczarek.verun.R;
import com.github.angelikaowczarek.verun.service.BackgroundScanService;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private Button startScanBtn;
    private Intent serviceIntent;
    private TextView beaconInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        setupButton();
        serviceIntent = new Intent(getApplicationContext(), BackgroundScanService.class);
        beaconInfo = (TextView) findViewById(R.id.beacon_info);
    }

    private void startBackgroundService() {
        startService(serviceIntent);
    }

    //Since Android Marshmallow starting a Bluetooth Low Energy scan requires permission from location group.
    private void checkPermissions() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult) {
            //Permission not granted so we ask for it. Results are handled in onRequestPermissionsResult() callback.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_CODE_PERMISSIONS == requestCode) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            disableButton();
            Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
        }
    }

    private void setupButton() {
        startScanBtn = (Button) findViewById(R.id.start_scan_button);
        startScanBtn.setOnClickListener(this);
    }

    private void disableButton() {
        startScanBtn.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        startBackgroundService();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Register Broadcast receiver that will accept results from background scanning
        IntentFilter intentFilter = new IntentFilter(BackgroundScanService.ACTION_DEVICE_DISCOVERED);
        registerReceiver(scanningBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(scanningBroadcastReceiver);
        super.onPause();
    }

    private final BroadcastReceiver scanningBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Device discovered!
            int devicesCount = intent.getIntExtra(BackgroundScanService.EXTRA_DEVICES_COUNT, 0);
            RemoteBluetoothDevice device = intent.getParcelableExtra(BackgroundScanService.EXTRA_DEVICE);
            beaconInfo.setText(String.format("Total discovered devices: %d\n\nLast scanned device:\n%s", devicesCount, device.toString()));
        }
    };
}
