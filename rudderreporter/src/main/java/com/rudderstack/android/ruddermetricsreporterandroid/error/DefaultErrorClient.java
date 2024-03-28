/*
 * Creator: Debanjan Chatterjee on 09/06/23, 7:59 pm Last modified: 09/06/23, 5:32 pm
 * Copyright: All rights reserved 2023 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.android.ruddermetricsreporterandroid.error;


import static com.rudderstack.android.ruddermetricsreporterandroid.error.SeverityReason.REASON_HANDLED_EXCEPTION;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.rudderstack.android.ruddermetricsreporterandroid.Configuration;
import com.rudderstack.android.ruddermetricsreporterandroid.Logger;
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.AppDataCollector;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.BackgroundTaskService;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ClientComponentCallbacks;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.ConnectivityCompat;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DataCollectionModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DeviceDataCollector;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.NoopLogger;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.StateObserver;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.TaskType;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ConfigModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.ContextModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.di.SystemServiceModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.BreadcrumbState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.Error;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ExceptionHandler;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.ImmutableConfig;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MemoryTrimState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataAware;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.MetadataState;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.RudderErrorStateModule;
import com.rudderstack.android.ruddermetricsreporterandroid.internal.error.Severity;
import com.rudderstack.android.ruddermetricsreporterandroid.models.ErrorEntity;
import com.rudderstack.rudderjsonadapter.JsonAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * A Rudder Error Client instance allows you to send error stats from Rudder SDK.
 */
@SuppressWarnings({"checkstyle:JavadocTagContinuationIndentation", "ConstantConditions"})
public class DefaultErrorClient implements MetadataAware, ErrorClient {

    final ImmutableConfig immutableConfig;

    final MetadataState metadataState;

    private final Context appContext;

    @NonNull
    private final DeviceDataCollector deviceDataCollector;

    @NonNull
    private final AppDataCollector appDataCollector;

    @NonNull
    private final BreadcrumbState breadcrumbState;

    @NonNull
    private final MemoryTrimState memoryTrimState;
    @NonNull
    private final JsonAdapter jsonAdapter;

    final Logger logger;

    final BackgroundTaskService bgTaskService = new BackgroundTaskService();
    private final ExceptionHandler exceptionHandler;
    private final Reservoir reservoir;

    private final AtomicBoolean isErrorEnabled;


    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     */


    /**
     * Initialize a Bugsnag client
     *
     * @param configuration a configuration for the Client
     */
    public DefaultErrorClient(@NonNull ContextModule contextModule,
                              @NonNull final Configuration configuration,
                              @NonNull ConfigModule configModule,
                              @NonNull DataCollectionModule dataCollectionModule,
                              @NonNull Reservoir reservoir,
                              @NonNull JsonAdapter jsonAdapter,
                              @NonNull MemoryTrimState memoryTrimState,
                              boolean isErrorEnabled) {
        appContext = contextModule.getCtx();
        this.isErrorEnabled = new AtomicBoolean(isErrorEnabled);
        this.memoryTrimState = memoryTrimState;
        this.jsonAdapter = jsonAdapter;

        // set sensible defaults for delivery/project packages etc if not set
        immutableConfig = configModule.getConfig();
        logger = immutableConfig.getLogger();

        if (!(appContext instanceof Application)) {
            logger.w("You should initialize Bugsnag from the onCreate() callback of your "
                    + "Application subclass, as this guarantees errors are captured as early "
                    + "as possible. "
                    + "If a custom Application subclass is not possible in your app then you "
                    + "should suppress this warning by passing the Application context instead: "
                    + "Bugsnag.start(context.getApplicationContext()). "
                    + "For further info see: "
                    + "https://docs.bugsnag.com/platforms/android/#basic-configuration");

        }

        // setup state trackers for error handling
        RudderErrorStateModule errorStateModule = new RudderErrorStateModule(
                immutableConfig, configuration);
        breadcrumbState = errorStateModule.getBreadcrumbState();
        metadataState = errorStateModule.getMetadataState();

        dataCollectionModule.resolveDependencies(bgTaskService, TaskType.IO);
        appDataCollector = dataCollectionModule.getAppDataCollector();
        deviceDataCollector = dataCollectionModule.getDeviceDataCollector();

        this.reservoir = reservoir;
        exceptionHandler = new ExceptionHandler(this, logger);

        this.reservoir.setMaxErrorCount(configuration.getMaxPersistedEvents());
        start();
    }

    @NonNull
    BreadcrumbState getBreadcrumbState() {
        return breadcrumbState;
    }

    @NonNull
    MemoryTrimState getMemoryTrimState() {
        return memoryTrimState;
    }

