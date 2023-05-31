package com.rudderstack.android.ruddermetricsreporterandroid;


import static com.rudderstack.android.ruddermetricsreporterandroid.SeverityReason.REASON_HANDLED_EXCEPTION;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.rudderstack.android.ruddermetricsreporterandroid.internal.AppDataCollector;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BackgroundTaskService;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BreadcrumbState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ClientComponentCallbacks;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Configuration;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Connectivity;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ConnectivityCompat;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DataCollectionModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceDataCollector;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Error;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ExceptionHandler;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ImmutableConfig;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.MemoryTrimState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.MetadataAware;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.MetadataState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.RudderErrorStateModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.Severity;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.StateObserver;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.TaskType;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ContextModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.SystemServiceModule;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * A Bugsnag Client instance allows you to use Bugsnag in your Android app.
 * Typically you'd instead use the static access provided in the Bugsnag class.
 * <p/>
 * Example usage:
 * <p/>
 * Client client = new Client(this, "your-api-key");
 * client.notify(new RuntimeException("something broke!"));
 *
 *
 */
@SuppressWarnings({"checkstyle:JavadocTagContinuationIndentation", "ConstantConditions"})
public class Client implements MetadataAware {

    final ImmutableConfig immutableConfig;

    final MetadataState metadataState;

    final Context appContext;

    @NonNull
    final DeviceDataCollector deviceDataCollector;

    @NonNull
    final AppDataCollector appDataCollector;

    @NonNull
    final BreadcrumbState breadcrumbState;

    @NonNull
    final MemoryTrimState memoryTrimState = new MemoryTrimState();

//    @NonNull
//    protected final EventStore eventStore;


    final Logger logger;
    final Connectivity connectivity;

//    final Notifier notifier;

    //    @Nullable
//    final LastRunInfo lastRunInfo;
//    final LastRunInfoStore lastRunInfoStore;
//    final LaunchCrashTracker launchCrashTracker;
    final BackgroundTaskService bgTaskService = new BackgroundTaskService();
    private final ExceptionHandler exceptionHandler;

    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     */
    public Client(@NonNull Context androidContext) {
        this(androidContext, Configuration.load(androidContext));
    }


    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     * @param configuration  a configuration for the Client
     */
    public Client(@NonNull Context androidContext, @NonNull final Configuration configuration) {
        ContextModule contextModule = new ContextModule(androidContext);
        appContext = contextModule.getCtx();

        connectivity = new ConnectivityCompat(appContext, new Function2<Boolean, String, Unit>() {
            @Override
            public Unit invoke(Boolean hasConnection, String networkState) {
                Map<String, Object> data = new HashMap<>();
                data.put("hasConnection", hasConnection);
                data.put("networkState", networkState);
                leaveAutoBreadcrumb("Connectivity changed", BreadcrumbType.STATE, data);
                if (hasConnection) {
//                    eventStore.flushAsync();
                }
                return null;
            }
        });

        // set sensible defaults for delivery/project packages etc if not set
        ConfigModule configModule = new ConfigModule(contextModule, configuration);
        immutableConfig = configModule.getConfig();
        logger = immutableConfig.getLogger();

        if (!(androidContext instanceof Application)) {
            logger.w("You should initialize Bugsnag from the onCreate() callback of your "
                    + "Application subclass, as this guarantees errors are captured as early "
                    + "as possible. "
                    + "If a custom Application subclass is not possible in your app then you "
                    + "should suppress this warning by passing the Application context instead: "
                    + "Bugsnag.start(context.getApplicationContext()). "
                    + "For further info see: "
                    + "https://docs.bugsnag.com/platforms/android/#basic-configuration");

        }

        // setup storage as soon as possible
//        final StorageModule storageModule = new StorageModule(appContext,
//                immutableConfig, logger);

        // setup state trackers for error handling
        RudderErrorStateModule errorStateModule = new RudderErrorStateModule(
                immutableConfig, configuration);
//        clientObservable = errorStateModule.getClientObservable();
//        callbackState = errorStateModule.getCallbackState();
        breadcrumbState = errorStateModule.getBreadcrumbState();
        metadataState = errorStateModule.getMetadataState();

        // lookup system services
        final SystemServiceModule systemServiceModule = new SystemServiceModule(contextModule);

        // block until storage module has resolved everything
//        storageModule.resolveDependencies(bgTaskService, TaskType.IO);

        // setup further state trackers and data collection
//        TrackerModule trackerModule = new TrackerModule(configModule,
//                storageModule, this, bgTaskService, callbackState);
//
        DataCollectionModule dataCollectionModule = new DataCollectionModule(contextModule,
                configModule, systemServiceModule,
                bgTaskService, connectivity,
                /*storageModule.getDeviceId()*/ "", //TODO
                /*storageModule.getInternalDeviceId()*/ "", //TODO
                memoryTrimState);
        dataCollectionModule.resolveDependencies(bgTaskService, TaskType.IO);
        appDataCollector = dataCollectionModule.getAppDataCollector();
        deviceDataCollector = dataCollectionModule.getDeviceDataCollector();

        // load the device + user information
//        EventStorageModule eventStorageModule = new EventStorageModule(contextModule, configModule,
//                dataCollectionModule, bgTaskService, trackerModule, systemServiceModule, notifier,
//                callbackState);
//        eventStorageModule.resolveDependencies(bgTaskService, TaskType.IO);
//        eventStore = eventStorageModule.getEventStore();

//        deliveryDelegate = new DeliveryDelegate(logger, eventStore,
//                immutableConfig, callbackState, notifier, bgTaskService);

        exceptionHandler = new ExceptionHandler(this, logger);

        start();
    }

