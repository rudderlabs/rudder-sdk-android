package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.telecom.Call;
import android.text.TextUtils;

import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.List;

/*
 * Primary class to be used in client
 * */
public class RudderClient {
    // singleton instance
    private static RudderClient instance;
    // repository instance
    private static EventRepository repository;
    private static Application application;

    /*
     * private constructor
     * */
    protected RudderClient() {
        // message constructor initialization
    }

    /*
     * API for getting instance for RudderClient with primitive types.
     *
     * ideally to be called from Unity plugin
     * */
    public static void initiateInstance(
            Context context,
            String writeKey,
            String endPointUri,
            int flushQueueSize,
            int dbCountThreshold,
            int sleepTimeout,
            boolean isDebug
    ) {
        initiateInstance(context, writeKey, endPointUri, flushQueueSize, dbCountThreshold, sleepTimeout, isDebug, null);
    }

    public static void initiateInstance(
            Context context,
            String writeKey,
            String endPointUri,
            int flushQueueSize,
            int dbCountThreshold,
            int sleepTimeout,
            boolean isDebug,
            List<RudderIntegration.Factory> factories
    ) {
        try {
            if (instance == null) {
                RudderConfigBuilder configBuilder = new RudderConfigBuilder()
                        .withEndPointUri(endPointUri)
                        .withFlushQueueSize(flushQueueSize)
                        .withDbThresholdCount(dbCountThreshold)
                        .withSleepCount(sleepTimeout)
                        .withDebug(isDebug);

                if (factories != null) configBuilder.withFactories(factories);

                application = (Application) context.getApplicationContext();
                instance = getInstance(application, writeKey, configBuilder.build());
            }
        } catch (Exception e) {
            RudderLogger.logError(e.getCause());
        }
    }

    /*
     * API to get the instance. It can return null if not initialized.
     * to be called after `initiateInstance` is called
     *
     * ideally to be called for testing Unity plugin
     * */
    public static RudderClient getInstance() {
        return instance;
    }

    /*
     * API to log messages with only basic objects instead of RudderObjects
     *
     * ideally to be called from Unity plugin
     * */
    public static void logEvent(
            String type,
            String messageName,
            String userId,
            String messagePropertiesJson,
            String userPropertiesJson,
            String integrationsJson
    ) {
        RudderMessage element = new RudderMessageBuilder()
                .setEventName(messageName)
                .setUserId(userId)
                .setProperty(Utils.convertToMap(messagePropertiesJson))
                .setUserProperty(Utils.convertToMap(userPropertiesJson))
                .build();
        element.setIntegrations(Utils.convertToMap(integrationsJson));
        element.setType(type);
        repository.dump(element);
    }

    /*
     * API for getting instance of RudderClient with context and writeKey (bare minimum
     * requirements)
     * */
    public static RudderClient getInstance(Context context, String writeKey) throws RudderException {
        return getInstance(context, writeKey, new RudderConfig());
    }

    /*
     * API for getting instance of RudderClient with config
     * */
    public static RudderClient getInstance(Context context, String writeKey,
                                           RudderConfigBuilder builder) throws RudderException {
        return getInstance(context, writeKey, builder.build());
    }

    /*
     * API for getting instance of RudderClient with config
     * */
    public static RudderClient getInstance(Context context, String writeKey, RudderConfig config)
            throws RudderException {
        // check if instance is already initiated
        if (instance == null) {
            // assert context is not null
            if (context == null) {
                throw new RudderException("context can not be null");
            }
            // assert writeKey is not null or empty
            if (TextUtils.isEmpty(writeKey)) {
                throw new RudderException("WriteKey can not be null or empty");
            }
            // assert config is not null
            if (config == null) {
                throw new RudderException("config can not be null");
            }

            application = (Application) context.getApplicationContext();
            // initiate RudderClient instance
            instance = new RudderClient();
            // get application context from provided context
            Application application = (Application) context.getApplicationContext();
            // initiate EventRepository class
            repository = new EventRepository(application, writeKey, config);
        }
        return instance;
    }

    /*
     * segment equivalent API
     * */
    public static RudderClient with(Context context) throws RudderException {
        if (context == null) {
            throw new RudderException("Context must not be null");
        }

        if (instance == null) {
            String writeKey = Utils.getWriteKeyFromStrings(context);
            return getInstance(context, writeKey);
        }

        return instance;
    }

    public Application getApplication() {
        return application;
    }

    /*
     * method for `track` messages
     * */
    public void track(RudderMessageBuilder builder) {
        track(builder.build());
    }

