package com.rudderlabs.android.sample.segment.java;

import android.app.Application;

import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderConfig;
import com.rudderlabs.android.sdk.core.RudderLogger;
import com.rudderlabs.android.sdk.core.RudderMessageBuilder;

import java.util.HashMap;
import java.util.Map;

public class MainApplication extends Application {
    private static MainApplication instance;
    private static RudderClient rudderClient;
    private static final String writeKey = "1R3JbxsqWZlbYjJlBxf0ZNWZOH6";
    private static final String endPointUrl = "https://2f0d770f.ngrok.io";

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        rudderClient = new RudderClient.Builder(this, writeKey)
                .withRudderConfig(new RudderConfig.Builder()
                        .withEndPointUri(endPointUrl)
                        .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                        .build())
                .build();

        RudderClient.with(this).onIntegrationReady("SOME_KEY", new RudderClient.Callback() {
            @Override
            public void onReady(Object instance) {

            }
        });

        RudderClient.setSingletonInstance(rudderClient);

        RudderClient client = RudderClient.getInstance(
                this,
                writeKey,
                new RudderConfig.Builder()
                        .withEndPointUri(endPointUrl)
                        .build()
        );

        Map<String,Object> properties = new HashMap<>();
        properties.put("test_key_1", "test_value_1");
        Map<String, String> childProperties = new HashMap<>();
        childProperties.put("test_child_key_1", "test_child_value_1");
        properties.put("test_key_2", childProperties);
        rudderClient.track(
                new RudderMessageBuilder()
                        .setEventName("test_track_event")
                        .setUserId("test_user_id")
                        .setProperty(properties)
                        .build()
        );
    }

    public static RudderClient getRudderClient() {
        return rudderClient;
    }

    public static MainApplication getInstance() {
        return instance;
    }
}
