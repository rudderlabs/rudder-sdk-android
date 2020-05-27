package com.rudderstack.android.integration.dummy;

import android.app.Application;
import android.content.Context;

import org.json.JSONObject;

public class DummyGAIntegration {
    private static DummyGAIntegration instance;

    private Application application;
    private String key;

    private DummyGAIntegration(Context context, String key) {
        this.application = (Application) context.getApplicationContext();
        this.key = key;
    }

    public static DummyGAIntegration getInstance(Context context, String key) {
        try {
            // to simulate initialization time
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (instance == null) {
            instance = new DummyGAIntegration(context, key);

            System.out.println("DummyGAIntegration is initialized with key " + key);
        }
        return instance;
    }
    
    public void dumpEvent(String eventName, String eventType, String userId, JSONObject eventProps, JSONObject userProps) {
        System.out.println(eventName + " event has been dumped with DummyGAIntegration");
    }
}
