package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Locale;

class RudderUserSession {
    private final RudderConfig config;
    private Long sessionId;
    private boolean sessionStart;
    private Long lastActiveTimestamp;
    private final RudderPreferenceManager preferenceManager;

    RudderUserSession(RudderPreferenceManager _preferenceManager, RudderConfig _config) {
        this.config = _config;
        this.preferenceManager = _preferenceManager;
        this.sessionId = _preferenceManager.getSessionId();
        this.lastActiveTimestamp = _preferenceManager.getLastActiveTimestamp();
    }

    public void startSession() {
        startSession(Utils.getCurrentTimeInSecondsLong());
    }

    public void startSession(Long sessionId) {
        synchronized (this) {
            this.sessionId = sessionId;
            this.sessionStart = true;
            this.preferenceManager.saveSessionId(sessionId);
        }
        RudderLogger.logDebug(String.format(Locale.US, "Starting new session with id: %s", sessionId));
    }

    public synchronized void startSessionIfNeeded() {
        if (this.lastActiveTimestamp == null) {
            this.startSession();
            return;
        }
        final long timeDifference;
        timeDifference = Math.abs((Utils.getCurrentTimeInMilliSeconds() - this.lastActiveTimestamp));
        if (timeDifference > this.config.getSessionTimeout()) {
            refreshSession();
        }
    }

    public void refreshSession() {
        this.clearSession();
        this.startSession();
    }

    public synchronized void updateLastActiveTimestamp() {
        this.lastActiveTimestamp = Utils.getCurrentTimeInMilliSeconds();
        this.preferenceManager.saveLastActiveTimestamp(this.lastActiveTimestamp);
    }

    @Nullable
    public Long getSessionId() {
        return this.sessionId;
    }

    public boolean getSessionStart() {
        return this.sessionStart;
    }

    public synchronized void setSessionStart(boolean sessionStart) {
        this.sessionStart = sessionStart;
    }

    public synchronized void clearSession() {
        this.sessionId = null;
        this.preferenceManager.clearSessionId();
        this.sessionStart = true;
        this.lastActiveTimestamp = null;
        this.preferenceManager.clearLastActiveTimestamp();
    }
}
