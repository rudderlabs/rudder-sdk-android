package com.rudderlabs.android.sdk.core;

import android.app.Application;
import android.text.TextUtils;

import androidx.test.core.app.ApplicationProvider;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@Config(sdk = 28)
@RunWith(RobolectricTestRunner.class)
public class RudderClientTest {

    private Application application;
    @Mock
    EventRepository repository;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        initMocks(this);
    }

    /**
     * Whether rudderClient is initiating with null values. events should not be passed
     */
    @Test
    public void getInstance() {
        RudderClient client = RudderClient.getInstance(null, null);
        assertNotNull(client);
    }

    /**
     * Test whether rudderClient is initiating with wrong endPointUrl and
     * wrong writeKey
     */
    @Test
    public void getInstance1() {
        RudderConfig.Builder configBuilder = new RudderConfig.Builder();
        configBuilder.withEndPointUri(TestConstants.WRONG_END_POINT_URL);
        configBuilder.withLogLevel(RudderLogger.RudderLogLevel.VERBOSE);

        RudderClient client = RudderClient.getInstance(this.application, TestConstants.WRONG_WRITE_KEY, configBuilder);

        assertNotNull(client);
    }

    /**
     * Test whether RudderClient is initiating with correct endPointUrl and wrong writeKey
     */
    @Test
    public void getInstance2() {
        RudderConfig.Builder configBuilder = new RudderConfig.Builder();
        configBuilder.withEndPointUri(TestConstants.CORRECT_END_POINT_URL);
        configBuilder.withLogLevel(RudderLogger.RudderLogLevel.VERBOSE);

        RudderClient client = RudderClient.getInstance(this.application, TestConstants.WRONG_WRITE_KEY, configBuilder);

        assertNotNull(client);
    }

    /**
     * Test whether client is initiated with correct endPointUrl and correct writeKey
     */
    @Test
    public void getInstance3() {
        RudderConfig.Builder configBuilder = new RudderConfig.Builder();
        configBuilder.withEndPointUri(TestConstants.CORRECT_END_POINT_URL);
        configBuilder.withLogLevel(RudderLogger.RudderLogLevel.VERBOSE);

        RudderClient client = RudderClient.getInstance(this.application, TestConstants.CORRECT_WRITE_KEY);

        assertNotNull(client);
    }

    @Test
    public void with() {
    }

    private RudderClient initiateClientCorrectly() {
        RudderClient client = RudderClient.getInstance(
                this.application,
                TestConstants.CORRECT_WRITE_KEY,
                new RudderConfig.Builder()
                        .withEndPointUri(TestConstants.CORRECT_END_POINT_URL)
                        .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
        );
        assertNotNull(client);
        return client;
    }

    @Test
    public void getApplication() {
        RudderClient client = initiateClientCorrectly();
        Application sdkApplication = client.getApplication();

        assertNotNull(sdkApplication);
    }

    @Test
    public void track() {
        RudderClient client = initiateClientCorrectly();

        client.track("foo");

        verify(repository).dump(argThat(
                new TypeSafeMatcher<RudderMessage>() {
                    @Override
                    public void describeTo(Description description) {
                        System.out.println(description);
                    }

                    @Override
                    protected boolean matchesSafely(RudderMessage item) {
                        System.out.println(item.getEventName());
                        return false;
                    }
                }
        ));
    }

    @Test
    public void track1() {
    }

    @Test
    public void track2() {
    }

    @Test
    public void track3() {
    }

    @Test
    public void track4() {
    }

    @Test
    public void screen() {
    }

    @Test
    public void screen1() {
    }

    @Test
    public void screen2() {
    }

    @Test
    public void screen3() {
    }

    @Test
    public void screen4() {
    }

    @Test
    public void screen5() {
    }

    @Test
    public void identify() {
    }

    @Test
    public void identify1() {
    }

    @Test
    public void identify2() {
    }

    @Test
    public void identify3() {
    }

    @Test
    public void identify4() {
    }

    @Test
    public void identify5() {
    }

    @Test
    public void alias() {
    }

    @Test
    public void alias1() {
    }

    @Test
    public void group() {
    }

    @Test
    public void group1() {
    }

    @Test
    public void group2() {
    }

    @Test
    public void setSingletonInstance() {
    }

    @Test
    public void getRudderContext() {
    }

    @Test
    public void reset() {
    }

    @Test
    public void onIntegrationReady() {
    }

    @Test
    public void optOut() {
    }

    @Test
    public void shutdown() {
    }
}