package com.rudderstack.android.sdk.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RudderOption {

    private Map<String, Object> integrations;
    private Map<String, Object> context;

    public static final String ALL_INTEGRATIONS_KEY = "All";

    public RudderOption() {
        integrations = new ConcurrentHashMap<>();
        context = new ConcurrentHashMap<>();
    }

    public RudderOption setIntegration(String integrationKey, boolean enabled) {
        integrations.put(integrationKey, enabled);
        return this;
    }

    public RudderOption setIntegrationOptions(String integrationKey, Map<String, Object> options) {
        integrations.put(integrationKey, options);
        return this;
    }

    public RudderOption putContext(String key, Object value) {
        context.put(key, value);
        return this;
    }

    public Map<String, Object> integrations() {
        return new LinkedHashMap<>(integrations);
    }

    public Map<String, Object> context() {
        return new LinkedHashMap<>(context);
    }

}
