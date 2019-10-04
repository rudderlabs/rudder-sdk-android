package com.rudderlabs.android.sdk.core;

import android.opengl.EGLObjectHandle;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class RudderElement {
    @SerializedName("rl_message")
    private RudderMessage message;

    RudderElement() {
        message = new RudderMessage();
    }

    public void setType(String type) {
        message.setType(type);
    }

    public void setProperty(RudderProperty property) {
        message.setProperty(property);
    }

    public void setProperty(RudderPropertyBuilder builder) throws RudderException {
        setProperty(builder.build());
    }

    public  void setUserProperty(RudderUserProperty userProperty) {
        message.setUserProperties(userProperty);
    }

    public void setUserId(String userId) {
        message.setUserId(userId);
    }

    public void setEventName(String eventName) {
        message.setEventName(eventName);
    }

    public void identifyWithTraits(RudderTraits traits) {
        message.updateTraits(traits);
    }

    public String getEventName() {
        return message.getEventName();
    }

    public Map<String, Object> getEventProperties() {
        return message.getProperties();
    }

    void addIntegrationProps(String integrationKey, boolean isEnabled, Map props) {
        message.addIntegrationProps(integrationKey, isEnabled, props);
    }

    void setIntegrations(Map<String, Object> integrations) {
        message.setIntegrations(integrations);
    }

    public RudderMessage getMessage() {
        return message;
    }

    public String getType() {
        return message.getType();
    }

    public String getAction() {
        return message.getAction();
    }

    public Map<String, Object> getUserProperties() {
        return message.getUserProperties();
    }

    public Map<String, Object> getProperties() {
        return message.getProperties();
    }

    public RudderTraits getTraits() {
        return  message.getTraits();
    }
}