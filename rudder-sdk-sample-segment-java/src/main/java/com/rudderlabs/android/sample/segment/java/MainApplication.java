package com.rudderlabs.android.sample.segment.java;

import android.app.Application;

import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderConfig;
import com.rudderlabs.android.sdk.core.RudderLogger;

public class MainApplication extends Application {
    private static MainApplication instance;
    private static RudderClient rudderClient;
    private static final String writeKey = "1S0ibSaDlBDkaQuHLi9feJqIUBN";
    private static final String endPointUrl = "https://0aae0ad7.ngrok.io";

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        rudderClient = new RudderClient.Builder(this, writeKey)
                .logLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withRudderConfig(new RudderConfig.Builder().withEndPointUri(endPointUrl).build())
                .build();

        RudderClient.with(this).onIntegrationReady("SOME_KEY", new RudderClient.Callback() {
            @Override
            public void onReady(Object instance) {

            }
        });

        RudderClient.setSingletonInstance(rudderClient);
    }

    public static RudderClient getRudderClient() {
        return rudderClient;
    }

    public static MainApplication getInstance() {
        return instance;
    }
}
