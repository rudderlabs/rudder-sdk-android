package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class AndroidXLifeCycleManager implements DefaultLifecycleObserver {
    private ApplicationLifeCycleManager applicationLifeCycleManager;
    private RudderUserSessionManager userSessionManager;

    public AndroidXLifeCycleManager(ApplicationLifeCycleManager applicationLifeCycleManager, RudderUserSessionManager userSessionManager) {
        this.applicationLifeCycleManager = applicationLifeCycleManager;
        this.userSessionManager = userSessionManager;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        userSessionManager.startSessionTrackingIfApplicable();
        applicationLifeCycleManager.sendApplicationOpened();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        applicationLifeCycleManager.sendApplicationBackgrounded();
    }
}