    public void track(RudderMessage message) {
        message.setType(MessageType.TRACK);
        repository.dump(message);
    }

    /*
     * segment equivalent API
     * */
    public void track(String event) {
        track(new RudderMessageBuilder().setEventName(event).build());
    }

    public void track(String event, RudderProperty property) {
        track(new RudderMessageBuilder().setEventName(event).setProperty(property).build());
    }

    public void track(String event, RudderProperty property, RudderOption option) {
        track(new RudderMessageBuilder()
                .setEventName(event)
                .setProperty(property)
                .setRudderOption(option)
                .build());
    }

    /*
     * method for `screen` messages
     * */
    public void screen(RudderMessageBuilder builder) {
        screen(builder.build());
    }

    public void screen(RudderMessage message) {
        message.setType(MessageType.SCREEN);
        repository.dump(message);
    }

    /*
     * segment equivalent API
     * */
    public void screen(String event) {
        screen(new RudderMessageBuilder().setEventName(event).build());
    }

    public void screen(String event, RudderProperty property) {
        screen(new RudderMessageBuilder().setEventName(event).setProperty(property).build());
    }

    public void screen(String event, String category, RudderProperty property, RudderOption option) {
        if (property == null) property = new RudderProperty();
        property.setProperty("category", category);

        screen(new RudderMessageBuilder().setEventName(event).setProperty(property).setRudderOption(option).build());
    }

    public void screen(String event, RudderProperty property, RudderOption option) {
        screen(new RudderMessageBuilder()
                .setEventName(event)
                .setProperty(property)
                .setRudderOption(option)
                .build());
    }

    /*
     * method for `page` messages
     * */
    public void page(RudderMessageBuilder builder) {
        page(builder.build());
    }

    public void page(final RudderMessage message) {
        message.setType(MessageType.PAGE);
        repository.dump(message);
    }

    /*
     * method for `identify` messages
     * */
    public void identify(RudderMessage message) {
        repository.dump(message);
    }

    public void identify(RudderTraits traits, RudderOption option) {
        RudderMessage message = new RudderMessageBuilder()
                .setEventName(MessageType.IDENTIFY)
                .setUserId(traits.getId())
                .setRudderOption(option)
                .build();
        message.updateTraits(traits);
        message.setType(MessageType.IDENTIFY);
        identify(message);
    }

    public void identify(RudderTraits traits) {
        identify(traits, null);
    }

    public void identify(RudderTraitsBuilder builder) {
        identify(builder.build());
    }

    public void identify(String userId) {
        identify(new RudderTraitsBuilder().setId(userId));
    }

    /*
     * segment equivalent API
     * */
    public void alias(String event) {
        alias(event, null);
    }

    public void alias(String event, RudderOption option) {
        // TODO:  yet to be decided
    }

    public void group(String groupId) {
        group(groupId, null);
    }

    public void group(String groupId, RudderTraits traits) {
        group(groupId, traits, null);
    }

    public void group(String groupId, RudderTraits traits, RudderOption option) {
        // TODO:  yet to be decided
    }

    /*
     * segment equivalent API
     * */
    public static void setSingletonInstance(RudderClient _instance) {
        if (_instance != null) instance = _instance;
    }

    public RudderContext getRudderContext() {
        return  RudderElementCache.getCachedContext();
    }

    public EventRepository getSnapShot() {
        return repository;
    }

    public void reset() {
        repository.reset();
    }

    public void optOut() {
        repository.optOut();
    }

    public <T> void onIntegrationReady(String key, Callback<T> callback) {
        repository.onIntegrationReady(key, callback);
    }

    public interface Callback<T> {
        void onReady(T instance);
    }

    public void shutdown() {
        repository.shutdown();
    }

    /*
     * segment equivalent API
     * */
    public static class Builder {
        private Application application;
        private String writeKey;

        public Builder(Context context, String writeKey) {
            this.application = (Application) context.getApplicationContext();
            this.writeKey = writeKey;
        }

        private boolean trackLifecycleEvents = false;

        public Builder trackApplicationLifecycleEvents() {
            this.trackLifecycleEvents = true;
            return this;
        }

        private boolean recordScreenViews = false;

        public Builder recordScreenViews() {
            this.recordScreenViews = true;
            return this;
        }

        private RudderConfig config;

        public Builder withRudderConfig(RudderConfig config) {
            this.config = config;
            return this;
        }

        public Builder withRudderConfigBuilder(RudderConfigBuilder builder) throws RudderException {
            this.config = builder.build();
            return this;
        }

        public RudderClient build() throws RudderException {
            return getInstance(this.application, this.writeKey, this.config);
        }
    }
}
