package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Date;
import java.util.Locale;

class RudderUserSession {
    private final RudderConfig config;
    private String sessionId;
    private boolean sessionStart;
    private Date sessionStartTime;

    RudderUserSession(RudderConfig _config) {
        this.config = _config;
    }

    public void startSession(String sessionId) {
        synchronized (this) {
            _startSession(sessionId);
        }
    }

    public void sessionStart(boolean sessionStart) {
        synchronized (this) {
            this.sessionStart = sessionStart;
        }
    }

    public void checkSessionDuration() {
        if (this.sessionStartTime == null) { return; }
        long prevSessionStartTime;
        synchronized (this) {
            prevSessionStartTime = this.sessionStartTime.getTime();
        }
        final long timeDifference = Math.abs((new Date()).getTime() - prevSessionStartTime);
        if (timeDifference > (config.getSessionDuration() * 60 * 1000)) {
            startSession(Utils.getCurrentTimeSeconds());
        }
    }

    private void _startSession(String sessionId) {
        if (sessionId.length() > 0) {
            synchronized (this) {
                this.sessionId = sessionId;
                this.sessionStart = true;
                this.sessionStartTime = new Date();
            }
            RudderLogger.logDebug(String.format(Locale.US, "Starting new session with id: %s", sessionId));
        } else {
            RudderLogger.logDebug("sessionId can not be empty");
        }
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    public boolean getSessionStart() {
        return sessionStart;
    }

    public void clearSession() {
        synchronized (this) {
            this.sessionId = null;
            this.sessionStart = true;
            this.sessionStartTime = null;
        }
    }
}
