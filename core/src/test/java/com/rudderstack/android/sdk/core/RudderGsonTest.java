package com.rudderstack.android.sdk.core;


import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static java.util.concurrent.TimeUnit.SECONDS;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({Utils.class})
public class RudderGsonTest {

    @Before
    public void setUp() throws Exception {
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp")
                .thenAnswer((Answer<String>) invocation -> "2022-03-14T06:46:41.365Z");
    }

    @Test
    public void testRudderContextSerializationSynchronicity() {
        AtomicInteger contextsSerialized = new AtomicInteger(0);
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 1; i <= 1000; i++) {
                    RudderContext context = getRudderContext(i);
                    assertThat("contexts got serialized perfectly " + i, areJsonStringsEqual(RudderGson.getInstance().toJson(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 1001; i <= 2000; i++) {
                    RudderContext context = getRudderContext(i);
                    assertThat("contexts got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 2001; i <= 3000; i++) {
                    RudderContext context = getRudderContext(i);
                    assertThat("contexts got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        await().atMost(60, SECONDS).until(() -> contextsSerialized.get() == 3000);
    }

    private RudderContext getRudderContext(int i) {
        RudderContext context = getDefaultRudderContext();
        Map<String, Object> customContextMap = new HashMap<>();
        Map<String, String> companyContext = new HashMap<>();
        companyContext.put("name", "company" + i);
        companyContext.put("city", "city" + i);
        customContextMap.put("company", companyContext);
        Map<String, String> personContext = new HashMap<>();
        personContext.put("tier", "enterprise" + i);
        customContextMap.put("person", personContext);
        Whitebox.setInternalState(context, "customContextMap", customContextMap);
        return context;
    }

    private RudderContext getDefaultRudderContext() {
        RudderContext context = PowerMockito.mock(RudderContext.class);
        RudderApp app = PowerMockito.mock(RudderApp.class);
        Whitebox.setInternalState(app, "build", "2");
        Whitebox.setInternalState(app, "name", "rudderstack-android-sdk-desu");
        Whitebox.setInternalState(app, "nameSpace", "com.rudderstack.android.sdk.core");
        Whitebox.setInternalState(app, "version", "1.1");
        Whitebox.setInternalState(context, "app", app);
        RudderLibraryInfo libraryInfo = PowerMockito.mock(RudderLibraryInfo.class);
        Whitebox.setInternalState(libraryInfo, "name", "com.rudderstack.android.sdk.core");
        Whitebox.setInternalState(libraryInfo, "version", "1.20.1");
        Whitebox.setInternalState(context, "libraryInfo", libraryInfo);
        RudderOSInfo osInfo = PowerMockito.mock(RudderOSInfo.class);
        Whitebox.setInternalState(osInfo, "name", "Android");
        Whitebox.setInternalState(osInfo, "version", "14");
        Whitebox.setInternalState(context, "osInfo", osInfo);
        RudderScreenInfo screenInfo = Mockito.mock(RudderScreenInfo.class);
        Whitebox.setInternalState(screenInfo, "density", 3);
        Whitebox.setInternalState(screenInfo, "height", 736);
        Whitebox.setInternalState(screenInfo, "width", 414);
        Whitebox.setInternalState(context, "screenInfo", screenInfo);
        Whitebox.setInternalState(context, "userAgent", "Dalvik/2.1.0 (Linux; U; Android 14; sdk_gphone64_arm64 Build/UPB4.230623.005)");
        Whitebox.setInternalState(context, "locale", "en-US");
        RudderNetwork networkInfo = Mockito.mock(RudderNetwork.class);
        Whitebox.setInternalState(networkInfo, "isCellularEnabled", true);
        Whitebox.setInternalState(networkInfo, "isWifiEnabled", true);
        Whitebox.setInternalState(networkInfo, "isBluetoothEnabled", false);
        Whitebox.setInternalState(networkInfo, "carrier", "T-Mobile");
        Whitebox.setInternalState(context, "networkInfo", networkInfo);
        RudderDeviceInfo deviceInfo = Mockito.mock(RudderDeviceInfo.class);
        Whitebox.setInternalState(deviceInfo, "type", "Android");
        Whitebox.setInternalState(deviceInfo, "manufacturer", "Google");
        Whitebox.setInternalState(deviceInfo, "model", "sdk_gphone64_arm64");
        Whitebox.setInternalState(deviceInfo, "name", "emu64a");
        Whitebox.setInternalState(deviceInfo, "deviceId", "10f9e80f-5342-48e0-8908-1b221179895b");
        Whitebox.setInternalState(context, "deviceInfo", deviceInfo);
        Whitebox.setInternalState(context, "timezone", "Asia/Kolkata");
        Whitebox.setInternalState(context, "sessionId", 1699538605l);
        return context;
    }

    private String getRudderContextJsonString(int i) {
        return "{\"app\":{\"build\":\"2\",\"name\":\"rudderstack-android-sdk-desu\",\"namespace\":\"com.rudderstack.android.sdk.core\",\"version\":\"1.1\"},\"library\":{\"name\":\"com.rudderstack.android.sdk.core\",\"version\":\"1.20.1\"},\"os\":{\"name\":\"Android\",\"version\":\"14\"},\"screen\":{\"density\":3,\"width\":414,\"height\":736},\"userAgent\":\"Dalvik/2.1.0 (Linux; U; Android 14; sdk_gphone64_arm64 Build/UPB4.230623.005)\",\"locale\":\"en-US\",\"device\":{\"id\":\"10f9e80f-5342-48e0-8908-1b221179895b\",\"manufacturer\":\"Google\",\"model\":\"sdk_gphone64_arm64\",\"name\":\"emu64a\",\"type\":\"Android\"},\"network\":{\"carrier\":\"T-Mobile\",\"wifi\":true,\"bluetooth\":false,\"cellular\":true},\"timezone\":\"Asia/Kolkata\",\"sessionId\":1699538605,\"person\":{\"tier\":\"enterprise" + i + "\"},\"company\":{\"city\":\"city" + i + "\",\"name\":\"company" + i + "\"}}";
    }

    @Test
    public void testRudderTraitsSerializationSynchronicity() {
        AtomicInteger traitsSerialized = new AtomicInteger(0);
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 1; i <= 3000; i++) {
                    RudderTraits traits = getTraits(i);
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(traits), getTraitsJsonString(i)), is(true));
                    traitsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 3001; i <= 6000; i++) {
                    RudderTraits traits = getTraits(i);
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(traits), getTraitsJsonString(i)), is(true));
                    traitsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 6001; i <= 9000; i++) {
                    RudderTraits traits = getTraits(i);
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(traits), getTraitsJsonString(i)), is(true));
                    traitsSerialized.addAndGet(1);
                }
            }
        }.start();
        await().atMost(60, SECONDS).until(() -> traitsSerialized.get() == 9000);
    }

    private RudderTraits getTraits(int i) {
        RudderTraits traits = new RudderTraits();
        traits.putName("User Name " + i);
        traits.putEmail("user" + i + "@gmail.com");
        traits.putFirstName("User " + i);
        traits.putLastName("Name " + i);
        traits.put("key" + i, "value" + i);
        traits.put("custom trait key" + i, "custom trait value" + i);
        return traits;
    }

    private String getTraitsJsonString(int i) {
        return "{\"email\":\"user" + i + "@gmail.com\",\"firstname\":\"User " + i + "\",\"lastname\":\"Name " + i + "\",\"name\":\"User Name " + i + "\",\"key" + i + "\":\"value" + i + "\",\"custom trait key" + i + "\":\"custom trait value" + i + "\"}";
    }

    @Test
    public void testJSONArrayJSONObjectSerializationSynchronicity() {
        AtomicInteger messagesSerialized = new AtomicInteger(0);
        new Thread(() -> {
            for (int i = 1; i <= 1000; i++) {
                RudderMessage trackMessage = getTrackMessage(i);
                assertThat("track message got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(trackMessage), getTrackJsonString(i, trackMessage)), is(true));
                RudderMessage identifyMessage = getIdentifyMessage(i);
                assertThat("identify message got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(identifyMessage), getIdentifyJsonString(i, identifyMessage)), is(true));
                messagesSerialized.addAndGet(2);
            }
        }, "serialize-rudder-message-thread-1") {
        }.start();

        new Thread(() -> {
            for (int i = 1001; i <= 2000; i++) {
                RudderMessage trackMessage = getTrackMessage(i);
                assertThat("track message got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(trackMessage), getTrackJsonString(i, trackMessage)), is(true));
                RudderMessage identifyMessage = getIdentifyMessage(i);
                assertThat("identify message got serialized perfectly", areJsonStringsEqual(RudderGson.getInstance().toJson(identifyMessage), getIdentifyJsonString(i, identifyMessage)), is(true));
                messagesSerialized.addAndGet(2);
            }
        }, "serialize-rudder-message-thread-2") {
        }.start();

        await().atMost(60, SECONDS).until(() -> messagesSerialized.get() == 4000);

    }

    private String getTrackJsonString(int i, RudderMessage message) {
        return "{\"messageId\":\"" + message.getMessageId() + "\",\"channel\":\"mobile\",\"context\":{},\"originalTimestamp\":\"2022-03-14T06:46:41.365Z\",\"event\":\"TestEvent" + i + "\",\"properties\":{\"pinId " + i + "\":2351636,\"userLocationLatitude " + i + "\":55.661663449562205,\"locationsBefore " + i + "\":[{\"latitude " + i + "\":55.66132122924984,\"longitude " + i + "\":12.51671169784383},{\"latitude " + i + "\":55.661428115890374,\"longitude " + i + "\":12.51677390468785},{\"latitude " + i + "\":55.661663449562205,\"longitude " + i + "\":12.516869940636775}],\"userLocationLongitude " + i + "\":12.516869940636775},\"integrations\":{}}";
    }

    private String getIdentifyJsonString(int i, RudderMessage message) {
        return "{\"messageId\":\"" + message.getMessageId() + "\",\"channel\":\"mobile\",\"context\":{},\"originalTimestamp\":\"2022-03-14T06:46:41.365Z\",\"userId\":\"user name " + i + "\",\"event\":\"identify\",\"integrations\":{}}";
    }

    private RudderMessage getIdentifyMessage(int i) {
        RudderMessage message = new RudderMessageBuilder()
                .setEventName(MessageType.IDENTIFY)
                .setUserId("user name " + i)
                .build();
        return message;
    }

    private RudderMessage getTrackMessage(int i) {
        HashMap jsonData = new HashMap<String, Object>();
        jsonData.put("pinId " + i, 2351636);
        jsonData.put("userLocationLongitude " + i, 12.516869940636775);
        jsonData.put("userLocationLatitude " + i, 55.661663449562205);
        JSONArray locationsBefore = new JSONArray();
        try {
            locationsBefore.put(new JSONObject().put("latitude " + i, 55.66132122924984).put("longitude " + i, 12.51671169784383));
            locationsBefore.put(new JSONObject().put("latitude " + i, 55.661428115890374).put("longitude " + i, 12.51677390468785));
            locationsBefore.put(new JSONObject().put("latitude " + i, 55.661663449562205).put("longitude " + i, 12.516869940636775));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        jsonData.put("locationsBefore " + i, locationsBefore);
        return new RudderMessageBuilder().setEventName("TestEvent" + i).setProperty(jsonData).build();
    }

    public boolean areJsonStringsEqual(String json1, String json2) {
        JsonObject obj1 = JsonParser.parseString(json1).getAsJsonObject();
        JsonObject obj2 = JsonParser.parseString(json2).getAsJsonObject();
        boolean result = obj1.equals(obj2);
        if (!result) {
            System.out.println(Thread.currentThread().getName() + " " + json1);
            System.out.println(Thread.currentThread().getName() + " " + json2);
            System.out.println(Thread.currentThread().getName() + " " + "#################");
        }
        return result;
    }
}
