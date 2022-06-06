package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.DBPersistentManager.UPDATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.iterableWithSize;

import static java.lang.Thread.sleep;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rudderstack.android.sdk.core.util.RudderContextSerializer;
import com.rudderstack.android.sdk.core.util.RudderTraitsSerializer;
import com.rudderstack.android.sdk.core.util.Utils;

import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
            "       \"event\": \"mess-1\",\n"+
            "       \"messageId\": \"e-1\",\n"+
            "      \"message\": \"m-1\",\n" +
            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
            "    }\n";
    private static final String MESSAGE_2 = "    {\n" +
            "       \"event\": \"mess-2\",\n"+
            "       \"messageId\": \"e-2\",\n"+
            "      \"message\": \"m-1\",\n" +
            "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
            "    }\n";
    @Before
    public void start(){
        /*RudderElementCache.cachedContext = new RudderContext(ApplicationProvider.<Application>getApplicationContext(),
                "anon-id", "ad-id", "dev_token");
        Mockito.mock(Utils.class);
        Mockito.*/
    }

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
                    Map<Integer,Integer> idStatusMap = new HashMap<>();
                    ArrayList<String> messages = new ArrayList<String>();

                    String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d",
                            DBPersistentManager.EVENTS_TABLE_NAME,
                            UPDATED, 2);
                    finalDbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);

                    assertThat(idStatusMap, allOf(
                            hasKey(1),
                            hasKey(2)
                    ));
                    finalDbPersistentManager.close();
                    DBPersistentManager dbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(), 2);
//                    finalDbPersistentManager.onUpgrade(finalDbPersistentManager.getWritableDatabase(), 1, 2);
                    sleep(2000);
                    idStatusMap = new HashMap<>();
                    messages = new ArrayList<String>();
                    dbPersistentManager.getEventsFromDB(idStatusMap, messages, selectSQL);

                    assertThat(idStatusMap.size(), CoreMatchers.is(2));
                    assertThat(idStatusMap, allOf(
                            hasEntry(1, 1),
                            hasEntry(2, 1)
                    ));
                    Map<String, Object> msg1FromDb = gson.fromJson(messages.get(0), new TypeToken<Map<String, Object>>() {}.getType());
//                    RudderMessage msg2FromDb = gson.fromJson(messages.get(1), RudderMessage.class);

                    assertThat((String) msg1FromDb.get("event"), CoreMatchers.is("mess-1"));


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    isFinished.set(true);
                }
            }


        }).start();
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isFinished);

    }

    @Test
    public void testEventTransformationGroupBy(){

        final AtomicBoolean isFinished = new AtomicBoolean(false);
        final DBPersistentManager finalDbPersistentManager = DBPersistentManager.getInstance(ApplicationProvider.<Application>getApplicationContext(),
                1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    SQLiteDatabase database = finalDbPersistentManager.getWritableDatabase();
                    for (int i = 1; i <= 20; i++) {
                        ContentValues eventTransformationValues = new ContentValues();
                        eventTransformationValues.put(DBPersistentManager.EVENTS_TRANSFORMER_ROW_ID_COL_NAME,
                                i % 5 + 1);
                        eventTransformationValues.put(DBPersistentManager.EVENTS_TRANSFORMER_TRANSFORMATION_ID_COL_NAME,
                                i);
                        database.insert(DBPersistentManager.EVENTS_ROW_ID_TRANSFORMATION_ID_TABLE_NAME,null, eventTransformationValues);

                    }
                    finalDbPersistentManager.fetchTransformationIdsGroupByEventRowId(Arrays.asList(1,2,3,4,5));
                    isFinished.set(true);
                }catch (Exception e){

                }
            }
        }).start();
        Awaitility.await().atMost(1, TimeUnit.MINUTES).untilTrue(isFinished);

    }
}