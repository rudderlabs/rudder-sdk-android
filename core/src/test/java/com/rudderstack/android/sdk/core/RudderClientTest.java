package com.rudderstack.android.sdk.core;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 16, maxSdk = 28)
public class RudderClientTest {

    private RudderClient client;
    private String writeKey = "1234";
    private String dataplaneUrl = "https//hosted.rudderlabs.com";
    private final ArrayList<RudderMessage> messageStore = new ArrayList<>();

    @Before
    public void setUp() {
        this.client = RudderClient.getInstance(
                ApplicationProvider.getApplicationContext(),
                writeKey,
                new RudderConfig.Builder()
                        .withDataPlaneUrl(dataplaneUrl)
                        .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                        .build()
        );
        try {
            // get the repository variable from client
            Field repository = client.getClass().getDeclaredField("repository");
            // make private variable repository accessible
            repository.setAccessible(true);
            // create EventRepository mock
            EventRepository repositoryMock = Mockito.mock(EventRepository.class);
            // mock EventRepository.dump to store dumped message to messageStore
            Mockito.doAnswer(i -> {
                System.out.println("Dumped :"+i.getArguments());
                messageStore.add(0, (RudderMessage) i.getArguments()[0]);
                return null;
            }).when(repositoryMock).dump(Mockito.any(RudderMessage.class));
            // replace the original repository with the mocked one
            repository.set(null, repositoryMock);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    RudderMessage getLatestDump() {
        RudderMessage message = null;
        if (messageStore.size() > 0) {
            message = messageStore.get(0);
            // remove the message to keep only one message in the store at a time
            messageStore.remove(0);
        }
        return message;
    }

    @Test
    public void getInstance_NoRudderConfig() {
        System.out.println(String.format("Calling getInstance with config - writeKey %s", 1234));
        RudderClient testClient = RudderClient.getInstance(
                ApplicationProvider.getApplicationContext(),
                writeKey
        );
        assertNotNull(testClient);
        System.out.println(testClient);
        System.out.println("Rudder Client successfully initialized");
    }

    @Test
    public void getInstance_WithRudderConfigNoFactories() {
        System.out.println(String.format("Calling getInstance with config - writeKey %s", 1234));
        RudderClient testClient = RudderClient.getInstance(
                ApplicationProvider.getApplicationContext(),
                writeKey,
                new RudderConfig.Builder()
                .withDataPlaneUrl(dataplaneUrl)
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                .build()
        );
        assertNotNull(testClient);
        System.out.println(testClient);
        System.out.println("Rudder Client successfully initialized");
    }

    @Test
    public void getInstance2() {
    }

    @Test
    public void getInstance3() {
    }

    @Test
    public void with() {
    }

    @Test
    public void getApplication() {
    }

    @Test
    public void track_OnlyEventName() {
        client.track("testEvent");
        RudderMessage message = getLatestDump();
        assert(message.getEventName() == "testEvent");
        System.out.println("Test track_OnlyEventName passed");
    }

    @Test
    public void track_EventNameAndProperties() {
        RudderProperty properties = new RudderProperty().putValue("key", "value");
        client.track("testEvent", properties);
        RudderMessage message = getLatestDump();
        assert(message.getEventName() == "testEvent");
        assert(message.getProperties().equals(properties.getMap()));
        System.out.println("Test track_EventNameAndProperties passed");
    }

    @Test
    public void track_EventNamePropertiesAndOptions() {
        RudderProperty properties = new RudderProperty().putValue("key", "value");
        RudderOption options = new RudderOption().putExternalId("braze_externalId", "1234");
        client.track("testEvent", properties, options);
        RudderMessage message = getLatestDump();
        assert(message.getEventName() == "testEvent");
        assert(message.getProperties().equals(properties.getMap()));
        assert(message.getRudderOption().getExternalIds() == options.getExternalIds());
        System.out.println("Test track_EventNamePropertiesAndOptions passed");
    }

    @Test
    public void track_RudderMessage() {
        RudderProperty properties = new RudderProperty().putValue("key", "value");
        RudderOption options = new RudderOption().putExternalId("braze_externalId", "1234");
        RudderMessage inputMessage = new RudderMessageBuilder()
                .setEventName("testEvent")
                .setProperty(properties)
                .setRudderOption(options)
                .build();
        client.track(inputMessage);
        RudderMessage message = getLatestDump();
        assert(message.getEventName() == "testEvent");
        assert(message.getProperties().equals(properties.getMap()));
        assert(message.getRudderOption().getExternalIds() == options.getExternalIds());
        System.out.println("Test track_RudderMessage passed");
    }

    @Test
    public void track_RudderMessageBuilder() {
        RudderProperty properties = new RudderProperty().putValue("key", "value");
        RudderOption options = new RudderOption().putExternalId("braze_externalId", "1234");
        RudderMessageBuilder builder = new RudderMessageBuilder()
                .setEventName("testEvent")
                .setProperty(properties)
                .setRudderOption(options);
        client.track(builder);
        RudderMessage message = getLatestDump();
        assert(message.getEventName() == "testEvent");
        assert(message.getProperties().equals(properties.getMap()));
        assert(message.getRudderOption().getExternalIds() == options.getExternalIds());
        System.out.println("Test track_RudderMessageBuilder passed");
    }

    @Test
    public void screen() {
    }

    @Test
    public void screen1() {
    }

    @Test
    public void screen2() {
    }

    @Test
    public void screen3() {
    }

    @Test
    public void screen4() {
    }

    @Test
    public void screen5() {
    }

    @Test
    public void page() {
    }

    @Test
    public void page1() {
    }

    @Test
    public void identify() {
    }

    @Test
    public void identify1() {
    }

    @Test
    public void identify2() {
    }

    @Test
    public void identify3() {
    }

    @Test
    public void identify4() {
    }

    @Test
    public void identify5() {
    }

    @Test
    public void alias() {
    }

    @Test
    public void alias1() {
    }

    @Test
    public void group() {
    }

    @Test
    public void group1() {
    }

    @Test
    public void group2() {
    }

    @Test
    public void setSingletonInstance() {
    }

    @Test
    public void getRudderContext() {
    }

    @Test
    public void getSnapShot() {
    }

    @Test
    public void reset() {
    }

    @Test
    public void optOut() {
    }

    @Test
    public void onIntegrationReady() {
    }

    @Test
    public void shutdown() {
    }
}
