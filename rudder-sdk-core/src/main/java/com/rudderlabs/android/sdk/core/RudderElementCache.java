package com.rudderlabs.android.sdk.core;

import android.app.Application;

/*
* RudderContext is populated once and cached through out the lifecycle
* */
class RudderElementCache {
    private static RudderContext cachedContext;

    private RudderElementCache() {
        // stop instantiating
    }

    static void initiate(Application application) {
        if (cachedContext == null) {
            RudderLogger.logDebug("RudderElementCache: initiating RudderContext");
            cachedContext = new RudderContext(application);
        }
    }

    static RudderContext getCachedContext() {
        return cachedContext;
    }
}

