package com.rudderstack.android.sdk.core;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.Build;
import android.os.Message;

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

    @Before
    public void setUp() throws Exception {
        dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
        PowerMockito.when(dbPersistentManager, "saveEvent", anyString()).thenCallRealMethod();
        PowerMockito.when(dbPersistentManager, "startHandlerThread").thenCallRealMethod();
        Whitebox.setInternalState(dbPersistentManager, "queue", new LinkedList<Message>());
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
        PowerMockito.when(dbPersistentManager, "addMessageToHandlerThread", any())
                .thenAnswer(new Answer<Void>() {
                                @Override
                                public Void answer(InvocationOnMock invocation) throws Throwable {
                                    ++addMessageCalled;
                                    System.out.println("addMessageToQueue called by: " + Thread.currentThread().getName());
                                    //assert if called by multiple thread
                                    assertThat(addMessageCalled, Matchers.lessThan(2));
                                    Thread.sleep(500);
                                    --addMessageCalled;
                                    assertThat(addMessageCalled, Matchers.lessThan(1));
                                    System.out.println("return from addMessageToQueue by: " + Thread.currentThread().getName());
                                    messagesSaved.incrementAndGet();
                                    return null;
                                }
                            }
                );

        // Triggering the saveEvent method of DBPersistentManager from save-event-thread, as this method adds messages to the queue.
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < messages.size(); i++) {
                    dbPersistentManager.saveEvent(messages.get(i));
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
        await().atMost(10, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return messagesSaved.get() == messages.size();
            }
        });
    }
}