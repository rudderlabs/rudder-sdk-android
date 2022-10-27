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
    DataResidencyServer dataResidencyServer;

    RudderDataResidency(@Nullable RudderServerConfig serverConfig, @NonNull RudderConfig config) {
        this.config = config;
        if (serverConfig != null && serverConfig.source != null && serverConfig.source.dataResidencyUrls != null) {
            this.dataResidencyUrls = serverConfig.source.dataResidencyUrls;
        }
        this.dataResidencyServer = config.getDataResidencyServer();
    }

    /**
     * This API will update the dataPlane URL present in the config, just after sourceConfig was fetched successfully.
     *
     * <p>Preference of dataPlane URL is decided as follows:</p>
     * <ul>
     *     <li>First look for the Residency server (which was set while SDK initialisation) in the source config.</li>
     *     <li>If Residency server was not set while SDK initialisation then fallback to default US residency server.</li>
     *     <li>If residency server list is empty or null then fallback to dataPlane URL (which was set while SDK initialisation).</li>
     *     <li>If dataPlane URL was not passed while SDK initialisation then as a last resort fallback to the default dataPlane URL.</li>
     * </ul>
     * <p>
     * Default Residency server is US.
     * <p>
     * Use the below code snippet to set the Data Residency:
     * <pre>
     *     {@code
     *     rudderClient = RudderClient.getInstance(
     *             this,
     *             WRITE_KEY,
     *             RudderConfig.Builder()
     *                 .withResidencyServer(ResidencyServer.US)
     *                 .build()
     *     )
     *      }
     * </pre>
     */
    void handleDataPlaneUrl() {
        if (dataResidencyServer == DataResidencyServer.US) {
            handleDefaultServer();
        } else {
            handleOtherServer(dataResidencyServer);
        }
    }

    /**
     * Handle all server other than US.
     * If passed server is not present then fallback to US server.
     *
     * @param dataResidencyServer Residency server set while SDK initialisation
     */
    @VisibleForTesting
    void handleOtherServer(@NonNull DataResidencyServer dataResidencyServer) {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(dataResidencyServer.name());
        if (dataResidencyUrl != null) {
            config.setDataPlaneUrl(dataResidencyUrl);
        } else {
            handleDefaultServer();
        }
    }

    /**
     * Handle default US server
     *
     * If default US server is also not present then no need to do any extra things,
     * as dataPlaneUrl logic is already handled while config object is build.
     */
    @VisibleForTesting
    void handleDefaultServer() {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(DataResidencyServer.US.name());
        if (dataResidencyUrl != null) {
            config.setDataPlaneUrl(dataResidencyUrl);
        }
    }

    /**
     * Look for the region in the data residency list.
     *
     * @param region Data Residency region set while SDK initialisation.
     * @return Corresponding residency URL if present else null.
     */
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