    public DefaultErrorClient(@NonNull Context context,
                              @NonNull Configuration configuration,
                              @NonNull Reservoir reservoir,
                              @NonNull JsonAdapter jsonAdapter) {

        appContext = context;
        this.isErrorEnabled = new AtomicBoolean(true);
        this.memoryTrimState = new MemoryTrimState();
        this.jsonAdapter = jsonAdapter;
        PackageInfo packageInfo = null;
        try {
            if (context.getPackageManager() != null) {
                packageInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(),
                        PackageManager.GET_PERMISSIONS
                );
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        // set sensible defaults for delivery/project packages etc if not set
        immutableConfig = new ImmutableConfig(configuration.getLibraryMetadata(),
                configuration.getProjectPackages(),
                configuration.getEnabledBreadcrumbTypes(), configuration.getDiscardClasses(),
                configuration.getCrashFilter(),
                NoopLogger.INSTANCE,
                configuration.getMaxBreadcrumbs(), configuration.getMaxPersistedEvents(),
                configuration.getEnabledReleaseStages(), "release",
                packageInfo, null);
        logger = immutableConfig.getLogger();

        if (!(appContext instanceof Application)) {
            logger.w("You should initialize Bugsnag from the onCreate() callback of your "
                    + "Application subclass, as this guarantees errors are captured as early "
                    + "as possible. "
                    + "If a custom Application subclass is not possible in your app then you "
                    + "should suppress this warning by passing the Application context instead: "
                    + "Bugsnag.start(context.getApplicationContext()). "
                    + "For further info see: "
                    + "https://docs.bugsnag.com/platforms/android/#basic-configuration");

        }

        // setup state trackers for error handling
        RudderErrorStateModule errorStateModule = new RudderErrorStateModule(
                immutableConfig, configuration);
        breadcrumbState = errorStateModule.getBreadcrumbState();
        metadataState = errorStateModule.getMetadataState();
        ContextModule contextModule = new ContextModule(context);
        DataCollectionModule dataCollectionModule = new DataCollectionModule(
                contextModule, new ConfigModule(contextModule, configuration),
                new SystemServiceModule(contextModule), new BackgroundTaskService(),
                new ConnectivityCompat(contextModule.getCtx(), null), new MemoryTrimState());
        dataCollectionModule.resolveDependencies(bgTaskService, TaskType.IO);
        appDataCollector = dataCollectionModule.getAppDataCollector();
        deviceDataCollector = dataCollectionModule.getDeviceDataCollector();

        this.reservoir = reservoir;
        exceptionHandler = new ExceptionHandler(this, logger);
        this.reservoir.setMaxErrorCount(configuration.getMaxPersistedEvents());

        start();
    }

    private void start() {
        exceptionHandler.install();
        registerComponentCallbacks();

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

    @VisibleForTesting
    void addObserver(StateObserver observer) {
        metadataState.addObserver(observer);
        breadcrumbState.addObserver(observer);
        memoryTrimState.addObserver(observer);
    }

    @VisibleForTesting
    void removeObserver(StateObserver observer) {
        metadataState.removeObserver(observer);
        breadcrumbState.removeObserver(observer);
        memoryTrimState.removeObserver(observer);
    }

    /**
     * Sends initial state values for Metadata/User/Context to any registered observers.
     */
    void syncInitialState() {
        metadataState.emitObservableEvent();
        memoryTrimState.emitObservableEvent();
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exc the exception to send to Bugsnag
     */
    @Override
    public void notify(@NonNull Throwable exc) {
        if (null == exc) {
            logNull("notify");
            return;
        }
        SeverityReason severityReason = SeverityReason.newInstance(REASON_HANDLED_EXCEPTION);
        Metadata metadata = metadataState.getMetadata();
        ErrorEvent event = new ErrorEvent(exc, immutableConfig, severityReason, metadata);
        populateAndNotifyAndroidEvent(event);

    }

    /**
     * Caches an error then attempts to notify.
     * <p>
     * Should only ever be called from the {@link ExceptionHandler}.
     */
    @Override
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

    private void notifyInternal(@NonNull ErrorEvent event) {

        if (!isErrorEnabled.get())
            return;
        // leave an error breadcrumb of this event - for the next event
        leaveErrorBreadcrumb(event);
        String serializedEvent =
                event.serialize(jsonAdapter);
        if (serializedEvent != null) {
            reservoir.saveError(new ErrorEntity(serializedEvent));
        } else {
            logger.e("Rudder Error Collector notifyInternal: Cannot serialize event: " + event);
        }
    }

    /**
     * Returns the current buffer of breadcrumbs that will be sent with captured events. This
     * ordered list represents the most recent breadcrumbs to be captured up to the limit
     * set in {@link Configuration#getMaxBreadcrumbs()}.
     * <p>
     * The returned collection is readonly and mutating the list will cause no effect on the
     * Client's state. If you wish to alter the breadcrumbs collected by the Client then you should
     * use {@link Configuration#setEnabledBreadcrumbTypes(Set)} and
     *
     * @return a list of collected breadcrumbs
     */
    @Override
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
    @Override
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
    @Override
    public void leaveBreadcrumb(@NonNull String message,
                                @NonNull Map<String, Object> metadata,
                                @NonNull BreadcrumbType type) {
        if (message != null && type != null && metadata != null) {
            breadcrumbState.add(new Breadcrumb(message, type, metadata, new Date(), logger));
        } else {
            logNull("leaveBreadcrumb");
        }
    }

    @Override
    public void enable(boolean enable) {
        this.isErrorEnabled.set(enable);

    }

    private static boolean isExceptionValid(@NonNull List<String> keywords, Throwable exc) {
        if(keywords.isEmpty()) return true;
        for (String keyword : keywords) {
            if (exc.getMessage().contains(keyword)) {
                return true;
            }
            if (exc.getStackTrace() != null && isStackTraceValid(exc, keyword)) {
                return true;
            }
        }
        if(exc.getCause() != null){
            return isExceptionValid(keywords, exc.getCause());
        }
        return false;
    }

    private static boolean isStackTraceValid(Throwable exc, String keyword) {
        for (StackTraceElement element : exc.getStackTrace()) {
            if (element.getClassName().contains(keyword) ||
                    element.getFileName().contains(keyword)
                    || element.getMethodName().contains(keyword)) {
                return true;
            }
        }
        return false;
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

        if (!errors.isEmpty()) {
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
        bgTaskService.shutdown();
    }

    Logger getLogger() {
        return logger;
    }


    MetadataState getMetadataState() {
        return metadataState;
    }

}
