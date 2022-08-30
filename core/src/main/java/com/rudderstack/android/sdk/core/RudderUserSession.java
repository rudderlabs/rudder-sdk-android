package com.rudderstack.android.sdk.core;

import android.app.Application;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Date;
import java.util.Locale;

class RudderUserSession {
    private final RudderConfig config;
    private String sessionId;
    private boolean sessionStart;
    private Long inactivityStartTime;
    RudderPreferenceManager preferenceManager;

    RudderUserSession(Application _application, RudderConfig _config) {
        this.config = _config;
        this.preferenceManager = RudderPreferenceManager.getInstance(_application);
        this.sessionId = preferenceManager.getSessionId();
        this.inactivityStartTime = preferenceManager.getInactivityStartTime();
    }

    public void startSession(String sessionId) {
        synchronized (this) {
            this._startSession(sessionId);
        }
    }

    public void sessionStart(boolean sessionStart) {
        synchronized (this) {
            this.sessionStart = sessionStart;
        }
    }

    public void checkSessionTimeoutDuration() {
        if (this.inactivityStartTime == null) {
            this.startSession(Utils.getCurrentTimeSeconds());
        } else {
            long previousInactivityStartTime;
            synchronized (this) {
                previousInactivityStartTime = this.inactivityStartTime;
            }
            final long timeDifference = Math.abs((new Date()).getTime() - previousInactivityStartTime);
            if (timeDifference > this.config.getSessionTimeout()) {
                this.startSession(Utils.getCurrentTimeSeconds());
                this.startInactivityTime(null);
            }
        }
    }

    public void startInactivityTime(Long time) {
        synchronized (this) {
            this.inactivityStartTime = time;
            this.preferenceManager.saveInactivityStartTime( (time == null) ? -1 : time);
        }
    }

    private void _startSession(String sessionId) {
        if (sessionId.length() > 0) {
            synchronized (this) {
                this.sessionId = sessionId;
                this.sessionStart = true;
                this.preferenceManager.saveSessionId(sessionId);
            }
            RudderLogger.logDebug(String.format(Locale.US, "Starting new session with id: %s", sessionId));
        } else {
            RudderLogger.logDebug("sessionId can not be empty");
        }
    }

    @Nullable
    public String getSessionId() {
        return this.sessionId;
    }

    public boolean getSessionStart() {
        return this.sessionStart;
    }

    public void clearSession() {
        synchronized (this) {
            this.sessionId = null;
            this.sessionStart = true;
            this.inactivityStartTime = null;
            this.preferenceManager.saveSessionId(null);
            this.startInactivityTime(null);
        }

    }
}
