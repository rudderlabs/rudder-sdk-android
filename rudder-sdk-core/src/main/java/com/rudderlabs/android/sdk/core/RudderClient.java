package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.rudderlabs.android.sdk.core.util.Utils;

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
    private RudderClient() {
        // message constructor initialization
    }

    /*
     * API for getting instance of RudderClient with context and writeKey (bare minimum
     * requirements)
     * */
    public static RudderClient getInstance(Context context, String writeKey) {
        return getInstance(context, writeKey, new RudderConfig());
    }

    /*
     * API for getting instance of RudderClient with config
     * */
    public static RudderClient getInstance(Context context, String writeKey,
                                           RudderConfig.Builder builder) {
        return getInstance(context, writeKey, builder.build());
    }

    /*
     * API for getting instance of RudderClient with config
     * */
    public static RudderClient getInstance(Context context, String writeKey, RudderConfig config) {
        // check if instance is already initiated
        if (instance == null) {
            // assert context is not null
            if (context == null) {
                RudderLogger.logError("context can not be null");
            }
            // assert writeKey is not null or empty
            if (TextUtils.isEmpty(writeKey)) {
                RudderLogger.logError("writeKey can not be null or empty");
            }
            // assert config is not null
            if (config == null) {
                config = new RudderConfig();
            } else {
                if (TextUtils.isEmpty(config.getEndPointUri())) {
                    config.setEndPointUri(Constants.BASE_URL);
                }
                if (config.getFlushQueueSize() < 0 || config.getFlushQueueSize() > 100) {
                    config.setFlushQueueSize(Constants.FLUSH_QUEUE_SIZE);
                }
                if (config.getDbCountThreshold() < 0) {
                    config.setDbCountThreshold(Constants.DB_COUNT_THRESHOLD);
                }
                // assure sleepTimeOut never goes below 10s
                if (config.getSleepTimeOut() < 10) {
                    config.setSleepTimeOut(10);
                }
            }

            // get application from provided context
            if (context != null) {
                application = (Application) context.getApplicationContext();
            }
            // initiate RudderClient instance
            instance = new RudderClient();
            // initiate EventRepository class
            if (application != null && writeKey != null) {
                repository = new EventRepository(application, writeKey, config);
            }
        }
        return instance;
    }

    /*
     * package private api to be used in EventRepository
     * */
    static RudderClient getInstance() {
        return instance;
    }

    /*
     * segment equivalent API
     * */
    public static RudderClient with(Context context) {
        if (context == null) {
            RudderLogger.logError("Context must not be null");
        }

        if (instance == null) {
            String writeKey = null;
            if (context != null) {
                writeKey = Utils.getWriteKeyFromStrings(context);
            }
            return getInstance(context, writeKey);
        }

        return instance;
    }

    /*
     * segment equivalent API
     * */
    public static RudderClient with(Context context) {
        if (context == null) {
            RudderLogger.logError("Context must not be null");
        }

        if (instance == null) {
            String writeKey = null;
            if (context != null) {
                writeKey = Utils.getWriteKeyFromStrings(context);
            }
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
        if (repository != null) repository.dump(message);
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
        if (repository != null) repository.dump(message);
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
        property.put("category", category);

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
        if (repository != null) repository.dump(message);
    }

    /*
     * method for `identify` messages
     * */
    public void identify(RudderMessage message) {
        if (repository != null) repository.dump(message);
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

    public void identify(String userId, RudderTraits traits, RudderOption option) {
        traits.putId(userId);
        identify(traits, option);
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
        return RudderElementCache.getCachedContext();
    }

    public EventRepository getSnapShot() {
        return repository;
    }

    public void reset() {
        if (repository != null) repository.reset();
    }

    public void optOut() {
        if (repository != null) repository.optOut();
    }

    public <T> void onIntegrationReady(String key, Callback callback) {
        if (repository != null) repository.onIntegrationReady(key, callback);
    }

    public interface Callback {
        void onReady(Object instance);
    }

    public void shutdown() {
        if (repository != null) repository.shutdown();
    }

    /*
     * segment equivalent API
     * */
    public static class Builder {
        private Application application;
        private String writeKey;

        public Builder(Context context, String writeKey) {
            if (context == null) {
                RudderLogger.logError("context can not be null");
                return;
            }

            if (TextUtils.isEmpty(writeKey)) {
                writeKey = Utils.getWriteKeyFromStrings(context);
            }

            if (TextUtils.isEmpty(writeKey)) {
                RudderLogger.logError("writeKey can not be null or empty");
                return;
            }
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

        public Builder withRudderConfigBuilder(RudderConfig.Builder builder) {
            this.config = builder.build();
            return this;
        }

        private int logLevel;
        public Builder logLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public RudderClient build() {
            return getInstance(this.application, this.writeKey, this.config);
        }
    }
}
