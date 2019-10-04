package com.rudderlabs.android.sdk.core;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BaseTestCase {
    protected RudderClient rudderClient;
    private static final String TEST_WRITE_KEY = "somewritekey";

    @Before
    public void setup() throws InterruptedException {
        try {
            Context context = ApplicationProvider.getApplicationContext();
            rudderClient = RudderClient.getInstance(context, TEST_WRITE_KEY, new RudderConfigBuilder().withDebug(true));
            Thread.sleep(1000);
        } catch (RudderException e) {
            e.printStackTrace();
        }
    }
}
