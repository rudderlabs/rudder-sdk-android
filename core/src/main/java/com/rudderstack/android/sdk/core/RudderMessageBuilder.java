package com.rudderstack.android.sdk.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * builder for RudderElement (alias RudderEvent)
 * */
public class RudderMessageBuilder {

    private String previousId = null;

    public RudderMessageBuilder setPreviousId(String previousId) {
        this.previousId = previousId;
        return this;
    }

    private String eventName = null;

    public RudderMessageBuilder setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    private String userId = null;

    /**
     * @param userId Developer identification
     * @return Builder instance
     *
     * @deprecated : use identify method instead
     */
    public RudderMessageBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    private RudderProperty property;

    public RudderMessageBuilder setProperty(RudderProperty property) {
        this.property = property;
        return this;
    }

    public RudderMessageBuilder setProperty(RudderPropertyBuilder builder) {
        this.property = builder.build();
        return this;
    }

    public RudderMessageBuilder setProperty(Map<String, Object> map) {
        if (this.property == null) property = new RudderProperty();
        property.putValue(map);
        return this;
    }

    private RudderUserProperty userProperty;

    public RudderMessageBuilder setUserProperty(RudderUserProperty userProperty) {
        this.userProperty = userProperty;
        return this;
    }

    public RudderMessageBuilder setUserProperty(Map<String, Object> map) {
        this.userProperty = new RudderUserProperty();
        userProperty.putValue(map);
        return this;
    }

    private String groupId = null;

    public RudderMessageBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    private RudderTraits groupTraits = null;

    public RudderMessageBuilder setGroupTraits(RudderTraits groupTraits) {
        this.groupTraits = groupTraits;
        return this;
    }

    // TODO:  need to figure out to use it as integrations
    private Map<String, Object> option;
    private Map<String, Object> contextOption;

    public RudderMessageBuilder setOption(RudderOption option) {
        if (option != null) {
            this.contextOption = new HashMap<>();
            this.contextOption.putAll(option.context());
            this.option = new HashMap<>();
            this.option.putAll(option.integrations());
        }
        return this;
    }

    public RudderMessage build() {
        RudderMessage message = new RudderMessage();

        if (this.userId != null) {
            message.setUserId(this.userId);
        }
        if (this.eventName != null) {
            message.setEventName(this.eventName);
        }
        if (this.property != null) {
            message.setProperty(this.property);
        }
        if (this.userProperty != null) {
            message.setUserProperty(this.userProperty);
        }
        if (this.previousId != null) {
            message.setPreviousId(this.previousId);
        }
        if (this.groupId != null) {
            message.setGroupId(this.groupId);
        }
        if (this.groupTraits != null) {
            message.setGroupTraits(this.groupTraits);
        }
        if (this.option != null) {
            message.setRudderOption(this.option);
        }
        if (this.contextOption != null) {
            message.setContextOption(this.contextOption);
        }

        return message;
    }
}
