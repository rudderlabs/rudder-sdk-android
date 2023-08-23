package com.rudderstack.android.sdk.core;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Build;
import android.os.Message;
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
import androidx.test.runner.AndroidJUnit4;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class DBPersistentManagerTest {

    DBPersistentManager dbPersistentManager;
    private final List<String> messages = new ArrayList<String>(ImmutableList.of("{\"message\":\"m-1\"}",
            "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}", "{\"message\":\"m-6\"}", "{\"message\":\"m-7\"}", "{\"message\":\"m-8\"}", "{\"message\":\"m-9\"}"));
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
    private RudderDeviceModeManager deviceModeManager ;
    @Before
    public void setUp() throws Exception {
//        final DBPersistentManager finalDbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider
//                .<Application>getApplicationContext());
//        dbPersistentManager = PowerMockito.spy(finalDbPersistentManager);
        dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
        PowerMockito.when(dbPersistentManager, "saveEventSync", anyString()).thenCallRealMethod();
        PowerMockito.when(dbPersistentManager, "saveEvent", anyString(), any()).thenCallRealMethod();
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
                for (int i = 0; i < messages.size(); i++) {
                    dbPersistentManager.saveEvent(messages.get(i),
                            new EventInsertionCallback(new RudderMessageBuilder().build(),
                                    deviceModeManager));
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
            messages.add(gson.<RudderMessage>fromJson(mJson, RudderMessage.class));
        }
        return messages;
    }
}