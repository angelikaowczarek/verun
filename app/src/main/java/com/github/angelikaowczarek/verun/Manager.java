package com.github.angelikaowczarek.verun;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by angelika on 25.03.17.
 */

public class Manager {

    private ProximityManager proximityManager;
    private AppCompatActivity activity;
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> uids = new ArrayList<>();

    public Manager(AppCompatActivity activity) {
        this.activity = activity;
        configureProximityManager();
        setListeners();
        startScan();
    }

    private void configureProximityManager() {
        proximityManager = ProximityManagerFactory.create(activity);
        proximityManager.configuration()
                .rssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                .resolveShuffledInterval(3)
                .scanMode(ScanMode.BALANCED)
                .eddystoneFrameTypes(EnumSet.of(EddystoneFrameType.URL))
                .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(5)))
                .activityCheckConfiguration(ActivityCheckConfiguration.DEFAULT);
    }

    private void setListeners() {
        proximityManager.setEddystoneListener(new SimpleEddystoneListener()
        {
            @Override public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace)
            {
                String url = eddystone.getUrl();
                urls.add(url);
                String uid = eddystone.getUniqueId();
                uids.add(uid);
//                adapter.updateUrls(urls, uids);
                //Dumping urls to the logs
            }
        });
    }


    private void startScan() {
        proximityManager.connect(new OnServiceReadyListener()
        {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();

            }
        });
    }
}
