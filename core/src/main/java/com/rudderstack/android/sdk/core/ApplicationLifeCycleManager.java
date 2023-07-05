package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationLifeCycleManager {
    private final RudderConfig config;
    private final Application application;
    private final RudderFlushWorkManager rudderFlushWorkManager;
    private final EventRepository repository;
    public static final String VERSION = "version";
    private static final AtomicBoolean isFirstLaunch = new AtomicBoolean(true);
    private final RudderPreferenceManager preferenceManager;
    private boolean isApplicationUpdated = false;

    public ApplicationLifeCycleManager(RudderConfig config, Application application,
                                       RudderFlushWorkManager rudderFlushWorkManager,
                                       EventRepository repository,
                                       RudderPreferenceManager preferenceManager) {
        this.config = config;
        this.application = application;
        this.rudderFlushWorkManager = rudderFlushWorkManager;
        this.repository = repository;
        this.preferenceManager = preferenceManager;
    }

    /*
     * Check if App is installed for the first time or it is updated.
     * If it is the first time then make LifeCycle event: Application Installed.
     * If it is updated then make LifeCycle event: Application Updated.
     */
    void trackApplicationUpdateStatus() {
        if (!this.config.isTrackLifecycleEvents() && !this.config.isNewLifeCycleEvents()) {
            return;
        }
        AppVersion appVersion = new AppVersion(application);
        if (appVersion.previousBuild == -1) {
            // application was not installed previously, now triggering Application Installed event
            appVersion.storeCurrentBuildAndVersion();
            sendApplicationInstalled(appVersion.currentBuild, appVersion.currentVersion);
            rudderFlushWorkManager.registerPeriodicFlushWorker();
        } else if (appVersion.previousBuild != appVersion.currentBuild) {
            this.isApplicationUpdated = true;
            appVersion.storeCurrentBuildAndVersion();
            sendApplicationUpdated(appVersion.previousBuild, appVersion.currentBuild, appVersion.previousVersion, appVersion.currentVersion);
        }
    }

    boolean isApplicationUpdated() {
        return this.isApplicationUpdated;
    }

    void sendApplicationInstalled(int currentBuild, String currentVersion) {
        RudderLogger.logDebug("ApplicationLifeCycleManager: sendApplicationInstalled: Tracking Application Installed");
        RudderMessage message = new RudderMessageBuilder()
                .setEventName("Application Installed")
                .setProperty(
                        new RudderProperty()
                                .putValue(VERSION, currentVersion)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        repository.processMessage(message);
    }

    void sendApplicationUpdated(int previousBuild, int currentBuild, String previousVersion, String currentVersion) {
        if (repository.getOptStatus()) {
            return;
        }
        RudderLogger.logDebug("ApplicationLifeCycleManager: sendApplicationUpdated: Tracking Application Updated");
        RudderMessage message = new RudderMessageBuilder().setEventName("Application Updated")
                .setProperty(
                        new RudderProperty()
                                .putValue("previous_version", previousVersion)
                                .putValue(VERSION, currentVersion)
                                .putValue("previous_build", previousBuild)
                                .putValue("build", currentBuild)
                ).build();
        message.setType(MessageType.TRACK);
        repository.processMessage(message);
    }

    void sendApplicationOpened() {
        Boolean isFirstLaunchValue = isFirstLaunch.getAndSet(false);
        if (repository.getOptStatus()) {
            return;
        }
        RudderProperty rudderProperty = new RudderProperty().putValue("from_background", !isFirstLaunch.get());
        if (Boolean.TRUE.equals(isFirstLaunchValue)) {
            rudderProperty.putValue(VERSION, preferenceManager.getVersionName());
        }
        RudderMessage trackMessage = new RudderMessageBuilder()
                .setEventName("Application Opened")
                .setProperty(rudderProperty)
                .build();
        trackMessage.setType(MessageType.TRACK);
        repository.processMessage(trackMessage);
    }

    void sendApplicationBackgrounded() {
        if (repository.getOptStatus()) {
            return;
        }
        RudderMessage message = new RudderMessageBuilder().setEventName("Application Backgrounded").build();
        message.setType(MessageType.TRACK);
        repository.processMessage(message);
    }

    void recordScreenView(@NonNull Activity activity) {
        if (repository.getOptStatus()) {
            return;
        }
        ScreenPropertyBuilder screenPropertyBuilder = new ScreenPropertyBuilder().setScreenName(activity.getLocalClassName()).isAutomatic(true);
        RudderMessage screenMessage = new RudderMessageBuilder().setEventName(activity.getLocalClassName()).setProperty(screenPropertyBuilder.build()).build();
        screenMessage.setType(MessageType.SCREEN);
        repository.processMessage(screenMessage);
    }

    public static Boolean isFirstLaunch() {
        return isFirstLaunch.get();
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