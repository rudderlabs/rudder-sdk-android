package com.rudderstack.android.sdk.core;


import static org.hamcrest.MatcherAssert.assertThat;
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

import java.util.concurrent.atomic.AtomicBoolean;

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
        PowerMockito.when(RudderClient.class, "getOptOutStatus").thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });
        config = PowerMockito.mock(RudderConfig.class);
        PowerMockito.whenNew(RudderConfig.class).withNoArguments().thenReturn(config);
        repository = PowerMockito.mock(EventRepository.class);
        PowerMockito.whenNew(EventRepository.class).withAnyArguments().thenReturn(repository);

    }

    private long lastInvokedAt = 0L;
    private int numberOfTimesFlushSyncCalled = 0;

    @Test
    public void flush() throws Exception {
        final int threadCount = 16;
        final AtomicBoolean isDone = new AtomicBoolean(false);

        PowerMockito.when(repository, "flushSync").thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                assertThat((System.currentTimeMillis() - lastInvokedAt), Matchers.greaterThan(1000L));
                lastInvokedAt = System.currentTimeMillis();
                System.out.println("last invoked " + lastInvokedAt);
                ++numberOfTimesFlushSyncCalled;
                //we assume, the threads have all started by now
                // this should be the the last thread or the first thread
                assertThat(numberOfTimesFlushSyncCalled, Matchers.lessThanOrEqualTo(2));

                Thread.sleep(1000);
                isDone.set(numberOfTimesFlushSyncCalled == 2);

                return null;
            }
        });
        final RudderClient client = RudderClient.getInstance(context, "dummy_write_key");

        for (int num = 0; num < threadCount; num++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Running thread : " + Thread.currentThread().getName() + " \ncalling at: " + System.currentTimeMillis());
                    client.flush();
                }
            }, "t-num-" + num).start();
        }
        //if within 10 secs other thread calls, that will be caught
        Thread.sleep(9000);
//        await().atMost(10, SECONDS).untilTrue(isDone);
        assertThat(isDone.get(), Matchers.is(true));
    }

    @After
    public void clearMocks() {
        Mockito.framework().clearInlineMocks();
    }
}
