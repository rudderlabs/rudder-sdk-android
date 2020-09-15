package com.rudderstack.android.sdk.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RudderOption {
    private Map<String, Object> integrations;
    private Map<String, Object> context;

    private static final String ALL_INTEGRATIONS_KEY = "All";

    /*
     * constructor
     * */
    public RudderOption() {
        this.integrations = new ConcurrentHashMap<>();
        this.context = new ConcurrentHashMap<>();
    }

    /**
     * API for enabling a particular integration
     *
     * @param integrationKey Name of the integration
     * @param enabled Whether the integration is enabled
     *
     * @return RudderOption instance to be used further
     */
    @NonNull
    public RudderOption setIntegration(@NonNull String integrationKey, boolean enabled) {
        this.integrations.put(integrationKey, enabled);
        return this;
    }

    /**
     * API for passing extra options for a particular integration
     *
     * @param integrationKey Name of the integration
     * @param options Extra options you want to pass for this integration
     *
     * @return RudderOption instance to be used further
     */
    @NonNull
    public RudderOption setIntegrationOptions(@NonNull String integrationKey, @NonNull Map<String, Object> options) {
        this.integrations.put(integrationKey, options);
        return this;
    }

    /**
     * API for passing extra values for the context
     *
     * @param key Name of the property
     * @param value Value for the property for the integration
     *
     * @return RudderOption instance to be used further
     */
    @NonNull
    public RudderOption putContext(@NonNull String key, @NonNull Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Get the list of integrations passed  along with RudderOption
     *
     * @return List of integrations passed with RudderOption
     */
    @NonNull
    public Map<String, Object> integrations() {
        return new HashMap<>(integrations);
    }

    /**
     * Get the context passed  along with RudderOption
     *
     * @return List of integrations passed with RudderOption
     */
    @NonNull
    public Map<String, Object> context() {
        return new HashMap<>(context);
    }
}
