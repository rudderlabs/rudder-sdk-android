package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Date;
import java.util.Locale;

class RudderUserSession {
    private RudderConfig config;
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
        synchronized (this) {
            long timeDifference = Math.abs((new Date()).getTime() - this.sessionStartTime.getTime());
            if (timeDifference > config.getSessionDuration() * 60) {
                _startSession(Utils.getCurrentTimeMillis());
            }
        }
    }

    private void _startSession(String sessionId) {
        if (config.isTrackLifecycleEvents()) {
            if (sessionId.length() > 0) {
                this.sessionId = sessionId;
                this.sessionStart = true;
                this.sessionStartTime = new Date();
                RudderLogger.logDebug(String.format(Locale.US, "Starting new session with id: %s", sessionId));
            } else {
                RudderLogger.logDebug("sessionId can not be empty");
            }
        } else {
            RudderLogger.logDebug("Life cycle events tracking is off");
        }
    }

    @Nullable
    public String getSessionId() {
        String sessionId;
        synchronized (this) {
            sessionId = this.sessionId;
        }
        return sessionId;
    }

    public boolean getSessionStart() {
        boolean sessionStart;
        synchronized (this) {
            sessionStart = this.sessionStart;
        }
        return sessionStart;
    }

    public void clearSession() {
        synchronized (this) {
            this.sessionId = null;
        }
    }
}
