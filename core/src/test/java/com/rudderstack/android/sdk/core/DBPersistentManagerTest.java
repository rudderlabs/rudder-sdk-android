//package com.rudderstack.android.sdk.core;
//
//import static com.rudderstack.android.sdk.core.DBPersistentManager.UPDATED_COL;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
//
//import static java.lang.Thread.sleep;
//
//import android.app.Application;
//import android.os.Build;
//
//import androidx.test.core.app.ApplicationProvider;
//
//import com.google.common.reflect.TypeToken;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
//import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
//
//import org.awaitility.Awaitility;
//import org.hamcrest.CoreMatchers;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricTestRunner;
//import org.robolectric.annotation.Config;
//import org.robolectric.shadows.ShadowLooper;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@RunWith(RobolectricTestRunner.class)
//@Config(sdk = Build.VERSION_CODES.P)
//public class DBPersistentManagerTest {
//
//    private final Gson gson = new GsonBuilder()
//            .registerTypeAdapter(RudderTraits.class, new RudderTraitsSerializer())
//            .registerTypeAdapter(RudderContext.class, new RudderContextSerializer())
//            .create();
//    private static final String MESSAGE_1 = "    {\n" +
//            "       \"event\": \"mess-1\",\n" +
//            "       \"messageId\": \"e-1\",\n" +
//            "      \"message\": \"m-1\",\n" +
//            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
//            "    }\n";
//    private static final String MESSAGE_2 = "    {\n" +
//            "       \"event\": \"mess-2\",\n" +
//            "       \"messageId\": \"e-2\",\n" +
//            "      \"message\": \"m-1\",\n" +
//            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
//            "    }\n";
//
//    @Before
//    public void start() {
//    }
//
//    @Test
//    public void testMigration() {
//
//        final AtomicBoolean isFinished = new AtomicBoolean(false);
//        final DBPersistentManager finalDbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(),
//                1);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    sleep(1000);
//                    finalDbPersistentManager.saveEventSync(MESSAGE_1);
//                    finalDbPersistentManager.saveEventSync(MESSAGE_2);
//                    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
//
//                    sleep(500);
//                    Map<Integer, Integer> idStatusMap = new HashMap<>();
//                    ArrayList<String> messages = new ArrayList<>();
//
//                    String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
//                            DBPersistentManager.EVENTS_TABLE_NAME,
//                            UPDATED_COL, 2);
//                    finalDbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);
//
//                    assertThat(idStatusMap, allOf(
//                            hasKey(1),
//                            hasKey(2)
//                    ));
//                    finalDbPersistentManager.close();
//                    DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(), 2);
//                    sleep(2000);
//                    idStatusMap = new HashMap<>();
//                    messages = new ArrayList<>();
//                    dbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);
//
//                    assertThat(idStatusMap.size(), CoreMatchers.is(2));
//                    assertThat(idStatusMap, allOf(
//                            hasEntry(1, 1),
//                            hasEntry(2, 1)
//                    ));
//                    Map<String, Object> msg1FromDb = gson.fromJson(messages.get(0), new TypeToken<Map<String, Object>>() {
//                    }.getType());
//                    assertThat((String) msg1FromDb.get("event"), CoreMatchers.is("mess-1"));
//
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                    isFinished.set(true);
//                }
//            }
//
//
//        }).start();
//        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isFinished);
//
//    }
//}