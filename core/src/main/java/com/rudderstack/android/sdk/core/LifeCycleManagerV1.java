package com.rudderstack.android.sdk.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.app.Application.ActivityLifecycleCallbacks;

import androidx.annotation.Nullable;


import com.rudderstack.android.sdk.core.util.Utils;

import java.util.concurrent.atomic.AtomicInteger;

public class LifeCycleManagerV1 implements ActivityLifecycleCallbacks {

    private final ApplicationLifeCycleManager applicationLifeCycleManager;
    private AtomicInteger noOfActivities;
    private final EventRepository repository;
    private final RudderConfig config;
    private RudderUserSessionManager userSessionManager;


    LifeCycleManagerV1(EventRepository repository, RudderConfig config, ApplicationLifeCycleManager applicationLifeCycleManager, RudderUserSessionManager userSessionManager) {
        this.noOfActivities = new AtomicInteger(0);
        this.repository = repository;
        this.config = config;
        this.applicationLifeCycleManager = applicationLifeCycleManager;
        this.userSessionManager = userSessionManager;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        if (this.config.isTrackDeepLinks()) {
            trackDeepLinks(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (this.config.isRecordScreenViews()) {
            applicationLifeCycleManager.recordScreenView(activity);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (!config.isNewLifeCycleEvents() && this.config.isTrackLifecycleEvents() && noOfActivities.incrementAndGet() == 1) {
            userSessionManager.startSessionTrackingIfApplicable();
            applicationLifeCycleManager.sendApplicationOpened();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!this.config.isNewLifeCycleEvents() && this.config.isTrackLifecycleEvents() && noOfActivities.decrementAndGet() == 0) {
            applicationLifeCycleManager.sendApplicationBackgrounded();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // NO-OP
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        // NO-OP
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // NO-OP
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
            setURIQueryParams(rudderProperty, intent.getData());
            RudderMessage message = new RudderMessageBuilder().setEventName("Deep Link Opened").setProperty(rudderProperty).build();
            message.setType(MessageType.TRACK);
            repository.processMessage(message);
        } catch (Exception e) {
            RudderLogger.logError("ApplicationLifeCycleManager: trackDeepLinks: Error occurred while tracking deep link" + e);
        }
    }

    private void setURIQueryParams(RudderProperty rudderProperty, Uri uri) {
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
    }
}
