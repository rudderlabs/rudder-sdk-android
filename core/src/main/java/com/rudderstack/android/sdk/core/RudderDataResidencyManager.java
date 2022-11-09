package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.util.Utils.getBoolean;
import static com.rudderstack.android.sdk.core.util.Utils.getString;
import static com.rudderstack.android.sdk.core.util.Utils.isEmpty;
import static com.rudderstack.android.sdk.core.util.Utils.appendSlashToUrl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.List;
import java.util.Map;

class RudderDataResidencyManager {
    @VisibleForTesting
    Map<String, List<Map<String, Object>>> dataResidencyUrls = null;
    @VisibleForTesting
    RudderDataResidencyServer rudderDataResidencyServer;

    RudderDataResidencyManager(@Nullable RudderServerConfig serverConfig, @NonNull RudderConfig config) {
        if (serverConfig != null && serverConfig.source != null) {
            this.dataResidencyUrls = serverConfig.source.dataResidencyUrls;
        }
        this.rudderDataResidencyServer = config.getDataResidencyServer();
    }

    /**
     * This API will fetch the dataPlane URL present from the sourceConfig based on the Residency Server region.
     *
     * <p>Preference of dataPlane URL is based on below logic:</p>
     * <ul>
     *     <li>First look for the Residency server (which was set while SDK initialisation) in the source config.</li>
     *     <li>If Residency server was not set while SDK initialisation then fallback to default US residency server.</li>
     *     <li>If residency server list is empty or null then fallback to dataPlane URL (which was set while SDK initialisation).</li>
     *     <li>If dataPlane URL was not set while SDK initialisation then as a last resort fallback to the default dataPlane URL.</li>
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
     *     }
     * </pre>
     *
     * @return Data Residency URL if present otherwise null
     */
    @Nullable
    String getDataResidencyUrl() {
        if (rudderDataResidencyServer == RudderDataResidencyServer.US) {
            return handleDefaultServer();
        } else {
            return handleOtherServer(rudderDataResidencyServer);
        }
    }

    /**
     * Handle all server other than US.
     * If passed server is not present then fallback to US server.
     *
     * @param rudderDataResidencyServer Residency server set while SDK initialisation
     */
    @Nullable
    private String handleOtherServer(@NonNull RudderDataResidencyServer rudderDataResidencyServer) {
        String dataResidencyUrl = getDataResidencyUrl(rudderDataResidencyServer.name());
        if (!isEmpty(dataResidencyUrl)) {
            return dataResidencyUrl;
        }
        return handleDefaultServer();
    }

    /**
     * Handle default server US.
     */
    @Nullable
    private String handleDefaultServer() {
        return getDataResidencyUrl(RudderDataResidencyServer.US.name());
    }

    /**
     * Look for the region in the data residency list.
     *
     * @param region Data Residency region which is set while SDK initialisation.
     * @return Corresponding residency URL if present otherwise null.
     */
    @VisibleForTesting
    @Nullable
    String getDataResidencyUrl(@NonNull String region) {
        if (isEmpty(dataResidencyUrls) ||
                !dataResidencyUrls.containsKey(region) || isEmpty(dataResidencyUrls.get(region))) {
            return null;
        }
        for (Map<String, Object> dataResidencyUrl : dataResidencyUrls.get(region)) {
            ResidencyUrl residencyUrl = getResidencyUrl(dataResidencyUrl);
            if (residencyUrl.defaultTo) {
                if (!isEmpty(residencyUrl.url)) {
                    return appendSlashToUrl(residencyUrl.url);
                }
                // It is decided that for any region there will be only one url defaulted to true
                break;
            }
        }
        return null;
    }

    private static class ResidencyUrl {
        String url = null;
        boolean defaultTo = false;
    }

    @NonNull
    private ResidencyUrl getResidencyUrl(Map<String, Object> dataResidencyUrl) {
        ResidencyUrl residencyUrl = new ResidencyUrl();
        if (isEmpty(dataResidencyUrl)) {
            return residencyUrl;
        }
        if (dataResidencyUrl.containsKey("url")) {
            residencyUrl.url = getString(dataResidencyUrl.get("url"));
        }
        if (dataResidencyUrl.containsKey("default")) {
            residencyUrl.defaultTo = getBoolean(dataResidencyUrl.get("default"));
        }
        return residencyUrl;
    }
}
