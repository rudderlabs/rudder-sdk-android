package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.consent.ConsentFilter;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private static String _deviceToken;

    private static final int NUMBER_OF_FLUSH_CALLS_IN_QUEUE = 1;

    private @Nullable
    ConsentFilter consentFilter = null;
    private RudderConfig config;

    /*
     * private constructor
     * */
    private RudderClient() {
        RudderLogger.logVerbose("RudderClient: constructor invoked.");
    }

    /**
     * A handler for rejected tasks that discards the oldest unhandled request and then retries
     * execute, unless the executor is shut down, in which case the task is discarded.
     */
    final RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();

    /**
     * A single thread executor to queue up all flush requests
     */
    final ExecutorService flushExecutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(NUMBER_OF_FLUSH_CALLS_IN_QUEUE),
            handler);

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
                throw new IllegalArgumentException("RudderClient: getInstance: writeKey can not be null or empty");
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
                if (config.getSleepTimeOut() < Utils.MIN_SLEEP_TIMEOUT) {
                    RudderLogger.logVerbose("getInstance: SleepTimeOut is wrong. using default.");
                    config.setSleepTimeOut(Constants.SLEEP_TIMEOUT);
                }
            }
            // get application from provided context
            application = (Application) context.getApplicationContext();

            // initiate RudderClient instance
            instance = new RudderClient();
            instance.config = config;

            // initiate EventRepository class
            if (application != null) {
                RudderLogger.logVerbose("getInstance: creating EventRepository.");
                repository = new EventRepository(application, writeKey, config, _anonymousId, _advertisingId, _deviceToken);
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
            return;
        }
        message.setType(MessageType.TRACK);
        processMessage(message);

    }

    /**
     * Track your event with eventName
     *
     * <b>Segment compatible API</b>
     *
     * @param event Name of the event you want to track
     */
    public void track(@NonNull String event) {
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
            return;
        }
        message.setType(MessageType.SCREEN);
        processMessage(message);

    }

    /**
     * Record screen view with screen name
     *
     * <b>Segment compatible API</b>
     *
     * @param screenName Name of the screen
     */
    public void screen(@NonNull String screenName) {
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
            return;
        }
        message.setType(MessageType.IDENTIFY);
        processMessage(message);

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
        if (getOptOutStatus()) {
            return;
        }

        RudderMessage message = new RudderMessageBuilder()
                .setEventName(MessageType.IDENTIFY)
                .setUserId(traits.getId())
                .setRudderOption(option)
                .build();
        message.updateTraits(traits);
        message.updateExternalIds(option);
        identify(message);
    }

    /**
     * Identify your user
     *
     * @param traits RudderTraits object
     */
    public void identify(@NonNull RudderTraits traits) {
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
            return;
        }
        message.setType(MessageType.ALIAS);
        processMessage(message);

    }

    /**
     * Alias call
     *
     * <b>Segment compatible API</b>
     *
     * @param newId New userId for the user
     */
    public void alias(String newId) {
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
            return;
        }
        Map<String, Object> traits = getRudderContext().getTraits();

        String prevUserId = null;
        if (traits.containsKey("userId")) {
            prevUserId = (String) traits.get("userId");
        } else if (traits.containsKey("id")) {
            prevUserId = (String) traits.get("id");
        } else {
            prevUserId = RudderContext.getAnonymousId();
        }

        traits.put("userId", newId);
        traits.put("id", newId);

        RudderMessage message = new RudderMessageBuilder()
                .setUserId(newId)
                .setRudderOption(option)
                .setPreviousId(prevUserId)
                .build();

        message.updateTraits(traits);
        alias(message);
    }

    // GROUP CALLS

    /**
     * Add the user to a group
     *
     * @param builder RudderMessageBuilder
     * @deprecated Will be removed soon
     */
    @Deprecated
    public void group(@NonNull RudderMessageBuilder builder) {
        if (getOptOutStatus()) {
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
    @Deprecated
    public void group(@NonNull RudderMessage message) {
        if (getOptOutStatus()) {
            return;
        }
        message.setType(MessageType.GROUP);
        processMessage(message);
    }

    private void processMessage(@NonNull RudderMessage message) {
        RudderMessage updatedMessage = updateMessageWithConsentedDestinations(message);
        dumpMessage(updatedMessage);
    }

    private RudderMessage updateMessageWithConsentedDestinations(RudderMessage message) {
        if (consentFilter == null) {
            return message;
        }
        return applyConsentFiltersToMessage(message, consentFilter);
    }

    private @NonNull
    RudderMessage applyConsentFiltersToMessage(@NonNull RudderMessage rudderMessage, @NonNull ConsentFilter consentFilter) {
        return consentFilter.applyConsent(config, rudderMessage);
    }

    private void dumpMessage(@NonNull RudderMessage message) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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
        if (getOptOutStatus()) {
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

    /**
     * Get the auto-populated RudderContext back
     *
     * @return cached RudderContext object
     */
    public RudderContext getRudderContext() {
        if (getOptOutStatus()) {
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
     * @deprecated Will be removed soon
     */
    public static void updateWithAdvertisingId(@NonNull String advertisingId) {
        RudderClient.putAdvertisingId(advertisingId);
    }

    /**
     * Set the AdvertisingId yourself. If set, SDK will not capture idfa automatically
     *
     * <b>Call this method before initializing the RudderClient</b>
     *
     * @param advertisingId IDFA for the device
     */
    public static void putAdvertisingId(@NonNull String advertisingId) {
        if (instance == null) {
            // rudder sdk is not initialised yet. let's use the advertisingId from the beginning
            _advertisingId = advertisingId;
            return;
        }
        if (getOptOutStatus()) {
            return;
        }
        RudderElementCache.cachedContext.updateWithAdvertisingId(advertisingId);
    }

    /**
     * Set the push token for the device to be passed to the downstream destinations
     *
     * @param deviceToken Push Token from FCM
     */
    public static void putDeviceToken(@NonNull String deviceToken) {
        if (instance == null) {
            // rudder sdk is not initialised yet. let's use the deviceToken from the beginning
            _deviceToken = deviceToken;
            return;
        }
        if (getOptOutStatus()) {
            return;
        }
        RudderElementCache.cachedContext.putDeviceToken(deviceToken);
    }

    /**
     * Set the anonymousId for the device to be used further
     *
     * @param anonymousId AnonymousId you want to use for the application
     * @deprecated Will be removed soon
     */
    public static void setAnonymousId(@NonNull String anonymousId) {
        RudderClient.putAnonymousId(anonymousId);
    }

    /**
     * Set the anonymousId for the device to be used further
     *
     * @param anonymousId AnonymousId you want to use for the application
     */
    public static void putAnonymousId(@NonNull String anonymousId) {
        if (instance == null) {
            // rudder sdk is not initialised yet. let's use the anonymousId from the beginning
            _anonymousId = anonymousId;
            return;
        }
        if (getOptOutStatus()) {
            return;
        }
        if (repository != null) {
            repository.updateAnonymousId(anonymousId);
        }
    }

    /**
     * Reset SDK
     */
    public void reset() {
        RudderElementCache.reset();
        if (repository != null) {
            repository.reset();
        }
    }

    /**
     * Flush Events in async manner.
     * This calls queues the requests on {@link RudderClient#flushExecutorService}
     */
    public void flush() {
        if (getOptOutStatus()) {
            return;
        }
        if (repository != null) {
            flushExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    repository.flushSync();
                }
            });

        }
    }

    public void cancelPeriodicWorkRequest() {
        if (repository != null) {
            repository.cancelPeriodicFlushWorker();
        }
    }

    /**
     * To retrieve OptOut status
     *
     * @return true, if either SDK is not initialised or OptOut status is set to true else it return false
     */
    private static boolean getOptOutStatus() {
        if (repository == null) {
            RudderLogger.logError("SDK is not initialised. Hence dropping the event");
            return true;
        }
        if (repository.getOptStatus()) {
            RudderLogger.logDebug("User Opted out for tracking the activity, hence dropping the event");
            return true;
        }
        return false;
    }

    /**
     * Opts out a user from tracking the activity. When enabled all the events will be dropped by the SDK.
     *
     * @param optOut Boolean value to store optOut status
     */
    public void optOut(boolean optOut) {
        if (repository != null) {
            repository.saveOptStatus(optOut);
            RudderLogger.logInfo("optOut() flag is set to " + optOut);
        } else {
            RudderLogger.logError("SDK is not initialised. Hence aborting optOut API call");
        }
    }

    /**
     * Stops this instance from accepting further requests.
     */
    public void shutdown() {
        if (repository != null)
            repository.shutDown();
    }

    /**
     * Register Native SDK callback for custom implementation
     *
     * @param key      Native SDK key like Google Analytics, Amplitude, Adjust
     * @param callback RudderClient.Callback object
     */
    public void onIntegrationReady(String key, Callback callback) {
        if (getOptOutStatus()) {
            return;
        }
        if (repository != null) {
            repository.onIntegrationReady(key, callback);
        }
    }

    /**
     * @return default RudderOption object which was set on the initialization of sdk
     */
    static RudderOption getDefaultOptions() {
        return defaultOptions;
    }

    @Nullable
    public ConsentFilter getConsentFilter() {
        return consentFilter;
    }

    public void setConsentFilter(@Nullable ConsentFilter consentFilter) {
        this.consentFilter = consentFilter;
    }


    /**
     * RudderClient.Callback for getting callback when native SDK integration is ready
     */
    public interface Callback {
        void onReady(Object instance);
    }

    /**
     * Public method for start a session.
     */
    public void startSession() {
        startSession(Utils.getCurrentTimeInSecondsLong());
    }

    /**
     * Public method for start a session with a unique id.
     *
     * @param sessionId Id of a session
     */
    public void startSession(@NonNull Long sessionId) {
        if (repository == null) {
            return;
        }
        if (Long.toString(sessionId).length() < 10) {
            RudderLogger.logError("RudderClient: startSession: Length of the session Id supplied should be atleast 10, hence ignoring it");
            return;
        }
        repository.startSession(sessionId);
    }

    /**
     * Public method for end an active session.
     */
    public void endSession() {
        if (repository == null) {
            return;
        }
        repository.endSession();
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
