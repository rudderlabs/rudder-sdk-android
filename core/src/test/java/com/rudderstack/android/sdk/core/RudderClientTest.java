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

    private int numberOfTimesFlushSyncCalled = 0;

    @Test
    public void makeEventsToTestThrottlingOfFlushApiCalls() throws Exception {
        final AtomicBoolean isDone = new AtomicBoolean(false);
        final AtomicInteger blockMoreThan2FlushApiCall = new AtomicInteger(0);

        PowerMockito.when(repository, "flushSync").thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ++numberOfTimesFlushSyncCalled;
                System.out.println("System.out.println: " + numberOfTimesFlushSyncCalled);

                blockMoreThan2FlushApiCall.addAndGet(1);

                isDone.set(numberOfTimesFlushSyncCalled == 2);

                // block the first flush API call, until unblocked in the third flush API call
                while (blockMoreThan2FlushApiCall.get() < 1);

                return null;
            }
        });

        final RudderClient client = RudderClient.getInstance(context, "dummy_write_key");
        // Making first Flush API call
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running thread : " + Thread.currentThread().getName());
                client.flush();
            }
        }).start();

        // Making second Flush API call - it'll not be blocked, since Executor service queue list is 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running thread : " + Thread.currentThread().getName());
                client.flush();
            }
        }).start();

        // This flush API call will technically replace the second one (There is no way to verify that directly)
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running thread : " + Thread.currentThread().getName());
                client.flush();
                // wait until first flush API starts executing
                while (!(blockMoreThan2FlushApiCall.get() == 1));
                // unblock the flush API call
                blockMoreThan2FlushApiCall.addAndGet(1);
            }
        }).start();

        // wait until all the Flush API call has been made
        while (blockMoreThan2FlushApiCall.get() < 3);
        assertThat(isDone.get(), Matchers.is(true));
    }

    @After
    public void clearMocks() {
        Mockito.framework().clearInlineMocks();
    }
}
