package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.List;
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
    private static String _advertisingId;
    private static String _anonymousId;
    private static RudderOption defaultOptions;
    private static boolean _isOptedOut;

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
     * RudderConfig
     *
     * @param context  Application context
     * @param writeKey Your Android WriteKey from RudderStack dashboard
     * @param config   Instance of RudderConfig for customised settings
     * @param option   Instance of RudderOption for customizing integrations to which events to be sent
     * @return RudderClient instance to be used further
     */
    @NonNull
    public static RudderClient getInstance(@NonNull Context context, @Nullable String writeKey, @NonNull RudderConfig config, @Nullable RudderOption option) {
        defaultOptions = option;
        return getInstance(context, writeKey, config);
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
                repository = new EventRepository(application, writeKey, config, _anonymousId, _advertisingId);
                _isOptedOut = repository.getOptStatus();
            }
        }
        return instance;
    }

    /*
     * package private api to be used in EventRepository
     * */
    @Nullable
    public static RudderClient getInstance() {
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
    public static Application getApplication() {
        return application;
    }

    /**
     * Track your event using RudderMessageBuilder
     *
     * @param builder instance of RudderMessageBuilder
     * @deprecated Will be removed soon
     */
    public void track(@NonNull RudderMessageBuilder builder) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        track(builder.build());
    }

    /**
     * Track your event using RudderMessage
     *
     * @param message RudderMessage you want to track. Use RudderMessageBuilder to create the message object
     * @deprecated Will be removed soon
     */
    public void track(@NonNull RudderMessage message) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        message.setType(MessageType.TRACK);
        if (repository != null) {
            repository.dump(message);
        }
    }

    /**
     * Track your event with eventName
     *
     * <b>Segment compatible API</b>
     *
     * @param event Name of the event you want to track
     */
    public void track(@NonNull String event) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
     * @deprecated Will be removed soon
     */
    public void screen(@NonNull RudderMessageBuilder builder) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        screen(builder.build());
    }

    /**
     * Record screen view with RudderMessage
     *
     * @param message instance of RudderMessage
     * @deprecated Will be removed soon
     */
    public void screen(@NonNull RudderMessage message) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        message.setType(MessageType.SCREEN);
        if (repository != null) {
            repository.dump(message);
        }
    }

    /**
     * Record screen view with screen name
     *
     * <b>Segment compatible API</b>
     *
     * @param screenName Name of the screen
     */
    public void screen(@NonNull String screenName) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        if (property == null) {
            property = new RudderProperty();
        }
        property.put("name", screenName);
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
     * @deprecated Will be removed soon
     */
    public void identify(@NonNull RudderMessage message) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        // update cached traits and persist
        RudderElementCache.updateTraits(message.getTraits());
        RudderElementCache.persistTraits();

        //  handle external Ids
        RudderOption option = message.getRudderOption();
        if (option != null) {
            List<Map<String, Object>> externalIds = option.getExternalIds();
            if (externalIds != null && !externalIds.isEmpty()) {
                RudderElementCache.updateExternalIds(externalIds);
            }
        }

        // set message type to identify
        message.setType(MessageType.IDENTIFY);
        message.updateContext();

        // dump to repository
        if (repository != null) {
            repository.dump(message);
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        identify(traits, null);
    }

    /**
     * Identify your user
     *
     * @param builder RudderTraitsBuilder object
     */
    public void identify(@NonNull RudderTraitsBuilder builder) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        // create new traits object from cache if supplied traits is null
        if (traits == null) {
            traits = new RudderTraits();
        }
        traits.putId(userId);
        identify(traits, option);
    }

    //ALIAS

    /**
     * Alias call
     *
     * @param builder RudderMessage.Builder
     * @deprecated Will be removed soon
     */
    public void alias(@NonNull RudderMessageBuilder builder) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        alias(builder.build());
    }

    /**
     * Alias call
     *
     * @param message RudderMessage
     * @deprecated Will be removed soon
     */
    void alias(@NonNull RudderMessage message) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        message.setType(MessageType.ALIAS);
        if (repository != null) {
            repository.dump(message);
        }
    }

    /**
     * Alias call
     *
     * <b>Segment compatible API</b>
     *
     * @param newId New userId for the user
     */
    public void alias(String newId) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        Map<String, Object> traits = getRudderContext().getTraits();
        RudderMessageBuilder builder = new RudderMessageBuilder()
                .setUserId(newId)
                .setRudderOption(option);

        String prevUserId = null;
        if (traits.containsKey("userId")) {
            prevUserId = (String) traits.get("userId");
        } else if (traits.containsKey("id")) {
            prevUserId = (String) traits.get("id");
        } else {
            prevUserId = RudderContext.getAnonymousId();
        }

        builder.setPreviousId(prevUserId);
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
     * @deprecated Will be removed soon
     */
    public void group(@NonNull RudderMessageBuilder builder) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        group(builder.build());
    }

    /**
     * Add the user to a group
     *
     * @param message RudderMessage
     * @deprecated Will be removed soon
     */
    public void group(@NonNull RudderMessage message) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        message.setType(MessageType.GROUP);
        if (repository != null) {
            repository.dump(message);
        }
    }

    /**
     * Add the user to a group
     *
     * <b>Segment compatible API</b>
     *
     * @param groupId Group ID you want your user to attach to
     */
    public void group(@NonNull String groupId) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
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

    // Comment: Need to discuss whether to block or not
    /**
     * Get the auto-populated RudderContext back
     *
     * @return cached RudderContext object
     */
    public RudderContext getRudderContext() {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence you cannot retrieve the context object");
            return null;
        }
        return RudderElementCache.getCachedContext();
    }

    /**
     * Set the AdvertisingId yourself. If set, SDK will not capture idfa automatically
     *
     * <b>Call this method before initializing the RudderClient</b>
     *
     * @param advertisingId IDFA for the device
     */
    public static void updateWithAdvertisingId(@Nullable String advertisingId) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence you cannot update his advertising Id");
            return;
        }
        if (instance == null) {
            // rudder sdk is not intialized yet. let's use the advertisingId from the beginning
            _advertisingId = advertisingId;
        } else {
            // update the advertisingId
            RudderElementCache.cachedContext.updateWithAdvertisingId(advertisingId);
        }
    }

    /**
     * Set the push token for the device to be passed to the downstream destinations
     *
     * @param deviceToken Push Token from FCM
     */
    public void putDeviceToken(@Nullable String deviceToken) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence you cannot set his device token");
            return;
        }
        RudderElementCache.cachedContext.putDeviceToken(deviceToken);
    }

    /**
     * Set the anonymousId for the device to be used further
     *
     * @param anonymousId AnonymousId you want to use for the application
     */
    public static void setAnonymousId(String anonymousId) {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence you cannot set his anonymousId");
            return;
        }
        if (instance != null) {
            RudderLogger.logWarn("Set the anonymousId before calling getInstance");
            return;
        }
        _anonymousId = anonymousId;
    }

    // Comment: Need to discuss whether reset should be blocked or not
    /**
     * Reset SDK
     */
    public void reset() {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        RudderElementCache.reset();
        if (repository != null) {
            repository.reset();
        }
    }

    // Comment: Need to discuss whether reset should be blocked or not
    /**
     * Flush Events
     */
    public void flush() {
        if (_isOptedOut) {
            RudderLogger.logDebug("User Opted out for tracking his activity, hence dropping the events");
            return;
        }
        if (repository != null) {
            repository.flush();
        }
    }

    /**
     * Opts out a user from tracking his activity. When enabled all the events will be dropped by the SDK.
     */
    public void optOut(@NonNull boolean optOut) {
        if (repository != null) {
            reset();
            flush();
            _isOptedOut = optOut;
            repository.saveOptStatus(optOut);
        }
    }

    // Comment: Maybe we can ignore this as this is not related to user activity.
    /**
     * Register Native SDK callback for custom implementation
     *
     * @param key      Native SDK key like Google Analytics, Amplitude, Adjust
     * @param callback RudderClient.Callback object
     */
    public void onIntegrationReady(String key, Callback callback) {
        if (repository != null) {
            repository.onIntegrationReady(key, callback);
        }
    }

    // Comment: Maybe we can ignore this as this is not related to user activity. Need to discuss as some part of it doesn't contain user data and some contains
    /**
     * @return default RudderOption object which was set on the initialization of sdk
     */
    static RudderOption getDefaultOptions() {
        return defaultOptions;
    }

    /**
     * RudderClient.Callback for getting callback when native SDK integration is ready
     */
    public interface Callback {
        void onReady(Object instance);
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
