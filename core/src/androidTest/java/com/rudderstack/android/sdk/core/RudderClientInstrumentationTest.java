package com.rudderstack.android.sdk.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RudderClientInstrumentationTest {
    Context context;
    RudderConfig config;
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        config = new RudderConfig.Builder()
                .withDataPlaneUrl("DATA_PLANE_URL")
                .build();
    }
    @Test
    public void anonymousIdIsValidWhenNotSetExplicitly() {
        RudderClient rudderClient = RudderClient.getInstance(context, "WRITE_KEY", config);

        String anonymousId = rudderClient.getAnonymousId();

        assertNotNull(anonymousId);
        assertNotEquals(0, anonymousId.length());
    }

    @Test
    public void anonymousIdIsValidWhenExplicitlySetAfterSDKInit() {
        RudderClient rudderClient = RudderClient.getInstance(context, "WRITE_KEY", config);
        String customAnonymousId = "CUSTOM_ANONYMOUS_ID";
        RudderClient.putAnonymousId(customAnonymousId);

        String anonymousId = rudderClient.getAnonymousId();

        assertEquals(customAnonymousId, anonymousId);
    }

    @Test
    public void anonymousIdIsValidWhenExplicitlySetBeforeSDKInit() {
        String customAnonymousId = "CUSTOM_ANONYMOUS_ID";
        RudderClient.putAnonymousId(customAnonymousId);
        RudderClient rudderClient = RudderClient.getInstance(context, "WRITE_KEY", config);

        String anonymousId = rudderClient.getAnonymousId();

        assertEquals(customAnonymousId, anonymousId);
    }

    @Test
    public void anonymousIdIsValidWhenChangedMultipleTimeAfterSDKInit() {
        RudderClient rudderClient = RudderClient.getInstance(context, "WRITE_KEY", config);
        String firstCustomAnonymousId = "FIRST_CUSTOM_ANONYMOUS_ID";
        RudderClient.putAnonymousId(firstCustomAnonymousId);

        String anonymousIdChangedFirstTime = rudderClient.getAnonymousId();

        String secondCustomAnonymousId = "SECOND_CUSTOM_ANONYMOUS_ID";
        RudderClient.putAnonymousId(secondCustomAnonymousId);

        String anonymousIdChangedSecondTime = rudderClient.getAnonymousId();

        assertEquals(firstCustomAnonymousId, anonymousIdChangedFirstTime);
        assertEquals(secondCustomAnonymousId, anonymousIdChangedSecondTime);
    }
}
