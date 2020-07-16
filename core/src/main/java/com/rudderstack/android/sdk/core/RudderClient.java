package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Map;

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
        RudderLogger.logVerbose("RudderClient: constructor invoked.");
    }


    /**
     * API for getting RudderClient instance with bare minimum
     *
     * @param context  Your Application context
     * @param writeKey Your Android WriteKey from RudderStack dashboard
     * @return RudderClient instance to be used further
     */
    @NonNull
    public static RudderClient getInstance(@NonNull Context context, @Nullable String writeKey) {
        return getInstance(context, writeKey, new RudderConfig());
    }

    /**
     * API for getting <b>RudderClient</b> instance with custom values for settings through
     * RudderConfig.Builder
     *
     * @param context  Application context
     * @param writeKey Your Android WriteKey from RudderStack dashboard
     * @param builder  Instance of RudderConfig.Builder for customised settings
     * @return RudderClient instance to be used further
     */
    @NonNull
    public static RudderClient getInstance(@NonNull Context context, @Nullable String writeKey, @NonNull RudderConfig.Builder builder) {
        return getInstance(context, writeKey, builder.build());
    }

    /**
     * API for getting <b>RudderClient</b> instance with custom values for settings through
     * RudderConfig.Builder
     *
     * @param context  Application context
     * @param writeKey Your Android WriteKey from RudderStack dashboard
     * @param config   Instance of RudderConfig for customised settings
     * @return RudderClient instance to be used further
     */
    @NonNull
    public static RudderClient getInstance(@NonNull Context context, @Nullable String writeKey, @Nullable RudderConfig config) {
        // check if instance is already initiated
        if (instance == null) {
            RudderLogger.logVerbose("getInstance: instance null. creating instance");
            // assert writeKey is not null or empty
            if (TextUtils.isEmpty(writeKey)) {
                RudderLogger.logError("RudderClient: getInstance: writeKey can not be null or empty");
            }
            // assert config is not null
            if (config == null) {
                RudderLogger.logVerbose("getInstance: config null. creating default config");
                config = new RudderConfig();
            } else {
                RudderLogger.logVerbose("getInstance: config present. using config.");
                if (TextUtils.isEmpty(config.getDataPlaneUrl())) {
                    RudderLogger.logVerbose("getInstance: EndPointUri is blank or null. using default.");
                    config.setDataPlaneUrl(Constants.DATA_PLANE_URL);
                }
                if (config.getFlushQueueSize() < 0 || config.getFlushQueueSize() > 100) {
                    RudderLogger.logVerbose("getInstance: FlushQueueSize is wrong. using default.");
                    config.setFlushQueueSize(Constants.FLUSH_QUEUE_SIZE);
                }
                if (config.getDbCountThreshold() < 0) {
                    RudderLogger.logVerbose("getInstance: DbCountThreshold is wrong. using default.");
                    config.setDbCountThreshold(Constants.DB_COUNT_THRESHOLD);
                }
                // assure sleepTimeOut never goes below 10s
                if (config.getSleepTimeOut() < 10) {
                    RudderLogger.logVerbose("getInstance: SleepTimeOut is wrong. using default.");
                    config.setSleepTimeOut(10);
                }
            }

            // get application from provided context
            application = (Application) context.getApplicationContext();

            // initiate RudderClient instance
            instance = new RudderClient();

            // initiate EventRepository class
            if (application != null && writeKey != null) {
                RudderLogger.logVerbose("getInstance: creating EventRepository.");
                repository = new EventRepository(application, writeKey, config);
            }
        }
        return instance;
    }

    /*
     * package private api to be used in EventRepository
     * */
    @Nullable
    static RudderClient getInstance() {
        return instance;
    }

    /**
     * Create <b>RudderClient</b> with Application context
     *
     * <b>Segment compatible API</b>
     *
     * @param context Application Context
     * @return instance of RudderClient
     */
    @NonNull
    public static RudderClient with(@NonNull Context context) {
        if (instance == null) {
            String writeKey = null;
            writeKey = Utils.getWriteKeyFromStrings(context);
            return getInstance(context, writeKey);
        }
        return instance;
    }

    /**
     * @return Application context
     */
    @Nullable
    public Application getApplication() {
        return application;
    }

    /**
     * Track your event using RudderMessageBuilder
     *
     * @param builder instance of RudderMessageBuilder
     */
    public void track(@NonNull RudderMessageBuilder builder) {
        track(builder.build());
    }

    /**
     * Track your event using RudderMessage
     *
     * @param message RudderMessage you want to track. Use RudderMessageBuilder to create the message object
     */
    public void track(@NonNull RudderMessage message) {
        message.setType(MessageType.TRACK);
        if (repository != null) repository.dump(message);
    }

    /**
     * Track your event with eventName
     *
     * <b>Segment compatible API</b>
     *
     * @param event Name of the event you want to track
     */
    public void track(@NonNull String event) {
        track(new RudderMessageBuilder().setEventName(event).build());
    }

    /**
     * Track your event with name and properties
     *
     * <b>Segment compatible API</b>
     *
     * @param event    Name of the event you want to track
     * @param property RudderProperty object you want to pass with the track call
     */
    public void track(@NonNull String event, @Nullable RudderProperty property) {
        track(new RudderMessageBuilder().setEventName(event).setProperty(property).build());
    }

    /**
     * Track your event
     *
     * <b>Segment compatible API</b>
     *
     * @param event    Name of the event you want to track
     * @param property RudderProperty object you want to pass with the track call
     * @param option   Options related to this track call
     */
    public void track(@NonNull String event, @Nullable RudderProperty property, @Nullable RudderOption option) {
        track(new RudderMessageBuilder()
                .setEventName(event)
                .setProperty(property)
                .setRudderOption(option)
                .build());
    }

    /**
     * Record screen view with RudderMessageBuilder
     *
     * @param builder instance of RudderMessageBuilder
     */
    public void screen(@NonNull RudderMessageBuilder builder) {
        screen(builder.build());
    }

    /**
     * Record screen view with RudderMessage
     *
     * @param message instance of RudderMessage
     */
    public void screen(@NonNull RudderMessage message) {
        message.setType(MessageType.SCREEN);
        if (repository != null) repository.dump(message);
    }

    /**
     * Record screen view with screen name
     *
     * <b>Segment compatible API</b>
     *
     * @param screenName Name of the screen
     */
    public void screen(@NonNull String screenName) {
        RudderProperty property = new RudderProperty();
        property.put("name", screenName);
        screen(new RudderMessageBuilder().setEventName(screenName).setProperty(property).build());
    }

    /**
     * Record screen view with screen name and object
     *
     * @param screenName Name of the screen
     * @param property   RudderProperty object you want to pass with the screen call
     */
    public void screen(@NonNull String screenName, @Nullable RudderProperty property) {
        if (property == null) property = new RudderProperty();
        property.put("name", screenName);
        screen(new RudderMessageBuilder().setEventName(screenName).setProperty(property).build());
    }

    /**
     * Record screen view
     *
     * @param screenName Name of the screen
     * @param category   Name of the category of the screen
     * @param property   RudderProperty object you want to pass with the screen call
     * @param option     Options related to this screen call
     */
    public void screen(@NonNull String screenName, @NonNull String category, @Nullable RudderProperty property, @Nullable RudderOption option) {
        if (property == null) property = new RudderProperty();
        property.put("category", category);
        property.put("name", screenName);
        screen(new RudderMessageBuilder().setEventName(screenName).setProperty(property).setRudderOption(option).build());
    }

    /**
     * Record screen view
     *
     * @param screenName Name of the screen
     * @param property   RudderProperty object you want to pass with the screen call
     * @param option     Options related to this screen call
     */
    public void screen(@NonNull String screenName, @Nullable RudderProperty property, @Nullable RudderOption option) {
        screen(new RudderMessageBuilder()
                .setEventName(screenName)
                .setProperty(property)
                .setRudderOption(option)
                .build());
    }

    /**
     * Identify your user
     *
     * @param message instance of RudderMessage
     */
    public void identify(@NonNull RudderMessage message) {
        // update cached traits and persist
        RudderElementCache.updateTraits(message.getTraits());
        RudderElementCache.persistTraits();

        // set message type to identify
        message.setType(MessageType.IDENTIFY);

        // dump to repository
        if (repository != null) repository.dump(message);
    }

    /**
     * Identify your user
     *
     * <b>Segment compatible API</b>
     *
     * @param traits RudderTraits object
     * @param option RudderOption object
     */
    public void identify(@NonNull RudderTraits traits, @Nullable RudderOption option) {
        RudderMessage message = new RudderMessageBuilder()
                .setEventName(MessageType.IDENTIFY)
                .setUserId(traits.getId())
                .setRudderOption(option)
                .build();
        message.updateTraits(traits);
        identify(message);
    }

    /**
     * Identify your user
     *
     * @param traits RudderTraits object
     */
    public void identify(@NonNull RudderTraits traits) {
        identify(traits, null);
    }

    /**
     * Identify your user
     *
     * @param builder RudderTraitsBuilder object
     */
    public void identify(@NonNull RudderTraitsBuilder builder) {
        identify(builder.build());
    }

    /**
     * Identify your user
     *
     * <b>Segment compatible API</b>
     *
     * @param userId userId of your User
     */
    public void identify(@NonNull String userId) {
        identify(new RudderTraitsBuilder().setId(userId));
    }

    /**
     * Identify your user
     *
     * <b>Segment compatible API</b>
     *
     * @param userId userId of your user
     * @param traits Other user properties using RudderTraits class
     * @param option Extra options using RudderOption class
     */
    public void identify(@NonNull String userId, @Nullable RudderTraits traits, @Nullable RudderOption option) {
        // create new traits object from cache if supplied traits is null
        if (traits == null) traits = new RudderTraits();
        traits.putId(userId);
        identify(traits, option);
    }

    //ALIAS

    /**
     * Alias call
     *
     * @param builder RudderMessage.Builder
     */
    public void alias(@NonNull RudderMessageBuilder builder) {
        alias(builder.build());
    }

    /**
     * Alias call
     *
     * @param message RudderMessage
     */
    void alias(@NonNull RudderMessage message) {
        message.setType(MessageType.ALIAS);
        if (repository != null) repository.dump(message);
    }

    /**
     * Alias call
     *
     * <b>Segment compatible API</b>
     *
     * @param newId New userId for the user
     */
    public void alias(String newId) {
        alias(newId, null);
    }

    /**
     * Alias call
     *
     * <b>Segment compatible API</b>
     *
     * @param newId  New userId for the user
     * @param option RudderOptions for this event
     */
    public void alias(@NonNull String newId, @Nullable RudderOption option) {
        Map<String, Object> traits = getRudderContext().getTraits();
        RudderMessageBuilder builder = new RudderMessageBuilder()
                .setUserId(newId)
                .setRudderOption(option);

        String prevUserId = null;
        if (traits.containsKey("userId")) {
            prevUserId = (String) traits.get("userId");
        } else if (traits.containsKey("anonymousId")) {
            prevUserId = (String) traits.get("anonymousId");
        }
        if (prevUserId != null) {
            builder.setPreviousId(prevUserId);
        }
        traits.put("userId", newId);
        traits.put("id", newId);
        RudderMessage message = builder.build();
        message.updateTraits(traits);

        RudderElementCache.updateTraits(traits);
        RudderElementCache.persistTraits();

        alias(message);
    }

    // GROUP CALLS

    /**
     * Add the user to a group
     *
     * @param builder RudderMessageBuilder
     */
    public void group(@NonNull RudderMessageBuilder builder) {
        group(builder.build());
    }

    /**
     * Add the user to a group
     *
     * @param message RudderMessage
     */
    public void group(@NonNull RudderMessage message) {
        message.setType(MessageType.GROUP);
        if (repository != null) repository.dump(message);
    }

    /**
     * Add the user to a group
     *
     * <b>Segment compatible API</b>
     *
     * @param groupId Group ID you want your user to attach to
     */
    public void group(@NonNull String groupId) {
        group(groupId, null);
    }

    /**
     * Add the user to a group
     *
     * <b>Segment compatible API</b>
     *
     * @param groupId Group ID you want your user to attach to
     * @param traits  Traits of the group
     */
    public void group(@NonNull String groupId, @Nullable RudderTraits traits) {
        group(groupId, traits, null);
    }

    /**
     * Add the user to a group
     *
     * <b>Segment compatible API</b>
     *
     * @param groupId Group ID you want your user to attach to
     * @param traits  Traits of the group
     * @param option  Options for this group call
     */
    public void group(@NonNull String groupId, @Nullable RudderTraits traits, @Nullable RudderOption option) {
        RudderMessage message = new RudderMessageBuilder()
                .setGroupId(groupId)
                .setGroupTraits(traits)
                .setRudderOption(option)
                .build();
        group(message);

    }

    /**
     * Set your RudderClient instance
     *
     * <b>Segment compatible API</b>
     *
     * @param _instance RudderClient instance
     */
    public static void setSingletonInstance(@NonNull RudderClient _instance) {
        instance = _instance;
    }

    /**
     * Get the auto-populated RudderContext back
     *
     * @return cached RudderContext object
     */
    public RudderContext getRudderContext() {
        return RudderElementCache.getCachedContext();
    }

    /**
     * Reset SDK
     */
    public void reset() {
        RudderElementCache.reset(application);
        if (repository != null) repository.reset();
    }

    /**
     * Flush Events
     */
    public void flush() {
        if (repository != null) repository.flush();
    }

    /**
     * Register Native SDK callback for custom implementation
     *
     * @param key      Native SDK key like Google Analytics, Amplitude, Adjust
     * @param callback RudderClient.Callback object
     */
    public void onIntegrationReady(String key, Callback callback) {
        if (repository != null) repository.onIntegrationReady(key, callback);
    }

    public void optOut() {
        if (repository != null) repository.optOut();
    }

    /**
     * RudderClient.Callback for getting callback when native SDK integration is ready
     */
    public interface Callback {
        void onReady(Object instance);
    }

    public void shutdown() {
        if (repository != null) repository.shutdown();
    }

    /*
     * RudderClient.Builder for building RudderClient with context, writeKey, endPointUrl
     * */
    public static class Builder {
        private Application application;
        private String writeKey;

        /**
         * @param context Your Application context
         */
        public Builder(Context context) {
            this(context, null);
        }

        /**
         * @param context  Your Application context
         * @param writeKey Your Android WriteKey from RudderStack dashboard
         */
        public Builder(Context context, String writeKey) {
            if (context == null) {
                RudderLogger.logError("context can not be null");
            }

            if (TextUtils.isEmpty(writeKey)) {
                RudderLogger.logDebug("WriteKey is not specified in constructor. looking for string file");
                if (context != null) {
                    writeKey = Utils.getWriteKeyFromStrings(context);
                }
            }

            if (TextUtils.isEmpty(writeKey)) {
                RudderLogger.logError("WriteKey can not be null or empty");
            }

            if (context != null) {
                this.application = (Application) context.getApplicationContext();
            }
            this.writeKey = writeKey;
        }

        private boolean trackLifecycleEvents = false;

        /**
         * @return get your builder back
         */
        public Builder trackApplicationLifecycleEvents() {
            if (this.config == null) {
                this.config = new RudderConfig();
            }
            this.config.setTrackLifecycleEvents(true);
            return this;
        }

        private boolean recordScreenViews = false;

        /**
         * @return get your builder back
         */
        public Builder recordScreenViews() {
            if (this.config == null) {
                this.config = new RudderConfig();
            }
            this.config.setRecordScreenViews(true);
            return this;
        }

        private RudderConfig config;

        /**
         * @param config instance of your RudderConfig
         * @return get your builder back
         */
        public Builder withRudderConfig(RudderConfig config) {
            if (this.config != null) {
                RudderLogger.logWarn("RudderClient: Builder: replacing old config");
            }
            this.config = config;
            return this;
        }

        /**
         * @param builder instance of your RudderConfig.Builder
         * @return get your builder back
         */
        public Builder withRudderConfigBuilder(RudderConfig.Builder builder) {
            if (this.config != null) {
                RudderLogger.logWarn("RudderClient: Builder: replacing old config");
            }
            this.config = builder.build();
            return this;
        }

        private int logLevel;

        /**
         * @param logLevel set how much log SDK should generate
         *                 Permitted values :
         *                 VERBOSE = 5;
         *                 DEBUG = 4;
         *                 INFO = 3;
         *                 WARN = 2;
         *                 ERROR = 1;
         *                 NONE = 0;
         * @return get your builder back
         */
        public Builder logLevel(int logLevel) {
            if (this.config == null) {
                this.config = new RudderConfig();
            }
            this.config.setLogLevel(logLevel);
            return this;
        }

        /**
         * @return build your RudderClient to be used
         */
        public @Nullable
        RudderClient build() {
            if (this.application == null) {
                RudderLogger.logError("Context is null. Aborting initialization. Returning null Client");
                return null;
            }
            if (TextUtils.isEmpty(this.writeKey)) {
                RudderLogger.logError("writeKey is null. Aborting initialization. Returning null Client");
                return null;
            }
            return getInstance(this.application, this.writeKey, this.config);
        }
    }
}
