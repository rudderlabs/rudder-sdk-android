package com.rudderstack.android.sdk.core;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({FlushUtils.class,
        RudderLogger.class,
        TextUtils.class, Utils.class,
        RudderNetworkManager.class})
public class EventRepositoryTest {

    // data to be used
    private final List<Integer> messageIds = new ArrayList<Integer>(ImmutableList.of(1, 2, 3, 4, 5));
    private final List<String> messages = new ArrayList<String>(ImmutableList.of("{\"message\":\"m-1\"}",
            "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}"));

    //database manager mock
    DBPersistentManager dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
    RudderNetworkManager networkManager = PowerMockito.mock(RudderNetworkManager.class);

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

        final RudderNetworkManager.Result mockResult = new RudderNetworkManager.Result(RudderNetworkManager.NetworkResponses.SUCCESS,
                200, "", null);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            ((ArrayList) invocation.getArgument(0)).addAll(messageIds);
            ((ArrayList) invocation.getArgument(1)).addAll(messages);
            return null;
        }).when(dbPersistentManager).fetchAllEventsFromDB(anyList(), anyList());
        Mockito.doAnswer((Answer<Void>) invocation -> {
            List<Integer> msgIdParams = (List<Integer>) invocation.getArguments()[0];
            List<String> msgParams = (List<String>) invocation.getArguments()[1];
            msgIdParams.addAll(messageIds);
            msgParams.addAll(messages);
            return null;
        }).when(dbPersistentManager).fetchAllCloudModeEventsFromDB(ArgumentMatchers.<List<Integer>>any(), ArgumentMatchers.<List<String>>any());
        Mockito.when(networkManager.sendNetworkRequest(anyString(), anyString(),
                        ArgumentMatchers.any(RudderNetworkManager.RequestMethod.class), anyBoolean()))
                .thenAnswer((Answer<RudderNetworkManager.Result>) invocation -> {
                    System.out.println(invocation.getArguments().length);
                    return mockResult;
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
        boolean result = FlushUtils.flushToServer(
                30, "api.rudderstack.com/", dbPersistentManager
                , networkManager);


        //verify flushEventsToServer is called once with proper arguments
        //we use argument captor, cause we would need to remove spaces from argument
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RudderNetworkManager.RequestMethod> arg3 = ArgumentCaptor.forClass(RudderNetworkManager.RequestMethod.class);

        Mockito.verify(networkManager, Mockito.times(1)).sendNetworkRequest(
                arg1.capture(),
                arg2.capture(),
                arg3.capture(),
                anyBoolean()
        );

        assertThat(result, is(true));
        assertThat(arg1.getValue().replace(" ", ""),
                is(expectedPayload.replace("\n", "").replace(" ", "")));
        assertThat(arg2.getValue().replace(" ", ""), is("api.rudderstack.com/v1/batch"));
        assertThat(arg3.getValue(), is(RudderNetworkManager.RequestMethod.POST));
    }

    private int dbFetchCalled = 0;

    @Test
    public void testSynchronicity() throws Exception {
        final AtomicInteger threadsCalledDb = new AtomicInteger(0);
        final RudderNetworkManager.Result mockResult = new RudderNetworkManager.Result(RudderNetworkManager.NetworkResponses.SUCCESS,
                200, "", null);
        //we add a sleep to db fetch to check for synchronicity
        // take a class level variable to check for thread access
        Mockito.doAnswer(new Answer<Void>() {
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
        }).when(dbPersistentManager).fetchAllCloudModeEventsFromDB(anyList(), anyList());

        PowerMockito.when(networkManager, "sendNetworkRequest",
                        anyString(), anyString(), ArgumentMatchers.any(), anyBoolean()
                )
                .thenAnswer(new Answer<RudderNetworkManager.Result>() {
                    @Override
                    public RudderNetworkManager.Result answer(InvocationOnMock invocation) {
                        System.out.println(invocation.getArguments().length);
                        return mockResult;
                    }
                });
        PowerMockito.when(networkManager.sendNetworkRequest(anyString(), anyString(), ArgumentMatchers.any(RudderNetworkManager.RequestMethod.class), anyBoolean(), anyBoolean()))
                .thenReturn(mockResult);
        //starting multiple threads to access the same.
        final int numberOfThreads = 8;
        for (int n = 0; n < numberOfThreads; n++) {
            Thread t = new Thread(() ->
                    FlushUtils.flushToServer(
                            30, "api.rudderstack.com/", dbPersistentManager
                            , networkManager), "flush-thread-" + n) {
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
    public void testGetEventJson() throws JSONException {
        EventRepository repo = new EventRepository();
        HashMap jsonData = new HashMap<String, Object>();
        jsonData.put("pinId", 2351636);
        jsonData.put("userLocationLongitude", 12.516869940636775);
        jsonData.put("userLocationLatitude", 55.661663449562205);
        jsonData.put("userDirection", 12.311176);
        jsonData.put("userSpeed", 13.888889);
        jsonData.put("locationHorizontalAccuracy", 3.813398);
        jsonData.put("locationVerticalAccuracy", 0.0);
        jsonData.put("speedAccuracy", 0.0);
        jsonData.put("directionAccuracy", 0.0);
        JSONArray locationsBefore = new JSONArray();
        locationsBefore.put(new JSONObject().put("latitude", 55.66132122924984).put("longitude", 12.51671169784383));
        locationsBefore.put(new JSONObject().put("latitude", 55.661428115890374).put("longitude", 12.51677390468785));
        locationsBefore.put(new JSONObject().put("latitude", 55.661663449562205).put("longitude", 12.516869940636775));
        jsonData.put("locationsBefore", locationsBefore);
        JSONArray locationsAfter = new JSONArray();
        locationsAfter.put(new JSONObject().put("latitude", 55.66190443423447).put("longitude", 12.516850445696493));
        locationsAfter.put(new JSONObject().put("latitude", 55.66214021085126).put("longitude", 12.516731010346978));
        locationsAfter.put(new JSONObject().put("latitude", 55.66237844832189).put("longitude", 12.516618424859859));
        jsonData.put("locationsAfter", locationsAfter);
        JSONArray speedsBefore = new JSONArray();
        speedsBefore.put(new JSONObject().put("speed", 0));
        speedsBefore.put(new JSONObject().put("speed", 13.888889));
        speedsBefore.put(new JSONObject().put("speed", 13.888889));
        jsonData.put("speedsBefore", speedsBefore);
        JSONArray speedsAfter = new JSONArray();
        speedsAfter.put(new JSONObject().put("speed", 13.888889));
        speedsAfter.put(new JSONObject().put("speed", 13.888889));
        jsonData.put("speedsAfter", speedsAfter);
        jsonData.put("pinType", 1);
        jsonData.put("pinSubtype", 11);
        jsonData.put("pinDirection", 0.0);
        jsonData.put("pinLocationLongitude", 12.507275);
        jsonData.put("pinLocationLatitude", 55.672436);
        jsonData.put("coDriverVersion", -1.0);
        RudderMessage message = new RudderMessageBuilder().setEventName("TestEvent").setProperty(jsonData).build();
        String expectedJsonString = "{\n" +
                "  \"messageId\": \"" + message.getMessageId() + "\",\n" +
                "  \"channel\": \"mobile\",\n" +
                "  \"context\": {},\n" +
                "  \"originalTimestamp\": \"2022-03-14T06:46:41.365Z\",\n" +
                "  \"event\": \"TestEvent\",\n" +
                "  \"properties\": {\n" +
                "    \"pinLocationLongitude\": 12.507275,\n" +
                "    \"userDirection\": 12.311176,\n" +
                "    \"locationHorizontalAccuracy\": 3.813398,\n" +
                "    \"pinId\": 2351636,\n" +
                "    \"pinDirection\": 0.0,\n" +
                "    \"coDriverVersion\": -1.0,\n" +
                "    \"userLocationLatitude\": 55.661663449562205,\n" +
                "    \"userSpeed\": 13.888889,\n" +
                "    \"locationsBefore\": [\n" +
                "      {\n" +
                "        \"latitude\": 55.66132122924984,\n" +
                "        \"longitude\": 12.51671169784383\n" +
                "      },\n" +
                "      {\n" +
                "        \"latitude\": 55.661428115890374,\n" +
                "        \"longitude\": 12.51677390468785\n" +
                "      },\n" +
                "      {\n" +
                "        \"latitude\": 55.661663449562205,\n" +
                "        \"longitude\": 12.516869940636775\n" +
                "      }\n" +
                "    ],\n" +
                "    \"speedAccuracy\": 0.0,\n" +
                "    \"locationVerticalAccuracy\": 0.0,\n" +
                "    \"pinType\": 1,\n" +
                "    \"speedsAfter\": [\n" +
                "      {\n" +
                "        \"speed\": 13.888889\n" +
                "      },\n" +
                "      {\n" +
                "        \"speed\": 13.888889\n" +
                "      }\n" +
                "    ],\n" +
                "    \"pinSubtype\": 11,\n" +
                "    \"locationsAfter\": [\n" +
                "      {\n" +
                "        \"latitude\": 55.66190443423447,\n" +
                "        \"longitude\": 12.516850445696493\n" +
                "      },\n" +
                "      {\n" +
                "        \"latitude\": 55.66214021085126,\n" +
                "        \"longitude\": 12.516731010346978\n" +
                "      },\n" +
                "      {\n" +
                "        \"latitude\": 55.66237844832189,\n" +
                "        \"longitude\": 12.516618424859859\n" +
                "      }\n" +
                "    ],\n" +
                "    \"speedsBefore\": [\n" +
                "      {\n" +
                "        \"speed\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"speed\": 13.888889\n" +
                "      },\n" +
                "      {\n" +
                "        \"speed\": 13.888889\n" +
                "      }\n" +
                "    ],\n" +
                "    \"pinLocationLatitude\": 55.672436,\n" +
                "    \"userLocationLongitude\": 12.516869940636775,\n" +
                "    \"directionAccuracy\": 0.0\n" +
                "  },\n" +
                "  \"integrations\": {}\n" +
                "}";
        String outputJsonString = repo.getEventJsonString(message);
        assertThat("JSONObjects and JSONArray are serialized perfectly", outputJsonString, is(expectedJsonString.replace("\n", "").replace(" ", "")));
    }
}

