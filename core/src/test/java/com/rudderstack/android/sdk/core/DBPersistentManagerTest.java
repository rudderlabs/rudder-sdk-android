package com.rudderstack.android.sdk.core;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Build;
import android.os.Message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static java.lang.Thread.sleep;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.common.collect.ImmutableList;
import com.rudderstack.android.sdk.core.gson.RudderGson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class DBPersistentManagerTest {

    DBPersistentManager dbPersistentManager;
    private final List<RudderMessage> messages = new ArrayList<RudderMessage>();
    private static final String MESSAGE_1 = "    {\n" +
            "       \"event\": \"mess-1\",\n" +
            "       \"messageId\": \"e-1\",\n" +
            "      \"message\": \"m-1\",\n" +
            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
            "    }\n";
    private static final String MESSAGE_2 = "    {\n" +
            "       \"event\": \"mess-2\",\n" +
            "       \"messageId\": \"e-2\",\n" +
            "      \"message\": \"m-1\",\n" +
            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
            "    }\n";
    private static final String MESSAGE_3 = "    {\n" +
            "       \"event\": \"mess-3\",\n" +
            "       \"messageId\": \"e-3\",\n" +
            "      \"message\": \"m-3\",\n" +
            "      \"sentAt\": \"2022-07-14T06:46:41.365Z\"\n" +
            "    }\n";
    private static final String MESSAGE_4 = "    {\n" +
            "       \"event\": \"mess-4\",\n" +
            "       \"messageId\": \"e-4\",\n" +
            "      \"message\": \"m-4\",\n" +
            "      \"sentAt\": \"2022-07-14T06:46:42.365Z\"\n" +
            "    }\n";
    private RudderDeviceModeManager deviceModeManager;

    @Before
    public void setUp() throws Exception {
        dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
        PowerMockito.when(dbPersistentManager, "saveEventSync", anyString()).thenCallRealMethod();
        PowerMockito.when(dbPersistentManager, "saveEvent", any(RudderMessage.class), any()).thenCallRealMethod();
        PowerMockito.when(dbPersistentManager, "startHandlerThread").thenCallRealMethod();
        Whitebox.setInternalState(dbPersistentManager, "queue", new LinkedList<Message>());
        deviceModeManager = Mockito.mock(RudderDeviceModeManager.class);
    }

    @After
    public void tearDown() {
        dbPersistentManager.deleteAllEvents();
        dbPersistentManager.close();
        dbPersistentManager = null;
    }

    private int addMessageCalled = 0;

    @Test
    public void testGetEventJson() throws JSONException {
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
        String outputJsonString = dbPersistentManager.getEventJsonString(message);
        assertThat("JSONObjects and JSONArray are serialized perfectly", outputJsonString, is(expectedJsonString.replace("\n", "").replace(" ", "")));
    }

    @Test
    public void testSynchronicity() throws Exception {
        final AtomicInteger messagesSaved = new AtomicInteger(0);
        // Mocking the addMessageToQueue, which is used by both the save-event-thread and Handler thread, to verify synchronization
        PowerMockito.when(dbPersistentManager, "addMessageToHandlerThread", any(Message.class))
                .thenAnswer((Answer<Void>) invocation -> {
                            ++addMessageCalled;
                            System.out.println("addMessageToQueue called by: " + Thread.currentThread().getName());
                            //assert if called by multiple thread
                            assertThat(addMessageCalled, Matchers.lessThan(2));
                            sleep(500);
                            --addMessageCalled;
                            assertThat(addMessageCalled, Matchers.lessThan(1));
                            System.out.println("return from addMessageToQueue by: " + Thread.currentThread().getName());
                            messagesSaved.incrementAndGet();
                            return null;
                        }
                );

        // Triggering the saveEvent method of DBPersistentManager from save-event-thread, as this method adds messages to the queue.
        new Thread(new Runnable() {
            @Override
            public void run() {
                messages.add(new RudderMessageBuilder()
                        .setEventName("e-1")
                        .setGroupId("g-id")
                        .setPreviousId("p-id")
                        .setProperty(Collections.singletonMap("s-id", "some-prop"))
                        .build());
                messages.add(new RudderMessageBuilder()
                        .setEventName("e-2")
                        .setGroupId("g-id")
                        .setPreviousId("p-id")
                        .setProperty(Collections.singletonMap("s-id", "some-prop"))
                        .build());
                messages.add(new RudderMessageBuilder()
                        .setEventName("e-3")
                        .setGroupId("g-id")
                        .setPreviousId("p-id")
                        .setProperty(Collections.singletonMap("s-id", "some-prop"))
                        .build());
                for (int i = 0; i < messages.size(); i++) {
                    dbPersistentManager.saveEvent(messages.get(i), new EventInsertionCallback(new RudderMessageBuilder().build(), deviceModeManager));
                    // Starting the Handler thread, only when some events are added to the queue, so that the replay happens, and handler
                    // thread starts reading from the queue.
                    if (i == messages.size() / 2) {
                        dbPersistentManager.startHandlerThread();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "save-event-thread") {
        }.start();


        //await until finished
        await().atMost(15, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return messagesSaved.get() == messages.size();
            }
        });
    }

    @Test
    public void doneEventsTest() {
        final DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider
                .<Application>getApplicationContext(), new DBPersistentManager.DbManagerParams(false, null, null));

        //insert data into db
        dbPersistentManager.saveEventSync(MESSAGE_1);
        dbPersistentManager.saveEventSync(MESSAGE_2);
        dbPersistentManager.saveEventSync(MESSAGE_3);
        dbPersistentManager.saveEventSync(MESSAGE_4);


        ArrayList<Integer> messageIds = new ArrayList<>();
        ArrayList<String> messageJsons = new ArrayList<>();
        dbPersistentManager.fetchDeviceModeEventsFromDb(messageIds, messageJsons, 5);
        List<RudderMessage> messages = parse(messageJsons);

        //test if events are available
        assertThat(messages, allOf(
                Matchers.<RudderMessage>iterableWithSize(greaterThanOrEqualTo(4)),
                Matchers.<RudderMessage>hasItems(hasProperty("eventName", equalTo("mess-1")),
                        hasProperty("eventName", equalTo("mess-2")),
                        hasProperty("eventName", equalTo("mess-3")),
                        hasProperty("eventName", equalTo("mess-4"))
                )
        ));

        //mark events as cloud done
        dbPersistentManager.markDeviceModeDone(messageIds);
        dbPersistentManager.markCloudModeDone(messageIds);

        //fetch again
        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchDeviceModeEventsFromDb(messageIds, messageJsons, 5);

        //list should be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(0));

        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchAllCloudModeEventsFromDB(messageIds, messageJsons);

        //list should be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(0));

        //delete done events
        dbPersistentManager.runGcForEvents();
        messageIds.clear();
        messages.clear();
        dbPersistentManager.getEventsFromDB(messageIds, messageJsons, "SELECT * FROM " + DBPersistentManager.EVENTS_TABLE_NAME);

        //should be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(0));

        dbPersistentManager.deleteAllEvents();
        dbPersistentManager.close();

    }

    private List<RudderMessage> parse(List<String> messageJsons) {
        List<RudderMessage> messages = new ArrayList<>();
        for (String mJson :
                messageJsons) {
            messages.add(RudderGson.deserialize(mJson, RudderMessage.class));
        }
        return messages;
    }
}