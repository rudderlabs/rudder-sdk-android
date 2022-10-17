package com.rudderstack.android.sdk.core;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

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
@PrepareForTest({
        RudderLogger.class,
        TextUtils.class, Utils.class,
RudderNetworkManager.class})
public class EventRepositoryTest {

    // data to be used
    private final List<Integer> messageIds = new ArrayList<>(ImmutableList.of(1, 2, 3, 4, 5));
    private final List<String> messages = new ArrayList<>(ImmutableList.of("{\"message\":\"m-1\"}",
            "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}"));
    private final List<Integer> messageIdsParams = new ArrayList<>(5);
    private final List<String> messagesParams = new ArrayList<>(5);

    //database manager mock
    DBPersistentManager dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
    RudderNetworkManager networkManager = PowerMockito.mock(RudderNetworkManager.class);

    @Before
    public void setup() throws Exception {
        messageIdsParams.clear();
        messagesParams.clear();
        //mocking timestamp
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp"
                )
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) {
                        return "2022-03-14T06:46:41.365Z";
                    }
                });
    }

    /**
     * test flushEventsToServer is called properly
     */
    @Test
    public void flush() throws Exception {
        final RudderNetworkManager.Result mockResult= new RudderNetworkManager.Result(RudderNetworkManager.NetworkResponses.SUCCESS,
                200, "", null);
        /*PowerMockito.when(dbPersistentManager, "fetchAllCloudModeEventsFromDB", messageIdsParams, messagesParams)
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        messageIdsParams.addAll(messageIds);
                        messagesParams.addAll(messages);
                        return null;
                    }
                });*/
        PowerMockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                List<Integer> msgIdParams = (List<Integer>) invocation.getArguments()[0];
                List<String> msgParams = (List<String>) invocation.getArguments()[1];
                msgIdParams.addAll(messageIds);
                msgParams.addAll(messages);
                return null;
            }
        }).when(dbPersistentManager).fetchAllCloudModeEventsFromDB(ArgumentMatchers.<List<Integer>>any(), ArgumentMatchers.<List<String>>any());

        PowerMockito.when(networkManager, "sendNetworkRequest",
                        anyString(), anyString(), any(RudderNetworkManager.RequestMethod.class)
                )
                .thenAnswer(new Answer<RudderNetworkManager.Result>() {
                    @Override
                    public RudderNetworkManager.Result answer(InvocationOnMock invocation) {
                        System.out.println(invocation.getArguments().length);
                        return mockResult;
                    }
                });
//        PowerMockito.doReturn(mockResult).when(networkManager).sendNetworkRequest(anyString(), anyString(), any(RudderNetworkManager.RequestMethod.class));
        /*PowerMockito.when(networkManager.sendNetworkRequest(anyString(), anyString(), ArgumentMatchers.any(RudderNetworkManager.RequestMethod.class)))
                .thenReturn(mockResult);
*/
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

        boolean result = FlushUtils.flush(30, "api.rudderstack.com/", dbPersistentManager, networkManager);

        //verify flushEventsToServer is called once with proper arguments
        //we use argument captor, cause we would need to remove spaces from argument
        PowerMockito.verifyPrivate(networkManager, times(1));

        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RudderNetworkManager.RequestMethod> arg3 = ArgumentCaptor.forClass(RudderNetworkManager.RequestMethod.class);

        networkManager.sendNetworkRequest(
                arg1.capture(),
                arg2.capture(),
                arg3.capture()
        );

        assertThat(result, Matchers.is(true));
        System.out.println(arg1.getValue());
        assertThat(arg1.getValue().replace(" ", ""),
                Matchers.is(expectedPayload.replace("\n", "").replace(" ", "")));
        System.out.println(arg2.getValue());
        assertThat(arg2.getValue().replace(" ", ""), Matchers.is("api.rudderstack.com/v1/batch"));
        System.out.println(arg3.getValue());
    }

    private int dbFetchCalled = 0;

    @Test
    public void testSynchronicity() throws Exception {
        final AtomicInteger threadsCalledDb = new AtomicInteger(0);
        //we add a sleep to db fetch to check for synchronicity
        // take a class level variable to check for thread access
        PowerMockito.doAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        List<Integer> msgIdParams = (List<Integer>) invocation.getArguments()[0];
                        List<String> msgParams = (List<String>) invocation.getArguments()[1];
                        ++dbFetchCalled;
                        System.out.println("fetchAllEvents called by: " + Thread.currentThread().getName());
                        //assert if called by multiple thread
                        assertThat(dbFetchCalled, Matchers.lessThan(2));
                        msgIdParams.addAll(messageIds);
                        msgParams.addAll(messages);
                        Thread.sleep(100);
                        --dbFetchCalled;
                        assertThat(dbFetchCalled, Matchers.lessThan(1));
                        System.out.println("return from fetchAllEvents by: " + Thread.currentThread().getName());
                        threadsCalledDb.incrementAndGet();
                        return null;
                    }
                }).when(dbPersistentManager).fetchAllCloudModeEventsFromDB(ArgumentMatchers.<List<Integer>>any(), ArgumentMatchers.<List<String>>any());
        PowerMockito.when(networkManager, "sendNetworkRequest",
                        anyString(), anyString(), any(RudderNetworkManager.RequestMethod.class)
                )
                .thenAnswer(new Answer<RudderNetworkManager.Result>() {
                    @Override
                    public RudderNetworkManager.Result answer(InvocationOnMock invocation) {
                        return new RudderNetworkManager.Result(RudderNetworkManager.NetworkResponses.SUCCESS, 200, anyString(), null);
                    }
                });

        //starting multiple threads to access the same.
        final int numberOfThreads = 8;
        for (int n = 0; n < numberOfThreads; n++) {
            final int finalN = n;
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Thread running " + (finalN));
                    FlushUtils.flush(30, "api.rudderstack.com/",
                            dbPersistentManager, networkManager);
                }
            }, "flush-thread-" + n) {
                @Override
                public synchronized void start() {
                    System.out.println("\nStarting thread: " + getName());
                    super.start();
                }
            };
            t.start();
        }
        //await until finished
        await().atMost(200, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return threadsCalledDb.get() == numberOfThreads;
            }
        });
    }
}