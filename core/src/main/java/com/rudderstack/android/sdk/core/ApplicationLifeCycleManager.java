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
    private RudderPreferenceManager preferenceManager;
    private EventRepository repository;
    private RudderFlushWorkManager rudderFlushWorkManager;
    private RudderConfig config;


    ApplicationLifeCycleManager(Application application, RudderPreferenceManager preferenceManager, EventRepository repository, RudderFlushWorkManager rudderFlushWorkManager, RudderConfig config) {
        try {
            this.preferenceManager = preferenceManager;
            this.checkApplicationUpdateStatus(application);
            this.repository = repository;
            this.rudderFlushWorkManager = rudderFlushWorkManager;
            this.config = config;
            if (config.isTrackLifecycleEvents() || config.isRecordScreenViews()) {
                application.registerActivityLifecycleCallbacks(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkApplicationUpdateStatus(Application application) {
        try {
            int previousBuild = preferenceManager.getBuildNumber();
            String previousVersion = preferenceManager.getVersionName();
            RudderLogger.logDebug("Previous Installed Version: " + previousVersion);
            RudderLogger.logDebug("Previous Installed Build: " + previousBuild);
            String packageName = application.getPackageName();
            PackageManager packageManager = application.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int currentBuild;
            String currentVersion = packageInfo.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentBuild = (int) packageInfo.getLongVersionCode();
            } else {
                currentBuild = packageInfo.versionCode;
            }
            RudderLogger.logDebug("Current Installed Version: " + currentVersion);
            RudderLogger.logDebug("Current Installed Build: " + currentBuild);
            if (previousBuild == -1) {
                // application was not installed previously, Application Installed event
                preferenceManager.saveBuildNumber(currentBuild);
                preferenceManager.saveVersionName(currentVersion);
                sendApplicationInstalled(currentBuild, currentVersion);
                rudderFlushWorkManager.registerPeriodicFlushWorker();
            } else if (previousBuild != currentBuild) {
                preferenceManager.saveBuildNumber(currentBuild);
                preferenceManager.saveVersionName(currentVersion);
                sendApplicationUpdated(previousBuild, currentBuild, previousVersion, currentVersion);
            }
        } catch (PackageManager.NameNotFoundException ex) {
            RudderLogger.logError(ex);
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
        repository.dump(message);
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
        repository.dump(message);
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
            repository.dump(screenMessage);
        }
        if (this.config.isTrackLifecycleEvents()) {
            noOfActivities += 1;
            if (noOfActivities == 1) {
                // If user has disabled tracking activities (i.e., set optOut() to true)
                // then discard the event
                if (repository.getOptStatus()) {
                    return;
                }
                RudderMessage trackMessage;
                trackMessage = new RudderMessageBuilder()
                        .setEventName("Application Opened")
                        .setProperty(Utils.trackDeepLink(activity, isFirstLaunch, preferenceManager.getVersionName()))
                        .build();
                trackMessage.setType(MessageType.TRACK);
                repository.dump(trackMessage);
            }
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
                repository.dump(message);
            }
        }
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
}
