package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

public class RudderUserSessionManager {
    private RudderUserSession userSession;
    private final RudderPreferenceManager preferenceManager;
    private final RudderConfig config;

    public RudderUserSessionManager(RudderPreferenceManager preferenceManager, RudderConfig config) {
        this.preferenceManager = preferenceManager;
        this.config = config;
    }

    public void startSessionTracking() {
        RudderLogger.logDebug("ApplicationLifecycleManager: startSessionTracking: Initiating RudderUserSession");
        userSession = new RudderUserSession(preferenceManager, config);

        // 8. clear session if automatic session tracking was enabled previously
        // but disabled presently or vice versa.
        boolean previousAutoSessionTrackingStatus = preferenceManager.getAutoSessionTrackingStatus();
        boolean currentAutomaticSessionTrackingStatus = isAutomaticSessionTrackingEnabled();
        if (previousAutoSessionTrackingStatus != currentAutomaticSessionTrackingStatus) {
            userSession.clearSession();
        }
        preferenceManager.saveAutoSessionTrackingStatus(currentAutomaticSessionTrackingStatus);
        // starting automatic session tracking if enabled.
        if (currentAutomaticSessionTrackingStatus) {
            userSession.startSessionIfNeeded();
        }
    }

    private boolean isAutomaticSessionTrackingEnabled() {
        return config.isTrackAutoSession() && isAutomaticLifeCycleEnabled();
    }

    private boolean isAutomaticLifeCycleEnabled() {
        return config.isTrackLifecycleEvents() || config.isNewLifeCycleEvents();
    }

    void applySessionTracking(RudderMessage message) {
        // Session Tracking
        if (getSessionId() != null) {
            message.setSession(userSession);
        }
        if (isAutomaticSessionTrackingEnabled()) {
            userSession.updateLastActiveTimestamp();
        }
    }

    void startSessionTrackingIfApplicable() {
        // Session Tracking
        // Automatic tracking session started
        if (!ApplicationLifeCycleManager.isFirstLaunch() && config.isTrackAutoSession() && userSession != null) {
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

    @Nullable
    Long getSessionId() {
        return userSession.getSessionId();
    }

    public void reset() {
        if (getSessionId() != null) {
            userSession.refreshSession();
            if (isAutomaticSessionTrackingEnabled()) {
                userSession.updateLastActiveTimestamp();
            }
        }
    }
}
