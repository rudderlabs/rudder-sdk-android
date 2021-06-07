package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RudderOption {
    private List<Map<String, Object>> externalIds;
    private Map<String, Object> integrations;
    private Map<String, Object> customContexts;

    public RudderOption() {
        if (RudderElementCache.cachedContext != null) {
            this.externalIds = RudderElementCache.cachedContext.getExternalIds();
        }
        this.integrations = new HashMap<>();
        this.customContexts = new HashMap<>();
    }

    public RudderOption putExternalId(String type, String id) {
        if (this.externalIds == null) {
            this.externalIds = new ArrayList<>();
        }

        // find out if something is already present in the storage (PreferenceManager)
        Map<String, Object> externalIdMap = null;
        int mapIndex = -1;
        for (int index = 0; index < this.externalIds.size(); index++) {
            Map<String, Object> map = this.externalIds.get(index);
            String mapType = (String) map.get("type");
            if (mapType != null && mapType.equalsIgnoreCase(type)) {
                externalIdMap = map;
                mapIndex = index;
                break;
            }
        }

        // if not present from previous runs: create new and assign the type
        if (externalIdMap == null) {
            externalIdMap = new HashMap<>();
            externalIdMap.put("type", type);
        }

        // assign new id or update existing id
        externalIdMap.put("id", id);

        // finally update existing position or add new id
        if (mapIndex == -1) { // not found in existing storage
            externalIds.add(externalIdMap);
        } else {
            externalIds.get(mapIndex).put("id", id);
        }

        // return for builder pattern
        return this;
    }

    public RudderOption putIntegration(@NonNull String type, @NonNull boolean enabled) {
        integrations.put(type, enabled);
        return this;
    }

    public RudderOption putIntegration(@NonNull RudderIntegration.Factory factory, @NonNull boolean enabled) {
        integrations.put(factory.key(), enabled);
        return this;
    }

    public RudderOption putCustomContext(@NonNull String key, @NonNull Map<String, Object> context) {
        customContexts.put(key, context);
        return this;
    }

    @Nullable
    List<Map<String, Object>> getExternalIds() {
        return externalIds;
    }

    @Nullable
    Map<String, Object> getIntegrations() {
        return this.integrations;
    }

    @NonNull
    Map<String, Object> getCustomContexts() {
        return this.customContexts;
    }
}
