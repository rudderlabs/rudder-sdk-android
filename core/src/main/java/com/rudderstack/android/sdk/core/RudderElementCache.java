package com.rudderstack.android.sdk.core;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * RudderContext is populated once and cached through out the lifecycle
 * */
class RudderElementCache {
    static RudderContext cachedContext;

    private RudderElementCache() {
        // stop instantiating
    }

    static void initiate(Application application, String anonymousId, String advertisingId, String deviceToken, boolean isAutoCollectAdvertId) {
        if (cachedContext == null) {
            RudderLogger.logDebug("RudderElementCache: initiating RudderContext");
            cachedContext = new RudderContext(application, anonymousId, advertisingId, deviceToken);
            if (isAutoCollectAdvertId) {
                cachedContext.updateDeviceWithAdId();
            }
        }
    }

    static RudderContext getCachedContext() {
        return cachedContext == null ? new RudderContext() :cachedContext.copy();
    }

    static void reset() {
        cachedContext.resetTraits();
        persistTraits();
        cachedContext.resetExternalIds();

    }

    static void persistTraits() {
        cachedContext.persistTraits();
    }

    static void updateTraits(RudderTraits traits) {
        cachedContext.updateTraits(traits);
        persistTraits();
    }

    static void updateTraits(Map<String, Object> traits) {
        cachedContext.updateTraitsMap(traits);
        persistTraits();
    }

    public static void updateExternalIds(@NonNull List<Map<String, Object>> externalIds) {
        cachedContext.updateExternalIds(externalIds);
        cachedContext.persistExternalIds();
    }

    public static void setAnonymousId(String anonymousId) {
        Map<String, Object> traits = new HashMap<>();
        traits.put("anonymousId", anonymousId);
        cachedContext.updateTraitsMap(traits);
    }

    static void updateAnonymousId(@NonNull String anonymousId) {
        RudderContext.updateAnonymousId(anonymousId);
        cachedContext.updateAnonymousIdTraits();
        persistTraits();
    }
}

