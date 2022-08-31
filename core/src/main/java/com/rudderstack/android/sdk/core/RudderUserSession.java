package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.Date;
import java.util.Locale;

class RudderUserSession {
    private final RudderConfig config;
    private String sessionId;
    private boolean sessionStart;
    private Long lastEventTimeStamp;
    private RudderPreferenceManager preferenceManager;

    RudderUserSession(RudderPreferenceManager _preferenceManager, RudderConfig _config) {
        this.config = _config;
        this.preferenceManager = _preferenceManager;
        this.sessionId = _preferenceManager.getSessionId();
        this.lastEventTimeStamp = _preferenceManager.getLastEventTimeStamp();
    }

    public synchronized void startSession(String sessionId) {
        this._startSession(sessionId);
    }

    public synchronized void setSessionStart(boolean sessionStart) {
        this.sessionStart = sessionStart;
    }

    public void startSessionIfNeeded() {
        if (this.lastEventTimeStamp == null) {
            this.startSession(Utils.getCurrentTimeSeconds());
        } else {
            long previousLastEventTimeStamp;
            synchronized (this) {
                previousLastEventTimeStamp = this.lastEventTimeStamp;
            }
            final long timeDifference = Math.abs((new Date()).getTime() - previousLastEventTimeStamp);
            if (timeDifference > this.config.getSessionTimeout()) {
                this.startSession(Utils.getCurrentTimeSeconds());
                this.setLastEventTimeStamp(null);
            }
        }
    }

    public synchronized void setLastEventTimeStamp(Long time) {
        this.lastEventTimeStamp = time;
        this.preferenceManager.saveLastEventTimeStamp( (time == null) ? -1 : time);
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

    public synchronized void clearSession() {
        this.sessionId = null;
        this.sessionStart = true;
        this.lastEventTimeStamp = null;
        this.preferenceManager.saveSessionId(null);
        this.setLastEventTimeStamp(null);
    }
}
