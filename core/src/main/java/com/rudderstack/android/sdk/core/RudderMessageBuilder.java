package com.rudderstack.android.sdk.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * builder for RudderElement (alias RudderEvent)
 * */
public class RudderMessageBuilder {

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
    private Map<String, Object> option;

    public RudderMessageBuilder setRudderOption(Map<String, Object> option) {
        this.option = new HashMap<String,Object>();
        this.option.putAll(option);
        return this;
    }

    private Map<String, Object> contextOption;

    public RudderMessageBuilder setContextOption(Map<String, Object> contextOption) {
        this.contextOption = new HashMap<String,Object>();
        this.contextOption.putAll(contextOption);
        return this;
    }


    public RudderMessage build() {
        RudderMessage event = new RudderMessage();
        if (this.userId != null) event.setUserId(this.userId);
        if (this.eventName != null) event.setEventName(this.eventName);
        if (this.property != null) event.setProperty(this.property);
        if (this.userProperty != null) event.setUserProperty(this.userProperty);
        if (this.previousId != null) event.setPreviousId(this.previousId);
        if (this.groupId != null) event.setGroupId(this.groupId);
        if (this.groupTraits != null) event.setGroupTraits(this.groupTraits);
        if (this.option != null) event.setRudderOption(this.option);
        if (this.contextOption != null) event.setContextOption(this.contextOption);

        return event;
    }
}
