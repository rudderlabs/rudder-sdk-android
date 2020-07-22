package com.rudderstack.android.sdk.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * builder for RudderElement (alias RudderEvent)
 * */
class RudderMessageBuilder {

    private String previousId = null;

    RudderMessageBuilder setPreviousId(String previousId) {
        this.previousId = previousId;
        return this;
    }

    private String eventName = null;

    RudderMessageBuilder setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    private RudderProperty property;

    RudderMessageBuilder setProperty(RudderProperty property) {
        this.property = property;
        return this;
    }

    private String groupId = null;

    RudderMessageBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    private RudderTraits groupTraits = null;

    RudderMessageBuilder setGroupTraits(RudderTraits groupTraits) {
        this.groupTraits = groupTraits;
        return this;
    }

    private Map<String, Object> option;
    private Map<String, Object> contextOption;

    RudderMessageBuilder setOption(RudderOption option) {
        if (option != null) {
            this.contextOption = new HashMap<>();
            this.contextOption.putAll(option.context());
            this.option = new HashMap<>();
            this.option.putAll(option.integrations());
        }
        return this;
    }

    RudderMessage build() {
        RudderMessage message = new RudderMessage();

        if (this.eventName != null) {
            message.setEventName(this.eventName);
        }
        if (this.property != null) {
            message.setProperty(this.property);
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
