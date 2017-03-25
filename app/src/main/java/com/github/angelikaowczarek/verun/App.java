package com.github.angelikaowczarek.verun;

import android.app.Application;

import com.kontakt.sdk.android.common.KontaktSDK;

/**
 * Created by angelika on 25.03.17.
 */

public class App extends Application {

    private static final String API_KEY = "uUouzlNcUXSTUllIPfknVFhocxnHWJho";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeDependencies();
    }

    private void initializeDependencies() {
        KontaktSDK.initialize(API_KEY);
    }
}
