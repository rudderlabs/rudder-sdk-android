package com.rudderstack.android.sdk.core;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RudderDataResidencyManager.class})
public class RudderDataResidencyManagerTest {
    private RudderDataResidencyManager rudderDataResidencyManager;
    private RudderServerConfig serverConfig;
    private RudderConfig config;
    private final String usUrl = "https://us-dataplane.com";
    private final String euUrl = "https://eu-dataplane.com";
    private final List<Map<String, Object>> usDataPlaneUrl = getDataResidencyUrl(usUrl, true);
    private final List<Map<String, Object>> euDataPlaneUrl = getDataResidencyUrl(euUrl, true);
    private Map<String, List<Map<String, Object>>> dataResidencyUrls = new HashMap<>();

    private List<Map<String, Object>> getDataResidencyUrl(@Nullable final String url, @Nullable final Object defaultTo) {
        final Map<String, Object> residencyUrl = new HashMap<String, Object>() {
            {
                {
                    put("url", url);
                    put("default", defaultTo);
                }
            }
        };
        return new LinkedList<Map<String, Object>>() {
            {
                add(residencyUrl);
            }
        };
    }

    @Before
    public void setup() throws Exception {
        serverConfig = PowerMockito.mock(RudderServerConfig.class);
        config = PowerMockito.mock(RudderConfig.class);

        when(config, "getDataResidencyServer").thenReturn(RudderDataResidencyServer.US);

        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
    }

    @Test
    public void EUUrl_DefaultToIsFalse() {
        List<Map<String, Object>> usDataPlaneUrl = getDataResidencyUrl(usUrl, true);
        List<Map<String, Object>> euDataPlaneUrl = getDataResidencyUrl(euUrl, false);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + "/", rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USUrl_DefaultToIsFalse() {
        List<Map<String, Object>> usDataPlaneUrl = getDataResidencyUrl(usUrl, false);
        List<Map<String, Object>> euDataPlaneUrl = getDataResidencyUrl(euUrl, true);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(euUrl + "/", rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUUrl_USUrl_DefaultToIsFalse() {
        List<Map<String, Object>> usDataPlaneUrl = getDataResidencyUrl(usUrl, false);
        List<Map<String, Object>> euDataPlaneUrl = getDataResidencyUrl(euUrl, false);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USNull_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USEmpty_Result_Default() {
        // Explicitly setting the residency server to US
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Default setting of residency server to US
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUValid_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(euUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_USValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void defaultResidency_Result_USUrl() {
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void dataResidencyIsNull() {
        // Residency server set to US
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Residency server set to EU
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Default residency server
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void getDataResidencyUrl() {
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        String region;

        // Different cases of US region
        region = "US";
        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "us";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "Us";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));

        // Different cases of EU region
        region = "eu";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "EU";
        assertEquals(euUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "eU";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));

        // Region not present in the residency list
        region = "IN";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));

        // When EU is present but not US
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        region = "US";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "EU";
        assertEquals(euUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        // When US is present but not EU
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        region = "EU";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "US";
        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        // When residency url is null
        rudderDataResidencyManager.dataResidencyUrls = null;
        region = "EU";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "US";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
    }
}