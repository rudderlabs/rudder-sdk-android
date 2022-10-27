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
@PrepareForTest({RudderDataResidency.class})
public class RudderDataResidencyTest {
    private RudderDataResidency rudderDataResidency;
    private RudderServerConfig serverConfig;
    private RudderConfig config;
    private final String usDataPlaneUrl = "https://us-dataplane.com";
    private final String euDataPlaneUrl = "https://eu-dataplane.com";
    private Map<String, String> dataResidencyUrls = new HashMap<>();
    private DataResidencyServer dataResidencyServer;

    @Before
    public void setup() throws Exception {
        serverConfig = PowerMockito.mock(RudderServerConfig.class);
        config = spy(PowerMockito.mock(RudderConfig.class));
        dataResidencyServer = PowerMockito.mock(DataResidencyServer.class);

        when(config, "getDataResidencyServer").thenReturn(DataResidencyServer.US);

        rudderDataResidency = new RudderDataResidency(serverConfig, config);

        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
    }

    @Test
    public void USNull_DPEmpty_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void USNull_DPValid_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void USValid_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void USValid_DPValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void USEmpty_DPEmpty_Result_Default() {
        // 1
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());

        // 2
        config = new RudderConfig();
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUValid_DPEmpty_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUNull_DPEmpty_Result_Default() {
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUValid_DPValid_Result_EUUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUNull_DPValid_Result_DPUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUNull_USValid_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void EUNull_USValid_DPValid_Result_USUrl() {
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void defaultResidency_DPEmpty_Result_USUrl() {
        config = new RudderConfig();
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void dataResidencyIsNull() {
        // 1
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.US);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        rudderDataResidency.dataResidencyUrls = null;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());

        // 2
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        rudderDataResidency.dataResidencyUrls = null;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());

        // 3
        config = new RudderConfig();
        config.setDataPlaneUrl(usDataPlaneUrl);
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());

        // 4
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        dataResidencyUrls = new HashMap<>();
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());

        // 5
        config = new RudderConfig();
        config.setDataResidencyServer(DataResidencyServer.EU);
        rudderDataResidency = new RudderDataResidency(serverConfig, config);
        rudderDataResidency.dataResidencyUrls = null;
        rudderDataResidency.processDataPlaneUrl();

        assertNull(rudderDataResidency.getDataPlaneUrl());
    }

    @Test
    public void handleDataPlaneUrl() {
        RudderDataResidency rudderDataResidency;
        rudderDataResidency = spy(this.rudderDataResidency);

        doNothing().when(rudderDataResidency).handleDefaultServer();
        rudderDataResidency.processDataPlaneUrl();
        verify(rudderDataResidency, times(1)).processDataPlaneUrl();
        verify(rudderDataResidency, times(1)).handleDefaultServer();

        this.rudderDataResidency.dataResidencyServer = DataResidencyServer.EU;
        rudderDataResidency = spy(this.rudderDataResidency);
        rudderDataResidency.processDataPlaneUrl();
        doNothing().when(rudderDataResidency).handleOtherServer(ArgumentMatchers.<DataResidencyServer>any());
        verify(rudderDataResidency, times(1)).processDataPlaneUrl();
        verify(rudderDataResidency, times(1)).handleOtherServer(DataResidencyServer.EU);
    }

    @Test
    public void handleOtherServer() throws Exception {
        RudderDataResidency rudderDataResidency = spy(this.rudderDataResidency);

        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidency, "getDataResidencyUrl", anyString()).thenReturn(euDataPlaneUrl);
        rudderDataResidency.handleOtherServer(dataResidencyServer);
        verify(rudderDataResidency, times(1)).setDataPlaneUrl(euDataPlaneUrl);

        config = spy(PowerMockito.mock(RudderConfig.class));
        this.rudderDataResidency = new RudderDataResidency(serverConfig, config);
        rudderDataResidency = spy(this.rudderDataResidency);
        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidency, "getDataResidencyUrl", anyString()).thenReturn(null);
        rudderDataResidency.handleOtherServer(dataResidencyServer);
        verify(config, times(0)).setDataPlaneUrl(anyString());
        verify(rudderDataResidency, times(1)).handleDefaultServer();
    }

    @Test
    public void handleDefaultServer() throws Exception {
        RudderDataResidency rudderDataResidency = spy(this.rudderDataResidency);

        doNothing().when(config).setDataPlaneUrl(anyString());

        when(rudderDataResidency, "getDataResidencyUrl", anyString()).thenReturn(euDataPlaneUrl);
        rudderDataResidency.handleDefaultServer();
        verify(rudderDataResidency, times(1)).setDataPlaneUrl(euDataPlaneUrl);

        config = spy(PowerMockito.mock(RudderConfig.class));
        this.rudderDataResidency = new RudderDataResidency(serverConfig, config);
        rudderDataResidency = spy(this.rudderDataResidency);
        doNothing().when(config).setDataPlaneUrl(anyString());
        when(rudderDataResidency, "getDataResidencyUrl", anyString()).thenReturn(null);
        rudderDataResidency.handleDefaultServer();
        verify(config, times(0)).setDataPlaneUrl(anyString());
    }

    @Test
    public void getDataResidencyUrl() {
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        String region;

        region = "US";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));
        region = "us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));
        region = "Us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));

        region = "eu";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));
        region = "EU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));
        region = "eU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));

        region = "IN";
        assertNull(rudderDataResidency.getDataResidencyUrl(region));

        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", euDataPlaneUrl);
        dataResidencyUrls.put("US", null);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        region = "US";
        assertNull(rudderDataResidency.getDataResidencyUrl(region));
        region = "eU";
        assertEquals(euDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));

        dataResidencyUrls = new HashMap<>();
        dataResidencyUrls.put("EU", null);
        dataResidencyUrls.put("US", usDataPlaneUrl);
        rudderDataResidency.dataResidencyUrls = dataResidencyUrls;
        region = "EU";
        assertNull(rudderDataResidency.getDataResidencyUrl(region));
        region = "Us";
        assertEquals(usDataPlaneUrl + '/', rudderDataResidency.getDataResidencyUrl(region));

        rudderDataResidency.dataResidencyUrls = null;
        region = "EU";
        assertNull(rudderDataResidency.getDataResidencyUrl(region));
        region = "US";
        assertNull(rudderDataResidency.getDataResidencyUrl(region));
    }
}