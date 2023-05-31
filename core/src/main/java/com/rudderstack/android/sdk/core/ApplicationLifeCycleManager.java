package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationLifeCycleManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private AtomicInteger noOfActivities;
    private Application application;
    private AtomicBoolean isFirstLaunch;
    private AtomicBoolean trackedApplicationLifeCycleEvents;
    private final RudderPreferenceManager preferenceManager;
    private final EventRepository repository;
    private final RudderFlushWorkManager rudderFlushWorkManager;
    private final RudderConfig config;
    private RudderUserSession userSession;

    // This is just a stub LifecycleOwner which is used when we need to call some lifecycle
    // methods without going through the actual lifecycle callbacks
    private static LifecycleOwner stubOwner =
            new LifecycleOwner() {
                Lifecycle stubLifecycle =
                        new Lifecycle() {
                            @Override
                            public void addObserver(@NonNull LifecycleObserver observer) {
                                // NO-OP
                            }

                            @Override
                            public void removeObserver(@NonNull LifecycleObserver observer) {
                                // NO-OP
                            }

                            @NonNull
                            @Override
                            public Lifecycle.State getCurrentState() {
                                return State.DESTROYED;
                            }
                        };

                @NonNull
                @Override
                public Lifecycle getLifecycle() {
                    return stubLifecycle;
                }
            };


    ApplicationLifeCycleManager(RudderPreferenceManager preferenceManager, EventRepository repository, RudderFlushWorkManager rudderFlushWorkManager, RudderConfig config, Application application) {
        this.noOfActivities = new AtomicInteger(1);
        this.isFirstLaunch = new AtomicBoolean(false);
        this.application = application;
        this.trackedApplicationLifeCycleEvents = new AtomicBoolean(false);
        this.preferenceManager = preferenceManager;
        this.repository = repository;
        this.rudderFlushWorkManager = rudderFlushWorkManager;
        this.config = config;
    }

    public void startSessionTracking() {
        RudderLogger.logDebug("ApplicationLifecycleManager: startSessionTracking: Initiating RudderUserSession");
        userSession = new RudderUserSession(preferenceManager, config);

        // 8. clear session if automatic session tracking was enabled previously
        // but disabled presently or vice versa.
        boolean previousAutoSessionTrackingStatus = preferenceManager.getAutoSessionTrackingStatus();
        if (previousAutoSessionTrackingStatus != config.isTrackAutoSession()) {
            userSession.clearSession();
        }
        preferenceManager.saveAutoSessionTrackingStatus(config.isTrackAutoSession());
        // starting automatic session tracking if enabled.
        if (config.isTrackLifecycleEvents() && config.isTrackAutoSession()) {
            userSession.startSessionIfNeeded();
        }
    }

    void applySessionTracking(RudderMessage message) {
        // Session Tracking
        if (userSession.getSessionId() != null) {
            message.setSession(userSession);
        }
        if (config.isTrackLifecycleEvents() && config.isTrackAutoSession()) {
            userSession.updateLastEventTimeStamp();
        }
    }

    private void startSessionTrackingIfApplicable() {
        // Session Tracking
        // Automatic tracking session started
        if (!isFirstLaunch.get() && config.isTrackAutoSession() && userSession != null) {
            userSession.startSessionIfNeeded();
        }
    }

    void startSession(Long sessionId) {
        if (config.isTrackAutoSession()) {
            endSession();
            config.setTrackAutoSession(false);
        }
        userSession.startSession(sessionId);
    }

    void endSession() {
        if (config.isTrackAutoSession()) {
            config.setTrackAutoSession(false);
        }
        userSession.clearSession();
    }

    public void reset() {
        if (userSession.getSessionId() != null) {
            userSession.refreshSession();
        }
    }

    /*
     * Check if App is installed for the first time or it is updated.
     * If it is the first time then make LifeCycle event: Application Installed.
     * If it is updated then make LifeCycle event: Application Updated.
     */
    private void sendApplicationUpdateStatus() {
        AppVersion appVersion = new AppVersion(this.application);
        if (appVersion.previousBuild == -1) {
            // application was not installed previously, now triggering Application Installed event
            appVersion.storeCurrentBuildAndVersion();
            sendApplicationInstalled(appVersion.currentBuild, appVersion.currentVersion);
            rudderFlushWorkManager.registerPeriodicFlushWorker();
        } else if (appVersion.previousBuild != appVersion.currentBuild) {
            appVersion.storeCurrentBuildAndVersion();
            sendApplicationUpdated(appVersion.previousBuild, appVersion.currentBuild, appVersion.previousVersion, appVersion.currentVersion);
        }
    }

    private void sendApplicationInstalled(int currentBuild, String currentVersion) {
        // If trackLifeCycleEvents is not allowed then discard the event
        if (!config.isTrackLifecycleEvents()) {
            return;
        }
        RudderLogger.logDebug("ApplicationLifeCycleManager: sendApplicationInstalled: Tracking Application Installed");
        RudderMessage message = new RudderMessageBuilder()
                .setEventName("Application Installed")
                .setProperty(
                        new RudderProperty()
                                .putValue("version", currentVersion)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        repository.processMessage(message);
    }

    private void sendApplicationUpdated(int previousBuild, int currentBuild, String previousVersion, String currentVersion) {
        // If either optOut() is set to true or LifeCycleEvents set to false then discard the event
        if (repository.getOptStatus() || !config.isTrackLifecycleEvents()) {
            return;
        }
        // Application Updated event
        RudderLogger.logDebug("ApplicationLifeCycleManager: sendApplicationInstalled: Tracking Application Updated");
        RudderMessage message = new RudderMessageBuilder().setEventName("Application Updated")
                .setProperty(
                        new RudderProperty()
                                .putValue("previous_version", previousVersion)
                                .putValue("version", currentVersion)
                                .putValue("previous_build", previousBuild)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        repository.processMessage(message);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (this.config.isTrackLifecycleEvents()) {
            if (noOfActivities.incrementAndGet() == 1) {
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                startSessionTrackingIfApplicable();
                if (repository.getOptStatus()) {
                    return;
                }
                RudderProperty rudderProperty = new RudderProperty()
                        .putValue("from_background", !isFirstLaunch.get());
                // If it is not firstLaunch then set the version as well
                if (isFirstLaunch.getAndSet(false)) {
                    rudderProperty.putValue("version", preferenceManager.getVersionName());
                }

                RudderMessage trackMessage = new RudderMessageBuilder()
                        .setEventName("Application Opened")
                        .setProperty(rudderProperty)
                        .build();
                trackMessage.setType(MessageType.TRACK);
                repository.processMessage(trackMessage);
            }
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (this.config.isTrackLifecycleEvents()) {
            if (noOfActivities.decrementAndGet() == 0) {
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                if (repository.getOptStatus()) {
                    return;
                }
                RudderMessage message = new RudderMessageBuilder().setEventName("Application Backgrounded").build();
                message.setType(MessageType.TRACK);
                repository.processMessage(message);
            }
        }
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        if (!trackedApplicationLifeCycleEvents.getAndSet(true)) {
            noOfActivities.set(0);
            isFirstLaunch.set(true);
            sendApplicationUpdateStatus();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (config.isRecordScreenViews()) {
            // If user has disabled tracking activities (i.e., set optOut() to true)
            // then discard the event
            if (repository.getOptStatus()) {
                return;
            }
            ScreenPropertyBuilder screenPropertyBuilder = new ScreenPropertyBuilder().setScreenName(activity.getLocalClassName()).isAtomatic(true);
            RudderMessage screenMessage = new RudderMessageBuilder().setEventName(activity.getLocalClassName()).setProperty(screenPropertyBuilder.build()).build();
            screenMessage.setType(MessageType.SCREEN);
            repository.processMessage(screenMessage);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!config.isUseNewLifeCycleEvents()) {
            onStop(stubOwner);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        if (!config.isUseNewLifeCycleEvents()) {
            onCreate(stubOwner);
        }
        trackDeepLinks(activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!config.isUseNewLifeCycleEvents()) {
            onStart(stubOwner);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (!config.isUseNewLifeCycleEvents()) {
            onPause(stubOwner);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (!config.isUseNewLifeCycleEvents()) {
            onDestroy(stubOwner);
        }
    }

    @NonNull
    public void trackDeepLinks(Activity activity) {
        try {
            RudderProperty rudderProperty = new RudderProperty();
            Intent intent = activity.getIntent();
            if (intent == null || intent.getData() == null) {
                RudderLogger.logVerbose("ApplicationLifeCycleManager: trackDeepLinks: No deep link found in the activity");
                return;
            }

            // Get information about who launched this activity
            String referrer = Utils.getReferrer(activity);
            if (referrer != null) {
                rudderProperty.putValue("referring_application", referrer);
            }

            Uri uri = intent.getData();
            if (uri != null) {
                try {
                    for (String parameter : uri.getQueryParameterNames()) {
                        String value = uri.getQueryParameter(parameter);
                        if (value != null && !value.trim().isEmpty()) {
                            rudderProperty.putValue(parameter, value);
                        }
                    }
                } catch (Exception e) {
                    RudderLogger.logError("ApplicationLifeCycleManager: trackDeepLinks: Failed to get uri query parameters: " + e);
                }
                rudderProperty.putValue("url", uri.toString());
            }
            RudderMessage message = new RudderMessageBuilder().setEventName("Deep Link Opened").setProperty(rudderProperty).build();
            message.setType(MessageType.TRACK);
            repository.processMessage(message);
        } catch (Exception e) {
            RudderLogger.logError("ApplicationLifeCycleManager: trackDeepLinks: Error occurred while tracking deep link" + e);
        }
    }

    private class AppVersion {
        int previousBuild;
        int currentBuild;
        String previousVersion;
        String currentVersion;

        AppVersion(Application application) {
            try {
                previousBuild = preferenceManager.getBuildNumber();
                previousVersion = preferenceManager.getVersionName();
                RudderLogger.logDebug("Previous Installed Version: " + previousVersion);
                RudderLogger.logDebug("Previous Installed Build: " + previousBuild);
                String packageName = application.getPackageName();
                PackageManager packageManager = application.getPackageManager();
                if (packageManager == null)
                    return;
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                currentVersion = packageInfo.versionName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentBuild = (int) packageInfo.getLongVersionCode();
                } else {
                    currentBuild = packageInfo.versionCode;
                }
                RudderLogger.logDebug("Current Installed Version: " + currentVersion);
                RudderLogger.logDebug("Current Installed Build: " + currentBuild);
            } catch (PackageManager.NameNotFoundException ex) {
                RudderLogger.logError(ex);
            }
        }

        /*
         * Call this method to store the Current Build and Current Version of the app.
         * In case of the LifeCycle events Application Installed or Application Updated only.
         */
        void storeCurrentBuildAndVersion() {
            preferenceManager.saveBuildNumber(currentBuild);
            preferenceManager.saveVersionName(currentVersion);
        }
    }
}
