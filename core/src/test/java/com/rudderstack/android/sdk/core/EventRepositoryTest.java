package com.rudderstack.android.sdk.core;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.rudderstack.android.sdk.core.util.Utils;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
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
    private final List<Integer> messageIdsParams = new ArrayList<Integer>(5);
    private final List<String> messagesParams = new ArrayList<String>(5);

    //database manager mock
    DBPersistentManager dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);

    @Before
    public void setup() throws Exception {
        messageIdsParams.clear();
        messagesParams.clear();


        // creating static mocks
        PowerMockito.spy(FlushUtils.class);

        //mocking timestamp
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp"
        )
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return "2022-03-14T06:46:41.365Z";
                    }
                });
    }
    /**
     * test flushEventsToServer is called properly
     */
    @Test
    public void flush() throws Exception {

        PowerMockito.when(dbPersistentManager, "fetchAllEventsFromDB", messageIdsParams, messagesParams)
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        messageIdsParams.addAll(messageIds);
                        messagesParams.addAll(messages);
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
        PowerMockito.when(dbPersistentManager, "fetchAllEventsFromDB", messageIdsParams, messagesParams)
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        ++dbFetchCalled;
                        System.out.println("fetchAllEvents called by: " + Thread.currentThread().getName());
                        //assert if called by multiple thread
                        assertThat(dbFetchCalled, Matchers.lessThan(2));
                        messageIdsParams.addAll(messageIds);
                        messagesParams.addAll(messages);
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
            }, "flush-thread-" + n){
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

    /*public void partialMockTest() throws Exception {
        assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("noMock"));
        PowerMockito.spy(MockSample.class);
        PowerMockito.when(MockSample.class, "returnNotMockIfNotMocked").thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "mocked";
            }
        });
        assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("mocked"));
        assertThat(MockSample.return2IfNotMocked(), Matchers.is(2));

        *//*try (MockedStatic<MockSample> utilities = Mockito.mockStatic(MockSample.class)) {
            utilities.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    MockSample.return2IfNotMocked();
                }
            }).thenReturn(*//**//*f.call(MockSample.class,new Object() , new Object[]{})*//**//*2);
            utilities.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    MockSample.returnNotMockIfNotMocked();
                }
            }).thenReturn("mocked");
            assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("mocked"));
            assertThat(MockSample.return2IfNotMocked(), Matchers.is(2));

        }*//*

//        assertThat(StaticUtils.name()).isEqualTo("Baeldung");
    }*/
}