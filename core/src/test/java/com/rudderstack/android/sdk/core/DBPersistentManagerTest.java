package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.DBPersistentManager.UPDATED_COL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static java.lang.Thread.sleep;

import android.app.Application;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;

import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class DBPersistentManagerTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
            .create();
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

    @Test
    public void testMigration() {

        final AtomicBoolean isFinished = new AtomicBoolean(false);
        final DBPersistentManager finalDbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(),
                1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    finalDbPersistentManager.saveEventSync(MESSAGE_1);
                    finalDbPersistentManager.saveEventSync(MESSAGE_2);
                    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                    sleep(500);

                    Map<Integer, Integer> idStatusMap = new HashMap<>();
                    ArrayList<String> messages = new ArrayList<String>();
                    String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
                            DBPersistentManager.EVENTS_TABLE_NAME,
                            UPDATED_COL, 3);
                    finalDbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);
                    assertThat(idStatusMap, allOf(
                            hasKey(1),
                            hasKey(2)
                    ));

                    DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(), 2);
                    sleep(2000);
                    idStatusMap = new HashMap<>();
                    messages = new ArrayList<>();
                    dbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);
                    assertThat(idStatusMap.size(), CoreMatchers.is(2));
                    assertThat(idStatusMap, allOf(
                            hasEntry(1, 1),
                            hasEntry(2, 1)
                    ));

                    Map<String, Object> msg1FromDb = gson.fromJson(messages.get(0), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    assertThat((String) msg1FromDb.get("event"), CoreMatchers.is("mess-1"));

                    finalDbPersistentManager.saveEventSync(MESSAGE_3);
                    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
                    sleep(500);
                    idStatusMap = new HashMap<>();
                    messages = new ArrayList<>();
                    dbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);
                    assertThat(idStatusMap.size(), CoreMatchers.is(3));
                    assertThat(idStatusMap, allOf(
                            hasEntry(1, 1),
                            hasEntry(2, 1),
                            hasEntry(3, 0)
                    ));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    isFinished.set(true);
                    finalDbPersistentManager.close();
                }
            }


        }).start();
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isFinished);

    }

    @Test
    public void cloudModeEventsTest() {
        final AtomicBoolean isFinished = new AtomicBoolean(false);
        final DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext());
        dbPersistentManager.deleteAllEvents();

        //insert data into db
        dbPersistentManager.saveEventSync(MESSAGE_1);
        dbPersistentManager.saveEventSync(MESSAGE_2);
        dbPersistentManager.saveEventSync(MESSAGE_3);
        dbPersistentManager.saveEventSync(MESSAGE_4);


        ArrayList<Integer> messageIds = new ArrayList<>();
        ArrayList<String> messageJsons = new ArrayList<>();
        dbPersistentManager.fetchAllCloudModeEventsFromDB(messageIds, messageJsons);
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
        dbPersistentManager.markCloudModeDone(messageIds);

        //fetch again
        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchAllCloudModeEventsFromDB(messageIds, messageJsons);

        //list should be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(0));

        //device modes shouldn't be empty
        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchDeviceModeEventsFromDb(messageIds, messageJsons, 5);

        //list shouldn't be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(4));


        dbPersistentManager.deleteAllEvents();
        dbPersistentManager.close();

    }

    @Test
    public void deviceModeEventsTest() {
        final AtomicBoolean isFinished = new AtomicBoolean(false);
        final DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext());
//        dbPersistentManager.deleteAllEvents();

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

        //fetch again
        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchDeviceModeEventsFromDb(messageIds, messageJsons, 5);

        //list should be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(0));

        //test cloud
        messageIds.clear();
        messages.clear();
        dbPersistentManager.fetchAllCloudModeEventsFromDB(messageIds, messageJsons);

        //list shouldn't be empty
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(4));

        dbPersistentManager.deleteAllEvents();
        dbPersistentManager.close();
    }

    @Test
    public void doneEventsTest() {
        final DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext());

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


    @Test
    public void deleteFirstEventsTest() throws InterruptedException {
        final DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext());
        sleep(500);
        //insert data into db
        dbPersistentManager.saveEventSync(MESSAGE_1);
        dbPersistentManager.saveEventSync(MESSAGE_2);
        dbPersistentManager.saveEventSync(MESSAGE_3);
        dbPersistentManager.saveEventSync(MESSAGE_4);

        //delete
        dbPersistentManager.deleteFirstEvents(2);

        List<Integer> messageIds = new ArrayList<>();
        List<String> messageJsons = new ArrayList<>();
        dbPersistentManager.getEventsFromDB(messageIds, messageJsons, "SELECT * FROM " + DBPersistentManager.EVENTS_TABLE_NAME);

        //check
        assertThat(messageIds, Matchers.<Integer>iterableWithSize(2));

        dbPersistentManager.deleteAllEvents();
        dbPersistentManager.close();

    }

    private List<RudderMessage> parse(List<String> messageJsons) {
        List<RudderMessage> messages = new ArrayList<>();
        for (String mJson :
                messageJsons) {
            messages.add(gson.<RudderMessage>fromJson(mJson, RudderMessage.class));
        }
        return messages;
    }
}