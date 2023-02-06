package com.rudderstack.android.sdk.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import android.app.Application;
import android.content.Context;
import android.webkit.URLUtil;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RudderClient.class, URLUtil.class})
//@Config(minSdk = 16, maxSdk = 28)
public class RudderClientTest {
    EventRepository repository;
    Application application = PowerMockito.mock(Application.class);
    Context context = PowerMockito.mock(Context.class);
    RudderConfig config;

    @Before
    public void setUp() throws Exception {
        PowerMockito.when(context, "getApplicationContext").thenAnswer(new Answer<Application>() {
            @Override
            public Application answer(InvocationOnMock invocation) throws Throwable {
                return application;
            }
        });
        PowerMockito.spy(URLUtil.class);
        PowerMockito.when(URLUtil.class, "isValidUrl", anyString()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return true;
            }
        });
        PowerMockito.spy(RudderClient.class);
        PowerMockito.when(RudderClient.class, "getOptOutStatus").thenAnswer((Answer<Boolean>) invocation -> false);
        config = PowerMockito.mock(RudderConfig.class);
        PowerMockito.whenNew(RudderConfig.class).withNoArguments().thenReturn(config);
        repository = PowerMockito.mock(EventRepository.class);
        PowerMockito.whenNew(EventRepository.class).withAnyArguments().thenReturn(repository);

    }

    @Test
    public void testFlushThrottling() throws Exception {
        final RudderClient client = RudderClient.getInstance(context, "dummy_write_key");

        final AtomicInteger flushSyncCalls = new AtomicInteger();
        PowerMockito.when(repository, "flushSync").thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws InterruptedException {
                int countBeforeSleep = flushSyncCalls.incrementAndGet();
                System.out.println("System.out.println: " + flushSyncCalls.get());
                Thread.sleep(500L);
                assertThat(flushSyncCalls.get(), Matchers.is(countBeforeSleep));
                return null;
            }
        });

        for (int counter = 0; counter < 6; counter++) {
            client.flush();
            Thread.sleep(105);
        }
        //105millis waiting x 6, whereas flush sync waits everytime for 500millis
        //flushSyncCalls hence, should be exactly 2
        assertThat(flushSyncCalls.get(), Matchers.is(2));
    }

    @After
    public void clearMocks() {
        RudderClient.getInstance().shutdown();
        RudderClient.setSingletonInstance(null);
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void consentFilterTest() throws Exception {

        final RudderClient client = RudderClient.getInstance(context, "dummy_write_key");
        RudderMessage rudderMessage = new RudderMessageBuilder().setEventName("e-1").setUserId("c-1").build();
        client.track(rudderMessage);
        //we only check if messages reach event repository.processMessage
        Mockito.verify(repository).processMessage(rudderMessage);
    }
}
