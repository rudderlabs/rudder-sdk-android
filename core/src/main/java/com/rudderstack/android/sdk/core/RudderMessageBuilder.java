package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import java.util.Map;

/*
 * builder for RudderElement (alias RudderEvent)
 * */
public class RudderMessageBuilder {
    private RudderMessage parentMessage;

    public static RudderMessageBuilder from(RudderMessage message){
        RudderMessageBuilder builder =  new RudderMessageBuilder();
        builder.parentMessage = message;
        return builder;
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

    // TODO:  need to figure out to use it as integrations
    private RudderOption option;

    public RudderMessageBuilder setRudderOption(RudderOption option) {
        this.option = option;
        return this;
    }

    public RudderMessage build() {
        RudderMessage message = parentMessage== null? new RudderMessage() : new RudderMessage(parentMessage);
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
            message.setRudderOption(option);
        }

        return message;
    }
}
