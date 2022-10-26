package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Map;

class RudderDataResidency {
    private final RudderConfig config;
    @VisibleForTesting
    Map<String, String> dataResidencyUrls = null;
    @VisibleForTesting
    ResidencyServer residencyServer;

    RudderDataResidency(@Nullable RudderServerConfig serverConfig, @NonNull RudderConfig config) {
        this.config = config;
        if (serverConfig != null && serverConfig.source != null && serverConfig.source.dataResidencyUrls != null) {
            this.dataResidencyUrls = serverConfig.source.dataResidencyUrls;
        }
        this.residencyServer = config.getResidencyServer();
    }

    void handleDataPlaneUrl() {
        if (residencyServer == ResidencyServer.US) {
            handleDefaultServer();
        } else {
            handleOtherServer(residencyServer);
        }
    }

    @VisibleForTesting
    void handleOtherServer(@NonNull ResidencyServer residencyServer) {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(residencyServer.name());
        if (dataResidencyUrl != null) {
            config.setDataPlaneUrl(dataResidencyUrl);
        } else {
            handleDefaultServer();
        }
    }

    @VisibleForTesting
    void handleDefaultServer() {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(ResidencyServer.US.name());
        if (dataResidencyUrl != null) {
            config.setDataPlaneUrl(dataResidencyUrl);
        }
    }

    @VisibleForTesting
    @Nullable
    String getDataResidencyUrl(@Nullable String region) {
        if (region != null)
            if (isNotEmpty(dataResidencyUrls)) {
                for (String key : dataResidencyUrls.keySet()) {
                    if (key.equalsIgnoreCase(region)) {
                        String dataResidencyUrl = dataResidencyUrls.get(key);
                        if (isNotEmpty(dataResidencyUrl)) {
                            return processDataPlaneUrl(dataResidencyUrl);
                        }
                        break;
                    }
                }
            }
        return null;
    }

    @VisibleForTesting
    @NonNull
    String processDataPlaneUrl(@NonNull String dataPlaneUrl) {
        if (!dataPlaneUrl.endsWith("/")) dataPlaneUrl += "/";
        return dataPlaneUrl;
    }

    @VisibleForTesting
    boolean isNotEmpty(@Nullable Map<String, String> value) {
        return !(value == null || value.isEmpty());
    }

    @VisibleForTesting
    boolean isNotEmpty(@Nullable String value) {
        return !(value == null || value.isEmpty());
    }
}
