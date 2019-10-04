package com.rudderlabs.android.sdk.core;

import com.google.gson.annotations.SerializedName;
import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.*;

class RudderMessage {
    @SerializedName("rl_message_id")
    private String messageId = String.format(Locale.US, "%d-%s", System.currentTimeMillis(), UUID.randomUUID().toString());
    @SerializedName("rl_channel")
    private String channel = "android-sdk";
    @SerializedName("rl_context")
    private RudderContext context;
    @SerializedName("rl_type")
    private String type;
    @SerializedName("rl_action")
    private String action;
    @SerializedName("rl_timestamp")
    private String timestamp = Utils.getTimeStamp();
    @SerializedName("rl_anonymous_id")
    private String anonymousId;
    @SerializedName("rl_user_id")
    private String userId;
    @SerializedName("rl_event")
    private String event;
    @SerializedName("rl_properties")
    private Map<String, Object> properties;
    @SerializedName("rl_user_properties")
    private Map<String, Object> userProperties;
    @SerializedName("rl_integrations")
    private Map<String, Boolean> integrations = new HashMap<>();
    @SerializedName("rl_destination_props")
    private Map<String, Map> destinationProps = new HashMap<>();

    RudderMessage() {
        context = RudderElementCache.getCachedContext();
        this.anonymousId = context.getDeviceId();
    }

    void setProperty(RudderProperty property) {
        if (property != null) this.properties = property.getMap();
    }

    void setUserProperties(RudderUserProperty userProperty) {
        this.userProperties = userProperty.getMap();
    }

    void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventName(String eventName) {
        this.event = eventName;
    }

    void updateTraits(RudderTraits traits) {
        this.context.updateTraits(traits);
    }

    String getEventName() {
        return event;
    }

    Map<String, Object> getProperties() {
        return properties;
    }

    void addIntegrationProps(String integrationKey, boolean isEnabled, Map props) {
        integrations.put(integrationKey, isEnabled);
        if (isEnabled) destinationProps.put(integrationKey, props);
    }

    String getType() {
        return type;
    }

    String getAction() {
        return action;
    }

    Map<String, Object> getUserProperties() {
        return userProperties;
    }

    RudderTraits getTraits() {
        return context.getTraits();
    }

    void setIntegrations(Map<String, Object> integrations) {
        if (integrations == null) {
            return;
        }
        for (String key : integrations.keySet()) {
            this.integrations.put(key, (Boolean) integrations.get(key));
        }
    }
}
