package com.rudderstack.android.sdk.core;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

import androidx.annotation.Nullable;

import org.hamcrest.Matchers;
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
    private final List<RudderDataResidencyUrls> usDataPlaneUrl = getDataResidencyUrl(usUrl, true);
    private final List<RudderDataResidencyUrls> euDataPlaneUrl = getDataResidencyUrl(euUrl, true);
    private Map<RudderDataResidencyServer, List<RudderDataResidencyUrls>> dataResidencyUrls = new HashMap<>();

    private List<RudderDataResidencyUrls> getDataResidencyUrl(@Nullable final String url, final boolean defaultTo) {
        final RudderDataResidencyUrls rudderDataResidencyUrls = new RudderDataResidencyUrls();
        rudderDataResidencyUrls.url = url;
        rudderDataResidencyUrls.defaultTo = defaultTo;
        
        return new LinkedList<RudderDataResidencyUrls>() {
            {
                add(rudderDataResidencyUrls);
            }
        };
    }

    @Before
    public void setup() throws Exception {
        serverConfig = PowerMockito.mock(RudderServerConfig.class);
        config = PowerMockito.mock(RudderConfig.class);

        when(config, "getDataResidencyServer").thenReturn(RudderDataResidencyServer.US);

        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
    }

    @Test
    public void EUUrl_DefaultToIsFalse() {
        List<RudderDataResidencyUrls> usDataPlaneUrl = getDataResidencyUrl(usUrl, true);
        List<RudderDataResidencyUrls> euDataPlaneUrl = getDataResidencyUrl(euUrl, false);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + "/", rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USUrl_DefaultToIsFalse() {
        List<RudderDataResidencyUrls> usDataPlaneUrl = getDataResidencyUrl(usUrl, false);
        List<RudderDataResidencyUrls> euDataPlaneUrl = getDataResidencyUrl(euUrl, true);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(euUrl + "/", rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUUrl_USUrl_DefaultToIsFalse() {
        List<RudderDataResidencyUrls> usDataPlaneUrl = getDataResidencyUrl(usUrl, false);
        List<RudderDataResidencyUrls> euDataPlaneUrl = getDataResidencyUrl(euUrl, false);

        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USNull_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void USEmpty_Result_Default() {
        // Explicitly setting the residency server to US
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Default setting of residency server to US
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUValid_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(euUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, null);
        dataResidencyUrls.put(RudderDataResidencyServer.US, null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, null);
        dataResidencyUrls.put(RudderDataResidencyServer.US, null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void EUNull_USValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, null);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void defaultResidency_Result_USUrl() {
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertEquals(usUrl + '/', rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void dataResidencyIsNull() {
        // Residency server set to US
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Residency server set to EU
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());

        // Default residency server
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(config);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.getDataResidencyUrl());
    }

    @Test
    public void fetchUrlFromRegion() {
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        // Different cases of US region
        assertEquals(usUrl + '/', rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.US));

        assertEquals(euUrl + '/', rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.EU));

        // When EU is present but not US
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, euDataPlaneUrl);
        dataResidencyUrls.put(RudderDataResidencyServer.US, null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.US));
        assertEquals(euUrl + '/', rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.EU));

        // When US is present but not EU
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put(RudderDataResidencyServer.EU, null);
        dataResidencyUrls.put(RudderDataResidencyServer.US, usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;

        assertNull(rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.EU));
        assertEquals(usUrl + '/', rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.US));

        // When residency url is null
        rudderDataResidencyManager.dataResidencyUrls = null;

        assertNull(rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.EU));
        assertNull(rudderDataResidencyManager.fetchUrlFromRegion(RudderDataResidencyServer.US));
    }


    @Test
    public void getDataPlaneUrlWrtResidencyConfig() {
        RudderConfig rudderConfig = new RudderConfig.Builder()
                .build();
        RudderServerConfig serverConfig = PowerMockito.mock(RudderServerConfig.class);
        rudderDataResidencyManager = new RudderDataResidencyManager(rudderConfig);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void getDataPlaneUrlWrtResidencyConfigWithDataPlaneUrl() {
        RudderConfig rudderConfig = new RudderConfig.Builder()
                .withDataPlaneUrl("https://random.dataplane.rudderstack.com")
                .build();
        RudderServerConfig serverConfig = new RudderServerConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(rudderConfig);
        rudderDataResidencyManager.setDataResidencyUrls(serverConfig);
        String dataPlaneUrl = rudderDataResidencyManager.getDataPlaneUrl();
        assertEquals("https://random.dataplane.rudderstack.com/", dataPlaneUrl);
    }
}