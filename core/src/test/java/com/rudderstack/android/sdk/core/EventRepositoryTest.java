package com.rudderstack.android.sdk.core;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.rudderstack.android.sdk.core.consent.ConsentFilterHandler;
import com.rudderstack.android.sdk.core.consent.RudderConsentFilter;
import com.rudderstack.android.sdk.core.util.Utils;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FlushUtils.class,
        RudderLogger.class,
        TextUtils.class, Utils.class})
public class EventRepositoryTest {

    // data to be used
    private final List<Integer> messageIds = new ArrayList<Integer>(ImmutableList.of(1, 2, 3, 4, 5));
    private final List<String> messages = new ArrayList<String>(ImmutableList.of("{\"message\":\"m-1\"}",
            "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}"));

    //database manager mock
    DBPersistentManager dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);

    @Before
    public void setup() throws Exception {
        // creating static mocks
        PowerMockito.spy(FlushUtils.class);

        //mocking timestamp
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp"
                )
                .thenAnswer((Answer<String>) invocation -> "2022-03-14T06:46:41.365Z");
    }
    @After
    public void clearMocks() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * test flushEventsToServer is called properly
     */
    @Test
    public void flush() throws Exception {

        PowerMockito.when(dbPersistentManager, "fetchAllEventsFromDB", anyList(), anyList())
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        ((ArrayList) invocation.getArgument(0)).addAll(messageIds);
                        ((ArrayList) invocation.getArgument(1)).addAll(messages);
                        return null;
                    }
                });
        PowerMockito.when(FlushUtils.class, "flushEventsToServer",
                        anyString(), anyString(), anyString(), anyString()
                )
                .thenAnswer(new Answer<Utils.NetworkResponses>() {
                    @Override
                    public Utils.NetworkResponses answer(InvocationOnMock invocation) throws Throwable {
                        return Utils.NetworkResponses.SUCCESS;
                    }
                });

        //expectation
        String expectedPayload = "{\n" +
                "  \"sentAt\": \"2022-03-14T06:46:41.365Z\",\n" +
                "  \"batch\": [\n" +
                "    {\n" +
                "      \"message\": \"m-1\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-2\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-3\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-4\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-5\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        //when
        boolean result = FlushUtils.flush(false, null,
                30, "api.rudderstack.com/", dbPersistentManager
                , "auth_key", "anon_id");


        //verify flushEventsToServer is called once with proper arguments
        //we use argument captor, cause we would need to remove spaces from argument
        PowerMockito.verifyStatic(FlushUtils.class, Mockito.times(1));
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg4 = ArgumentCaptor.forClass(String.class);

        FlushUtils.flushEventsToServer(
                arg1.capture(),
                arg2.capture(),
                arg3.capture(),
                arg4.capture()
//                Mockito.eq(expectedPayload.replace("\n", "").replace(" ", "")),
//                Mockito.eq("api.rudderstack.com/"),
//                Mockito.eq("auth_key"),
//                Mockito.eq("anon_id")
        );
        assertThat(result, Matchers.is(true));
        System.out.println(arg1.getValue());
        assertThat(arg1.getValue().replace(" ", ""),
                Matchers.is(expectedPayload.replace("\n", "").replace(" ", "")));
        System.out.println(arg2.getValue());
        assertThat(arg2.getValue().replace(" ", ""), Matchers.is("api.rudderstack.com/"));
        System.out.println(arg3.getValue());
        assertThat(arg3.getValue(), Matchers.is("auth_key"));
        System.out.println(arg4.getValue());
        assertThat(arg4.getValue(), Matchers.is("anon_id"));
    }

    private int dbFetchCalled = 0;

    @Test
    public void testSynchronicity() throws Exception {
        final AtomicInteger threadsCalledDb = new AtomicInteger(0);
        //we add a sleep to db fetch to check for synchronicity
        // take a class level variable to check for thread access
        PowerMockito.when(dbPersistentManager, "fetchAllEventsFromDB", anyList(), anyList())
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        ++dbFetchCalled;
                        System.out.println("fetchAllEvents called by: " + Thread.currentThread().getName());
                        //assert if called by multiple thread
                        assertThat(dbFetchCalled, Matchers.lessThan(2));
                        ((ArrayList) invocation.getArgument(0)).addAll(messageIds);
                        ((ArrayList) invocation.getArgument(1)).addAll(messages);
                        Thread.sleep(100);
                        --dbFetchCalled;
                        assertThat(dbFetchCalled, Matchers.lessThan(1));
                        System.out.println("return from fetchAllEvents by: " + Thread.currentThread().getName());
                        threadsCalledDb.incrementAndGet();
                        return null;
                    }
                });
        PowerMockito.when(FlushUtils.class, "flushEventsToServer",
                        anyString(), anyString(), anyString(), anyString()
                )
                .thenAnswer(new Answer<Utils.NetworkResponses>() {
                    @Override
                    public Utils.NetworkResponses answer(InvocationOnMock invocation) throws Throwable {
                        return Utils.NetworkResponses.SUCCESS;
                    }
                });

        //starting multiple threads to access the same.
        final int numberOfThreads = 8;
        for (int n = 0; n < numberOfThreads; n++) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    FlushUtils.flush(false, null,
                            30, "api.rudderstack.com/", dbPersistentManager
                            , "auth_key", "anon_id");
                }
            }, "flush-thread-" + n) {
                @Override
                public synchronized void start() {
                    super.start();
                    System.out.println("\nStarting thread: " + getName());
                }
            };
            t.start();
