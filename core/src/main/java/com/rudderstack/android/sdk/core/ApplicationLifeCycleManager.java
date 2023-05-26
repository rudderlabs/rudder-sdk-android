package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationLifeCycleManager implements Application.ActivityLifecycleCallbacks {

    private int noOfActivities;
    private final AtomicBoolean isFirstLaunch = new AtomicBoolean(true);
    private final RudderPreferenceManager preferenceManager;
    private final EventRepository repository;
    private final RudderFlushWorkManager rudderFlushWorkManager;
    private final RudderConfig config;
    private RudderUserSession userSession;


    ApplicationLifeCycleManager(RudderPreferenceManager preferenceManager, EventRepository repository, RudderFlushWorkManager rudderFlushWorkManager, RudderConfig config) {
        this.preferenceManager = preferenceManager;
        this.repository = repository;
        this.rudderFlushWorkManager = rudderFlushWorkManager;
        this.config = config;
    }

    public void start(Application application) {
        startSessionTracking();
        this.sendApplicationUpdateStatus(application);
        if (config.isTrackLifecycleEvents() || config.isRecordScreenViews()) {
            application.registerActivityLifecycleCallbacks(this);
        }
    }

    private void startSessionTracking() {
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

    /*
     * Check if App is installed for the first time or it is updated.
     * If it is the first time then make LifeCycle event: Application Installed.
     * If it is updated then make LifeCycle event: Application Updated.
     */
    private void sendApplicationUpdateStatus(Application application) {
        AppVersion appVersion = new AppVersion(application);
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
    public void onActivityStarted(@NonNull Activity activity) {
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities += 1;
            if (noOfActivities == 1) {
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                if (repository.getOptStatus()) {
                    return;
                }
                startSessionTrackingIfApplicable();
                RudderMessage trackMessage;
                trackMessage = new RudderMessageBuilder()
                        .setEventName("Application Opened")
                        .setProperty(Utils.trackDeepLink(activity, isFirstLaunch, preferenceManager.getVersionName()))
                        .build();
                trackMessage.setType(MessageType.TRACK);
                repository.processMessage(trackMessage);
            }
        }
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

    private void startSessionTrackingIfApplicable() {
        // Session Tracking
        // Automatic tracking session started
        if (!isFirstLaunch.get() && config.isTrackAutoSession() && userSession != null) {
            userSession.startSessionIfNeeded();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities -= 1;
            if (noOfActivities == 0) {
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

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        // Empty
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Empty
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // Empty
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        // Empty
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Empty
    }

    public void reset() {
        if (userSession.getSessionId() != null) {
            userSession.refreshSession();
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
    /*create or replace view app_opened_installed as select result.event_name, result.userId,
    result.timestamp as sent_at, result.platform as platform, result.writeKey as write_key, result.anon_id as anonymous_id, result.sdk_version as sdk_version from (
        select gw_jobs_1.event ->> 'event' as event_name, gw_jobs_1.event->>'userId' as userId, gw_jobs_1.event->>'anonymousId' as anon_id, gw_jobs_1.event ->> 'originalTimestamp' as timestamp, gw_jobs_1.event->'context'->'library'->>'name' as platform, gw_jobs_1.event->'context'->'library'->>'version' as sdk_version, writeKey as writeKey from (select jsonb_array_elements(jsonb_extract_path(event_payload, 'batch')) as event, event_payload ->> 'writeKey' as writeKey
        from gw_jobs_4584) as gw_jobs_1 where gw_jobs_1.event->'context'->'library'->>'name' = 'com.rudderstack.android.sdk.core' )
        as result order by timestamp


        with app_installed as (select * from app_opened_installed order by sent_at),
        app_opened as( select event_name, lead(event_name,1) over (order by sent_at) next_event, userid, anonymous_id, sent_at, write_key, platform, sdk_version from app_installed)
        select * from app_opened where event_name = 'Application Installed'*/