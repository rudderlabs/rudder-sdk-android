package com.rudderstack.android.sdk.core;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import android.os.Build;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import com.rudderstack.android.sdk.core.gson.RudderGson;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class DBPersistentManagerTest {

    DBPersistentManager dbPersistentManager;
    private final List<String> messages = new ArrayList<String>(ImmutableList.of("{\"message\":\"m-1\"}",
            "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}", "{\"message\":\"m-6\"}", "{\"message\":\"m-7\"}", "{\"message\":\"m-8\"}", "{\"message\":\"m-9\"}"));
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
        dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
        PowerMockito.when(dbPersistentManager, "saveEventSync", anyString()).thenCallRealMethod();
        PowerMockito.when(dbPersistentManager, "saveEvent", anyString(), any()).thenCallRealMethod();
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
