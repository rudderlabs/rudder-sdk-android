package com.rudderstack.android.sdk.core;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RudderDataResidencyManager.class})
public class RudderDataResidencyManagerTest {
    private RudderDataResidencyManager rudderDataResidencyManager;
    private RudderServerConfig serverConfig;
    private RudderConfig config;
    private final String usDataPlaneUrl = "https://us-dataplane.com";
    private final String euDataPlaneUrl = "https://eu-dataplane.com";
    private Map<String, String> dataResidencyUrls = new HashMap<>();
    private RudderDataResidencyServer rudderDataResidencyServer;

    @Before
    public void setup() throws Exception {
        serverConfig = PowerMockito.mock(RudderServerConfig.class);
        config = spy(PowerMockito.mock(RudderConfig.class));
        rudderDataResidencyServer = PowerMockito.mock(RudderDataResidencyServer.class);

        when(config, "getDataResidencyServer").thenReturn(RudderDataResidencyServer.US);

        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);

        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", usDataPlaneUrl);
    }

    @Test
    public void USNull_DPEmpty_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void USNull_DPValid_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void USValid_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void USValid_DPValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void USEmpty_DPEmpty_Result_Default() {
        // 1
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());

        // 2
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUValid_DPEmpty_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUNull_DPEmpty_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", null);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUValid_DPValid_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUNull_DPValid_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", null);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUNull_USValid_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", null);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void EUNull_USValid_DPValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", null);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void defaultResidency_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void dataResidencyIsNull() {
        // 1
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.US);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());

        // 2
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());

        // 3
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());

        // 4
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());

        // 5
        config = new RudderConfig();
        config.setDataResidencyServer(RudderDataResidencyServer.EU);
        rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager.dataResidencyUrls = null;
        rudderDataResidencyManager.processDataPlaneUrl();

        assertNull(rudderDataResidencyManager.getDataPlaneUrl());
    }

    @Test
    public void handleDataPlaneUrl() {
        RudderDataResidencyManager rudderDataResidencyManager;
        rudderDataResidencyManager = spy(this.rudderDataResidencyManager);

        doNothing().when(rudderDataResidencyManager).handleDefaultServer();
        rudderDataResidencyManager.processDataPlaneUrl();
        verify(rudderDataResidencyManager, times(1)).processDataPlaneUrl();
        verify(rudderDataResidencyManager, times(1)).handleDefaultServer();

        this.rudderDataResidencyManager.rudderDataResidencyServer = RudderDataResidencyServer.EU;
        rudderDataResidencyManager = spy(this.rudderDataResidencyManager);
        rudderDataResidencyManager.processDataPlaneUrl();
        doNothing().when(rudderDataResidencyManager).handleOtherServer(ArgumentMatchers.<RudderDataResidencyServer>any());
        verify(rudderDataResidencyManager, times(1)).processDataPlaneUrl();
        verify(rudderDataResidencyManager, times(1)).handleOtherServer(RudderDataResidencyServer.EU);
    }

    @Test
    public void handleOtherServer() throws Exception {
        RudderDataResidencyManager rudderDataResidencyManager = spy(this.rudderDataResidencyManager);

        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidencyManager, "getDataResidencyUrl", anyString()).thenReturn(euDataPlaneUrl);
        rudderDataResidencyManager.handleOtherServer(rudderDataResidencyServer);
        verify(rudderDataResidencyManager, times(1)).setDataPlaneUrl(euDataPlaneUrl);

        config = spy(PowerMockito.mock(RudderConfig.class));
        this.rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager = spy(this.rudderDataResidencyManager);
        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidencyManager, "getDataResidencyUrl", anyString()).thenReturn(null);
        rudderDataResidencyManager.handleOtherServer(rudderDataResidencyServer);
        verify(config, times(0)).setDataPlaneUrl(anyString());
        verify(rudderDataResidencyManager, times(1)).handleDefaultServer();
    }

    @Test
    public void handleDefaultServer() throws Exception {
        RudderDataResidencyManager rudderDataResidencyManager = spy(this.rudderDataResidencyManager);

        doNothing().when(config).setDataPlaneUrl(anyString());

        when(rudderDataResidencyManager, "getDataResidencyUrl", anyString()).thenReturn(euDataPlaneUrl);
        rudderDataResidencyManager.handleDefaultServer();
        verify(rudderDataResidencyManager, times(1)).setDataPlaneUrl(euDataPlaneUrl);

        config = spy(PowerMockito.mock(RudderConfig.class));
        this.rudderDataResidencyManager = new RudderDataResidencyManager(serverConfig, config);
        rudderDataResidencyManager = spy(this.rudderDataResidencyManager);
        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidencyManager, "getDataResidencyUrl", anyString()).thenReturn(null);
        rudderDataResidencyManager.handleDefaultServer();
        verify(config, times(0)).setDataPlaneUrl(anyString());
    }

    @Test
    public void getDataResidencyUrl() {
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        String region;

        region = "US";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "Us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        region = "eu";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "EU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "eU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        region = "IN";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));

        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", euDataPlaneUrl);
        dataResidencyUrls.put("us", null);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        region = "US";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "eU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("eu", null);
        dataResidencyUrls.put("us", usDataPlaneUrl);
        rudderDataResidencyManager.dataResidencyUrls = dataResidencyUrls;
        region = "EU";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "Us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidencyManager.getDataResidencyUrl(region));

        rudderDataResidencyManager.dataResidencyUrls = null;
        region = "EU";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
        region = "US";
        assertNull(rudderDataResidencyManager.getDataResidencyUrl(region));
    }
}