//            t.join();
        }
        //await until finished
        await().atMost(10, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return threadsCalledDb.get() == numberOfThreads;
            }
        });
    }

    @Test
    public void applyRudderOptionsToMessageIntegrations() {
        EventRepository repo = new EventRepository();

        RudderOption options = new RudderOption();
        options.putIntegration("Dummy-Integration-1", false);
        options.putIntegration("Dummy-Integration-2", false);

        RudderMessage message = new RudderMessageBuilder()
                .setUserId("u-id")
                .setRudderOption(options)
                .build();
        repo.applyRudderOptionsToMessageIntegrations(message);

        assertThat("All: true should be added", message.getIntegrations(),
                allOf(hasEntry("Dummy-Integration-1", false),
                        hasEntry("Dummy-Integration-2", false),
                        hasEntry("All", true)
                ));
    }

    @Test
    public void applySessionTracking() {
        RudderUserSession userSession = PowerMockito.mock(RudderUserSession.class);
        PowerMockito.doReturn(123L).when(userSession).getSessionId();

        RudderConfig mockConfig = PowerMockito.mock(RudderConfig.class);
        PowerMockito.doReturn(false).when(mockConfig).isTrackLifecycleEvents();
//        Mockito.doReturn()
        EventRepository repo = new EventRepository();
        RudderMessage message = new RudderMessageBuilder().setUserId("u-1").build();
        RudderMessage spyMessage = PowerMockito.spy(message);
        PowerMockito.doNothing().when(spyMessage).setSession(userSession);

        repo.applySessionTracking(spyMessage, mockConfig, userSession);
        Mockito.verify(spyMessage).setSession(userSession);
    }

    @Test
    public void getDataPlaneUrlWrtResidencyConfig() {
        EventRepository repo = new EventRepository();
        RudderConfig rudderConfig = new RudderConfig.Builder()
                .build();
        RudderServerConfig serverConfig = PowerMockito.mock(RudderServerConfig.class);
        String dataPlaneUrl = repo.getDataPlaneUrlWrtResidencyConfig(serverConfig, rudderConfig);
        assertThat(dataPlaneUrl, Matchers.nullValue());
    }

    @Test
    public void getDataPlaneUrlWrtResidencyConfigWithDataPlaneUrl() {
        EventRepository repo = new EventRepository();
        RudderConfig rudderConfig = new RudderConfig.Builder()
                                        .withDataPlaneUrl("https://random.dataplane.rudderstack.com")
                                        .build();
        RudderServerConfig serverConfig = new RudderServerConfig();
        String dataPlaneUrl = repo.getDataPlaneUrlWrtResidencyConfig(serverConfig, rudderConfig);
        assertEquals(dataPlaneUrl, "https://random.dataplane.rudderstack.com/");
    }

}