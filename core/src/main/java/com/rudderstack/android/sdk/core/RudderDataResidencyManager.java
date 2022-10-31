package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.util.Utils.isEmpty;
import static com.rudderstack.android.sdk.core.util.Utils.appendSlashToUrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Locale;
import java.util.Map;

class RudderDataResidencyManager {
    @VisibleForTesting
    Map<String, String> dataResidencyUrls = null;
    @VisibleForTesting
    RudderDataResidencyServer rudderDataResidencyServer;
    private String dataPlaneUrl = null;

    RudderDataResidencyManager(@Nullable RudderServerConfig serverConfig, @NonNull RudderConfig config) {
        if (serverConfig != null && serverConfig.source != null && serverConfig.source.dataResidencyUrls != null) {
            this.dataResidencyUrls = serverConfig.source.dataResidencyUrls;
        }
        this.rudderDataResidencyServer = config.getDataResidencyServer();
    }

    /**
     * This API will fetch the dataPlane URL present from the sourceConfig based on the Residency Server region.
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
    void processDataPlaneUrl() {
        if (rudderDataResidencyServer == RudderDataResidencyServer.US) {
            handleDefaultServer();
        } else {
            handleOtherServer(rudderDataResidencyServer);
        }
    }

    /**
     * Handle all server other than US.
     * If passed server is not present then fallback to US server.
     *
     * @param rudderDataResidencyServer Residency server set while SDK initialisation
     */
    @VisibleForTesting
    void handleOtherServer(@NonNull RudderDataResidencyServer rudderDataResidencyServer) {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(rudderDataResidencyServer.name());
        if (dataResidencyUrl != null) {
            setDataPlaneUrl(dataResidencyUrl);
        } else {
            handleDefaultServer();
        }
    }

    /**
     * Handle default US server
     * <p>
     * If default US server is also not present then no need to do any extra things,
     * as dataPlaneUrl logic is already handled while config object is build.
     */
    @VisibleForTesting
    void handleDefaultServer() {
        // TODO: Decide upper or lower case
        String dataResidencyUrl = getDataResidencyUrl(RudderDataResidencyServer.US.name());
        if (dataResidencyUrl != null) {
            setDataPlaneUrl(dataResidencyUrl);
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
    String getDataResidencyUrl(String region) {
        if (isEmpty(dataResidencyUrls)) {
            return null;
        }
        region = region.toLowerCase(Locale.ROOT);
        if (dataResidencyUrls.containsKey(region)) {
            String dataResidencyUrl = dataResidencyUrls.get(region);
            if (!isEmpty(dataResidencyUrl)) {
                return appendSlashToUrl(dataResidencyUrl);
            }
        }
        return null;
    }

    // Getter and Setter

    /**
     *
     * @return dataPlaneUrl from the sourceConfig based on the ResidencyServer region.
     *
     * If either required region is not present in the sourceConfig or sourceConfig is null
     * then return null.
     */
    @Nullable
    String getDataPlaneUrl() {
        return this.dataPlaneUrl;
    }

    @VisibleForTesting
    void setDataPlaneUrl(String dataPlaneUrl) {
        this.dataPlaneUrl = dataPlaneUrl;
    }
}
