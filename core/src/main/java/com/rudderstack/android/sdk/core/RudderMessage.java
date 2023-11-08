package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RudderMessage {
    @SerializedName("messageId")
    private String messageId = UUID.randomUUID().toString();
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
    private Map<String, Object> integrations = new HashMap<>();
    @SerializedName("destinationProps")
    private Map<String, Map> destinationProps = null;
    @SerializedName("previousId")
    private String previousId;
    @SerializedName("traits")
    private RudderTraits traits;
    @SerializedName("groupId")
    private String groupId;

    private transient RudderOption rudderOption;
    private transient Map<String, Object> customContexts;

    RudderMessage() {
        this.context = RudderElementCache.getCachedContext();
        this.anonymousId = RudderContext.getAnonymousId();

        if (context != null) {
            Map<String, Object> traits = context.getTraits();
            if (traits != null && traits.containsKey("id")) {
                this.userId = String.valueOf(traits.get("id"));
            }
        }
    }

    RudderMessage(@NonNull RudderMessage message) {

        this.messageId = message.messageId;
        this.channel = message.channel;
        this.context = message.context;
        this.type = message.type;
        this.action = message.action;
        this.timestamp = message.timestamp;
        this.anonymousId = message.anonymousId;
        this.userId = message.userId;
        this.event = message.event;
        this.properties = message.properties;
        this.userProperties = message.userProperties;
        this.integrations = message.integrations;
        this.destinationProps = message.destinationProps;
        this.previousId = message.previousId;
        this.traits = message.traits;
        this.groupId = message.groupId;
        this.rudderOption = message.rudderOption;
        this.customContexts = message.customContexts;
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
        RudderElementCache.updateTraits(traits);
        updateContext();
    }

    void updateTraits(Map<String, Object> traits) {
        RudderElementCache.updateTraits(traits);
        updateContext();
    }

    void updateExternalIds(RudderOption option) {
        if (option != null) {
            List<Map<String, Object>> externalIds = option.getExternalIds();
            if (externalIds != null && !externalIds.isEmpty()) {
                RudderElementCache.updateExternalIds(externalIds);
                updateContext();
            }
        }
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
            this.integrations.put(key, integrations.get(key));
        }
    }

    void setCustomContexts(Map<String, Object> customContexts) {
        if (customContexts == null) return;
        this.customContexts = customContexts;
        if(this.context != null) {
            this.context.setCustomContexts(customContexts);
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
        if (rudderOption != null) {
            setIntegrations(rudderOption.getIntegrations());
            setCustomContexts(rudderOption.getCustomContexts());
        }
    }

    /**
     * @return Returns message level context
     */
    @NonNull
    public RudderContext getContext() {
        return context;
    }

    void updateContext() {
        this.context = RudderElementCache.getCachedContext();
        if (this.customContexts != null && this.context != null)
            this.context.setCustomContexts(this.customContexts);
    }

    /**
     * @return Group ID for the event
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * @return Integrations Map passed for the event
     */
    @NonNull
    public Map<String, Object> getIntegrations() {
        return this.integrations;
    }


    void setSession(RudderUserSession userSession) {
        this.context.setSession(userSession);
    }

    @VisibleForTesting
    String getMessageId () {
        return this.messageId;
    }
}
