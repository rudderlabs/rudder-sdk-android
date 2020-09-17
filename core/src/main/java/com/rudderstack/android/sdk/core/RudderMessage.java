package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class RudderMessage {
    @SerializedName("messageId")
    private String messageId = String.format(Locale.US, "%d-%s", System.currentTimeMillis(), UUID.randomUUID().toString());
    @SerializedName("channel")
    private String channel = "mobile";
    @SerializedName("context")
    private RudderContext context;
    @SerializedName("type")
    private String type;
    @SerializedName("action")
    private String action;
    @SerializedName("originalTimestamp")
    private String timestamp = Utils.getTimeStamp();
    @SerializedName("anonymousId")
    private String anonymousId;
    @SerializedName("userId")
    private String userId;
    @SerializedName("event")
    private String event;
    @SerializedName("properties")
    private Map<String, Object> properties;
    @SerializedName("userProperties")
    private Map<String, Object> userProperties;
    @SerializedName("integrations")
    private Map<String, Boolean> integrations = new HashMap<>();
    @SerializedName("destinationProps")
    private Map<String, Map> destinationProps = null;
    @SerializedName("previousId")
    private String previousId;
    @SerializedName("traits")
    private RudderTraits traits;
    @SerializedName("groupId")
    private String groupId;

    private transient RudderOption rudderOption;

    RudderMessage() {
        context = RudderElementCache.getCachedContext();
        this.anonymousId = context.getDeviceId();

        Map<String, Object> traits = context.getTraits();
        if (traits != null && traits.containsKey("id")) {
            this.userId = String.valueOf(traits.get("id"));
        }
    }

    void setPreviousId(String previousId) {
        this.previousId = previousId;
    }

    void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    void setGroupTraits(RudderTraits groupTraits) {
        this.traits = groupTraits;
    }

    void setProperty(RudderProperty property) {
        if (property != null) this.properties = property.getMap();
    }

    void setUserProperty(RudderUserProperty userProperty) {
        this.userProperties = userProperty.getMap();
    }

    void setType(String type) {
        this.type = type;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    void setEventName(String eventName) {
        this.event = eventName;
    }

    void updateTraits(RudderTraits traits) {
        this.context.updateTraits(traits);
    }

    void updateTraits(Map<String, Object> traits) {
        this.context.updateTraitsMap(traits);
    }

    /**
     * @return Name of the event tracked
     */
    @Nullable
    public String getEventName() {
        return event;
    }

    /**
     * Get the properties back as set to the event
     *
     * @return Map of String-Object
     */
    @Nullable
    public Map<String, Object> getProperties() {
        return properties;
    }

    void addIntegrationProps(String integrationKey, boolean isEnabled, Map props) {
        integrations.put(integrationKey, isEnabled);

        if (isEnabled) {
            if (destinationProps == null) destinationProps = new HashMap<>();
            destinationProps.put(integrationKey, props);
        }
    }

    /**
     * @return Type of event (track, identify, screen, group, alias)
     */
    @Nullable
    public String getType() {
        return type;
    }

    String getAction() {
        return action;
    }

    /**
     * Get your User properties for the event
     *
     * @return Map of String-Object
     */
    @Nullable
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    /**
     * @return User ID for the event
     */
    @Nullable
    public String getUserId() {
        return userId;
    }

    void setIntegrations(Map<String, Object> integrations) {
        if (integrations == null) return;
        for (String key : integrations.keySet()) {
            this.integrations.put(key, (Boolean) integrations.get(key));
        }
    }

    public Map<String, Object> getTraits() {
        return this.context.getTraits();
    }

    /**
     * @return Anonymous ID of the user
     */
    public String getAnonymousId() {
        return anonymousId;
    }

    RudderOption getRudderOption() {
        return rudderOption;
    }

    void setRudderOption(RudderOption rudderOption) {
        this.rudderOption = rudderOption;
    }

    /**
     * @return Returns message level context
     */
    @NonNull
    public RudderContext getContext() {
        return context;
    }
}
