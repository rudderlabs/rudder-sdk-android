package com.rudderstack.android.sdk.core;

import static org.junit.Assert.*;

import com.google.gson.GsonBuilder;

import org.junit.Test;


public class SourceConfigurationTest {

    private static final String TEST_SOURCE_CONFIGURATION = "{\n" +
            "  \"statsCollection\": {\n" +
            "    \"errors\": {\n" +
            "      \"enabled\": true\n" +
            "    },\n" +
            "    \"metrics\": {\n" +
            "      \"enabled\": true\n" +
            "    }\n" +
            "  }\n" +
            "}";
    @Test
    public void testSourceConfigurationSerialization(){
//        SourceConfiguration sourceConfiguration = new SourceConfiguration(new
//                SourceConfiguration.StatsCollection(new SourceConfiguration.Errors(
//                        true), new SourceConfiguration.Metrics(true)));
//        assertEquals(sourceConfiguration.getStatsCollection().getErrors().isEnabled(), true);
//        assertEquals(sourceConfiguration.getStatsCollection().getMetrics().isEnabled(), true);

        SourceConfiguration config = new GsonBuilder().create().fromJson(TEST_SOURCE_CONFIGURATION, SourceConfiguration.class);
        assertTrue(config.getStatsCollection().getErrors().isEnabled());
        assertTrue(config.getStatsCollection().getMetrics().isEnabled());

    }
}