    @VisibleForTesting
    Client(
            ImmutableConfig immutableConfig,
            MetadataState metadataState,
//            ContextState contextState,
//            CallbackState callbackState,
//            UserState userState,
//            FeatureFlagState featureFlagState,
//            ClientObservable clientObservable,
            Context appContext,
            @NonNull DeviceDataCollector deviceDataCollector,
            @NonNull AppDataCollector appDataCollector,
            @NonNull BreadcrumbState breadcrumbState,
//            @NonNull EventStore eventStore,
//            SystemBroadcastReceiver systemBroadcastReceiver,
//            SessionTracker sessionTracker,
            Connectivity connectivity,
            Logger logger,
//            DeliveryDelegate deliveryDelegate,
//            LastRunInfoStore lastRunInfoStore,
//            LaunchCrashTracker launchCrashTracker,
            ExceptionHandler exceptionHandler
//            Notifier notifier
    ) {
        this.immutableConfig = immutableConfig;
        this.metadataState = metadataState;
//        this.contextState = contextState;
//        this.callbackState = callbackState;
//        this.userState = userState;
//        this.featureFlagState = featureFlagState;
//        this.clientObservable = clientObservable;
        this.appContext = appContext;
        this.deviceDataCollector = deviceDataCollector;
        this.appDataCollector = appDataCollector;
        this.breadcrumbState = breadcrumbState;
//        this.eventStore = eventStore;
//        this.systemBroadcastReceiver = systemBroadcastReceiver;
//        this.sessionTracker = sessionTracker;
        this.connectivity = connectivity;
        this.logger = logger;
//        this.deliveryDelegate = deliveryDelegate;
//        this.lastRunInfoStore = lastRunInfoStore;
//        this.launchCrashTracker = launchCrashTracker;
//        this.lastRunInfo = null;
        this.exceptionHandler = exceptionHandler;
//        this.notifier = notifier;
//        internalMetrics = new InternalMetricsNoop();
//        configDifferences = new HashMap<>();
    }

