package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import com.rudderlabs.android.sdk.core.util.Utils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        // prevent constructor initialization
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
     * API to log events with only basic objects instead of RudderObjects
     *
     * ideally to be called from Unity plugin
     * */
    public static void logEvent(
            String type,
            String eventName,
            String userId,
            String eventPropertiesJson,
            String userPropertiesJson,
            String integrationsJson
    ) {
        RudderElement element = new RudderElementBuilder()
                .setEventName(eventName)
                .setUserId(userId)
                .setProperty(Utils.convertToMap(eventPropertiesJson))
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

    public Application getApplication() {
        return application;
    }

    /*
     * method for `track` events
     * */
    public void track(RudderElementBuilder builder) {
        track(builder.build());
    }

    public void track(RudderElement event) {
        event.setType(MessageType.TRACK);
        repository.dump(event);
    }

    /*
     * method for `screen` events
     * */
    public void screen(RudderElementBuilder builder) {
        screen(builder.build());
    }

    public void screen(final RudderElement event) {
        event.setType(MessageType.SCREEN);
        repository.dump(event);
    }

    /*
     * method for `page` events
     * */
    public void page(RudderElementBuilder builder) {
        page(builder.build());
    }

    public void page(final RudderElement event) {
        event.setType(MessageType.PAGE);
        repository.dump(event);
    }

    /*
     * method for `identify` events
     * */
    public void identify(final RudderElement event) {
        repository.dump(event);
    }

    public void identify(RudderTraits traits) {
        RudderElement event = new RudderElementBuilder()
                .setEventName(MessageType.IDENTIFY)
                .setUserId(traits.getId())
                .build();
        event.identifyWithTraits(traits);
        event.setType(MessageType.IDENTIFY);
        identify(event);
    }

    public void identify(RudderTraitsBuilder builder) {
        identify(builder.build());
    }
}
