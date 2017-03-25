package com.github.angelikaowczarek.verun.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.github.angelikaowczarek.verun.NotificationBuilder;
import com.github.angelikaowczarek.verun.activity.MainActivity;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackgroundScanService extends Service {

    public static final String TAG = "BackgroundScanService";
    public static final String ACTION_DEVICE_DISCOVERED = "DeviceDiscoveredAction";
    public static final String EXTRA_DEVICE = "DeviceExtra";

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(3000);

    private final Handler handler = new Handler();
    private ProximityManager proximityManager;
    private boolean isRunning;
    private int devicesCount;
    private List<String> beacons = new ArrayList<>();
    NotificationBuilder notificationBuilder;


    @Override
    public void onCreate() {
        super.onCreate();
        setupProximityManager();
        notificationBuilder  = new NotificationBuilder(this, new Intent(this, MainActivity.class));
        isRunning = false;
        beacons.add("N2cT");
        beacons.add("TZCI");
    }

    private void setupProximityManager() {
        proximityManager = ProximityManagerFactory.create(this);

        proximityManager.configuration()
                .scanPeriod(ScanPeriod.RANGING)
                .scanMode(ScanMode.BALANCED);

        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(createEddystoneListener());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning) {
            Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        }
        startScanning();
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
                devicesCount = 0;
                Toast.makeText(BackgroundScanService.this, "Scanning service started.", Toast.LENGTH_SHORT).show();
            }
        });
        stopAfterDelay();
    }

    private void stopAfterDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proximityManager.disconnect();
                stopSelf();
            }
        }, TIMEOUT);
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
//                onDeviceDiscovered(ibeacon);
                Log.i(TAG, "onIBeaconDiscovered: " + ibeacon.toString());
            }
        };
    }

    private EddystoneListener createEddystoneListener() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                onDeviceDiscovered(eddystone);
                Log.i(TAG, "onEddystoneDiscovered: " + eddystone.toString());
                if (beacons.contains(eddystone.getUniqueId())) {

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0, notificationBuilder.getNotification());
                }
            }
        };
    }

    private void onDeviceDiscovered(RemoteBluetoothDevice device) {
        devicesCount++;
        //Send a broadcast with discovered device
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_DISCOVERED);
        intent.putExtra(EXTRA_DEVICE, device);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (proximityManager != null) {
            proximityManager.disconnect();
            proximityManager = null;
        }
        Toast.makeText(BackgroundScanService.this, "Scanning service stopped.", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