    private void start() {
//        if (immutableConfig.getEnabledErrorTypes().getUnhandledExceptions()) {
        exceptionHandler.install();
//        }


        // Flush any on-disk errors and sessions
//        eventStore.flushOnLaunch();
//        eventStore.flushAsync();

        // These call into NdkPluginCaller to sync with the native side, so they must happen later
//        internalMetrics.setConfigDifferences(configDifferences);
//        callbackState.setInternalMetrics(internalMetrics);

        // Register listeners for system events in the background
        registerComponentCallbacks();

        // Leave auto breadcrumb
        Map<String, Object> data = new HashMap<>();

        logger.d("Rudder Error Colloector loaded");
    }


    private void logNull(String property) {
        logger.e("Invalid null value supplied to client." + property + ", ignoring");
    }

    private void registerComponentCallbacks() {
        appContext.registerComponentCallbacks(new ClientComponentCallbacks(
                deviceDataCollector,
                (oldOrientation, newOrientation) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("from", oldOrientation);
                    data.put("to", newOrientation);
                    leaveAutoBreadcrumb("Orientation changed", BreadcrumbType.STATE, data);
                    return null;
                }, (isLowMemory, memoryTrimLevel) -> {
                    memoryTrimState.setLowMemory(Boolean.TRUE.equals(isLowMemory));
                    if (memoryTrimState.updateMemoryTrimLevel(memoryTrimLevel)) {
                        leaveAutoBreadcrumb(
                                "Trim Memory",
                                BreadcrumbType.STATE,
                                Collections.<String, Object>singletonMap(
                                        "trimLevel", memoryTrimState.getTrimLevelDescription()
                                )
                        );
                    }

                    memoryTrimState.emitObservableEvent();
                    return null;
                }
        ));
    }


    void addObserver(StateObserver observer) {
        metadataState.addObserver(observer);
        breadcrumbState.addObserver(observer);
//        clientObservable.addObserver(observer);
//        contextState.addObserver(observer);
//        deliveryDelegate.addObserver(observer);
//        launchCrashTracker.addObserver(observer);
        memoryTrimState.addObserver(observer);
    }

    void removeObserver(StateObserver observer) {
        metadataState.removeObserver(observer);
        breadcrumbState.removeObserver(observer);
//        sessionTracker.removeObserver(observer);
//        clientObservable.removeObserver(observer);
//        contextState.removeObserver(observer);
//        deliveryDelegate.removeObserver(observer);
//        launchCrashTracker.removeObserver(observer);
        memoryTrimState.removeObserver(observer);
//        featureFlagState.removeObserver(observer);
    }

    /**
     * Sends initial state values for Metadata/User/Context to any registered observers.
     */
    void syncInitialState() {
        metadataState.emitObservableEvent();
//        contextState.emitObservableEvent();
//        userState.emitObservableEvent();
        memoryTrimState.emitObservableEvent();
//        featureFlagState.emitObservableEvent();
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exc     the exception to send to Bugsnag
     * @param onError callback invoked on the generated error report for
     *                additional modification
     */
    public void notify(@NonNull Throwable exc) {
        if (exc != null) {
            if (immutableConfig.shouldDiscardError(exc)) {
                return;
            }
            SeverityReason severityReason = SeverityReason.newInstance(REASON_HANDLED_EXCEPTION);
            Metadata metadata = metadataState.getMetadata();
//            FeatureFlags featureFlags = featureFlagState.getFeatureFlags();
            ErrorEvent event = new ErrorEvent(exc, immutableConfig, severityReason, metadata);
            populateAndNotifyAndroidEvent(event);
        } else {
            logNull("notify");
        }
    }

    /**
     * Caches an error then attempts to notify.
     * <p>
     * Should only ever be called from the {@link ExceptionHandler}.
     */
    public void notifyUnhandledException(@NonNull Throwable exc, Metadata metadata,
                                         @SeverityReason.SeverityReasonType String severityReason,
                                         @Nullable String attributeValue) {
        SeverityReason handledState
                = SeverityReason.newInstance(severityReason, Severity.ERROR, attributeValue);
        Metadata data = Metadata.merge(metadataState.getMetadata(), metadata);
        ErrorEvent event = new ErrorEvent(exc, immutableConfig, handledState,
                data);
        populateAndNotifyAndroidEvent(event);

        // suspend execution of any further background tasks, waiting for previously
        // submitted ones to complete.
        bgTaskService.shutdown();
    }

    void populateAndNotifyAndroidEvent(@NonNull ErrorEvent event) {
        // Capture the state of the app and device and attach diagnostics to the event
        event.setDevice(deviceDataCollector.generateDeviceWithState(new Date().getTime()));
        event.addMetadata("device", deviceDataCollector.getDeviceMetadata());

        // add additional info that belongs in metadata
        // generate new object each time, as this can be mutated by end-users
        event.setApp(appDataCollector.generateAppWithState());
        event.addMetadata("app", appDataCollector.getAppDataMetadata());

        // Attach breadcrumbState to the event
        event.setBreadcrumbs(breadcrumbState.copy());

        notifyInternal(event);
    }

    void notifyInternal(@NonNull ErrorEvent event) {
        // leave an error breadcrumb of this event - for the next event
        leaveErrorBreadcrumb(event);
        logger.e("Rudder Error Colloector notifyInternal: ");
        logger.e(event.toString());
//        deliveryDelegate.deliver(event);
    }

    /**
     * Returns the current buffer of breadcrumbs that will be sent with captured events. This
     * ordered list represents the most recent breadcrumbs to be captured up to the limit
     * set in {@link Configuration#getMaxBreadcrumbs()}.
     * <p>
     * The returned collection is readonly and mutating the list will cause no effect on the
     * Client's state. If you wish to alter the breadcrumbs collected by the Client then you should
     * use {@link Configuration#setEnabledBreadcrumbTypes(Set)} and
     * {@link Configuration#addOnBreadcrumb(OnBreadcrumbCallback)} instead.
     *
     * @return a list of collected breadcrumbs
     */
    @NonNull
    public List<Breadcrumb> getBreadcrumbs() {
        return breadcrumbState.copy();
    }

    @NonNull
    AppDataCollector getAppDataCollector() {
        return appDataCollector;
    }

    @NonNull
    DeviceDataCollector getDeviceDataCollector() {
        return deviceDataCollector;
    }

    /**
     * Adds a map of multiple metadata key-value pairs to the specified section.
     */
    @Override
    public void addMetadata(@NonNull String section, @NonNull Map<String, ?> value) {
        if (section != null && value != null) {
            metadataState.addMetadata(section, value);
        } else {
            logNull("addMetadata");
        }
    }

    /**
     * Adds the specified key and value in the specified section. The value can be of
     * any primitive type or a collection such as a map, set or array.
     */
    @Override
    public void addMetadata(@NonNull String section, @NonNull String key, @Nullable Object value) {
        if (section != null && key != null) {
            metadataState.addMetadata(section, key, value);

        } else {
            logNull("addMetadata");
        }
    }

    /**
     * Removes all the data from the specified section.
     */
    @Override
    public void clearMetadata(@NonNull String section) {
        if (section != null) {
            metadataState.clearMetadata(section);
        } else {
            logNull("clearMetadata");
        }
    }

    /**
     * Removes data with the specified key from the specified section.
     */
    @Override
    public void clearMetadata(@NonNull String section, @NonNull String key) {
        if (section != null && key != null) {
            metadataState.clearMetadata(section, key);
        } else {
            logNull("clearMetadata");
        }
    }

    /**
     * Returns a map of data in the specified section.
     */
    @Nullable
    @Override
    public Map<String, Object> getMetadata(@NonNull String section) {
        if (section != null) {
            return metadataState.getMetadata(section);
        } else {
            logNull("getMetadata");
            return null;
        }
    }

    /**
     * Returns the value of the specified key in the specified section.
     */
    @Override
    @Nullable
    public Object getMetadata(@NonNull String section, @NonNull String key) {
        if (section != null && key != null) {
            return metadataState.getMetadata(section, key);
        } else {
            logNull("getMetadata");
            return null;
        }
    }

    // cast map to retain original signature until next major version bump, as this
    // method signature is used by Unity/React native
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    Map<String, Object> getMetadata() {
        return (Map) metadataState.getMetadata().toMap();
    }

    /**
     * Leave a "breadcrumb" log message, representing an action that occurred
     * in your app, to aid with debugging.
     *
     * @param message the log message to leave
     */
    public void leaveBreadcrumb(@NonNull String message) {
        if (message != null) {
            breadcrumbState.add(new Breadcrumb(message, logger));
        } else {
            logNull("leaveBreadcrumb");
        }
    }

    /**
     * Leave a "breadcrumb" log message representing an action or event which
     * occurred in your app, to aid with debugging
     *
     * @param message  A short label
     * @param metadata Additional diagnostic information about the app environment
     * @param type     A category for the breadcrumb
     */
    public void leaveBreadcrumb(@NonNull String message,
                                @NonNull Map<String, Object> metadata,
                                @NonNull BreadcrumbType type) {
        if (message != null && type != null && metadata != null) {
            breadcrumbState.add(new Breadcrumb(message, type, metadata, new Date(), logger));
        } else {
            logNull("leaveBreadcrumb");
        }
    }

    /**
     * Intended for internal use only - leaves a breadcrumb if the type is enabled for automatic
     * breadcrumbs.
     *
     * @param message  A short label
     * @param type     A category for the breadcrumb
     * @param metadata Additional diagnostic information about the app environment
     */
    void leaveAutoBreadcrumb(@NonNull String message,
                             @NonNull BreadcrumbType type,
                             @NonNull Map<String, Object> metadata) {
        if (!immutableConfig.shouldDiscardBreadcrumb(type)) {
            breadcrumbState.add(new Breadcrumb(message, type, metadata, new Date(), logger));
        }
    }

    private void leaveErrorBreadcrumb(@NonNull ErrorEvent event) {
        // Add a breadcrumb for this event occurring
        List<Error> errors = event.getErrors();

        if (errors.size() > 0) {
            String errorClass = errors.get(0).getErrorClass();
            String message = errors.get(0).getErrorMessage();

            Map<String, Object> data = new HashMap<>();
            data.put("errorClass", errorClass);
            data.put("message", message);
            data.put("unhandled", String.valueOf(event.getUnhandled()));
            data.put("severity", event.getSeverity().toString());
            breadcrumbState.add(new Breadcrumb(errorClass,
                    BreadcrumbType.ERROR, data, new Date(), logger));
        }
    }

//    @NonNull
//    EventStore getEventStore() {
//        return eventStore;
//    }

    public ImmutableConfig getConfig() {
        return immutableConfig;
    }

    void setBinaryArch(String binaryArch) {
        getAppDataCollector().setBinaryArch(binaryArch);
    }

    Context getAppContext() {
        return appContext;
    }

    /**
     * Intended for internal use only - sets the code bundle id for React Native
     */
    @Nullable
    String getCodeBundleId() {
        return appDataCollector.getCodeBundleId();
    }

    /**
     * Intended for internal use only - sets the code bundle id for React Native
     */
    void setCodeBundleId(@Nullable String codeBundleId) {
        appDataCollector.setCodeBundleId(codeBundleId);
    }

    void addRuntimeVersionInfo(@NonNull String key, @NonNull String value) {
        deviceDataCollector.addRuntimeVersionInfo(key, value);
    }

    @VisibleForTesting
    void close() {
        connectivity.unregisterForNetworkChanges();
        bgTaskService.shutdown();
    }

    Logger getLogger() {
        return logger;
    }


//    Notifier getNotifier() {
//        return notifier;
//    }

    MetadataState getMetadataState() {
        return metadataState;
    }

//    void setAutoDetectAnrs(boolean autoDetectAnrs) {
//        pluginClient.setAutoDetectAnrs(this, autoDetectAnrs);
//    }
}
