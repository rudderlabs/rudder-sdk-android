package com.rudderstack.android.sdk.core;


import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.concurrent.TimeUnit.SECONDS;


import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rudderstack.android.sdk.core.gson.RudderGson;
import com.rudderstack.android.sdk.core.util.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class})
public class RudderGsonTest {

    @Before
    public void setup() throws Exception {
        //mocking timestamp
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp"
                )
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
                    RudderContext context = null;
                    try {
                        context = getRudderContext(i);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    assertThat("contexts got serialized perfectly " + i, areJsonStringsEqual(RudderGson.serialize(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 1001; i <= 2000; i++) {
                    RudderContext context = null;
                    try {
                        context = getRudderContext(i);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    assertThat("contexts got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 2001; i <= 3000; i++) {
                    RudderContext context = null;
                    try {
                        context = getRudderContext(i);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    assertThat("contexts got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(context), getRudderContextJsonString(i)), is(true));
                    contextsSerialized.addAndGet(1);
                }
            }
        }.start();
        await().atMost(60, SECONDS).until(() -> contextsSerialized.get() == 3000);
    }

    private RudderContext getRudderContext(int i) throws NoSuchFieldException, IllegalAccessException {
        RudderContext context = getDefaultRudderContext();
        Map<String, Object> customContextMap = new HashMap<>();
        Map<String, String> companyContext = new HashMap<>();
        companyContext.put("name", "company" + i);
        companyContext.put("city", "city" + i);
        customContextMap.put("company", companyContext);
        Map<String, String> personContext = new HashMap<>();
        personContext.put("tier", "enterprise" + i);
        customContextMap.put("person", personContext);
        ReflectionUtils.setPrivateField(context, "customContextMap", customContextMap);
        return context;
    }
    private RudderContext getDefaultRudderContext() throws NoSuchFieldException, IllegalAccessException {
        RudderContext context = Mockito.mock(RudderContext.class);
        RudderApp app = Mockito.mock(RudderApp.class);
        ReflectionUtils.setPrivateField(app, "build", "2");
        ReflectionUtils.setPrivateField(app, "name", "rudderstack-android-sdk-desu");
        ReflectionUtils.setPrivateField(app, "nameSpace", "com.rudderstack.android.sdk.core");
        ReflectionUtils.setPrivateField(app, "version", "1.1");
        ReflectionUtils.setPrivateField(context, "app", app);
        RudderLibraryInfo libraryInfo = Mockito.mock(RudderLibraryInfo.class);
        ReflectionUtils.setPrivateField(libraryInfo, "name", "com.rudderstack.android.sdk.core");
        ReflectionUtils.setPrivateField(libraryInfo, "version", "1.20.1");
        ReflectionUtils.setPrivateField(context, "libraryInfo", libraryInfo);
        RudderOSInfo osInfo = Mockito.mock(RudderOSInfo.class);
        ReflectionUtils.setPrivateField(osInfo, "name", "Android");
        ReflectionUtils.setPrivateField(osInfo, "version", "14");
        ReflectionUtils.setPrivateField(context, "osInfo", osInfo);
        RudderScreenInfo screenInfo = Mockito.mock(RudderScreenInfo.class);
        ReflectionUtils.setPrivateField(screenInfo, "density", 3);
        ReflectionUtils.setPrivateField(screenInfo, "height", 736);
        ReflectionUtils.setPrivateField(screenInfo, "width", 414);
        ReflectionUtils.setPrivateField(context, "screenInfo", screenInfo);
        ReflectionUtils.setPrivateField(context, "userAgent", "Dalvik/2.1.0 (Linux; U; Android 14; sdk_gphone64_arm64 Build/UPB4.230623.005)");
        ReflectionUtils.setPrivateField(context, "locale", "en-US");
        RudderNetwork networkInfo = Mockito.mock(RudderNetwork.class);
        ReflectionUtils.setPrivateField(networkInfo, "isCellularEnabled", true);
        ReflectionUtils.setPrivateField(networkInfo, "isWifiEnabled", true);
        ReflectionUtils.setPrivateField(networkInfo, "isBluetoothEnabled", false);
        ReflectionUtils.setPrivateField(networkInfo, "carrier", "T-Mobile");
        ReflectionUtils.setPrivateField(context, "networkInfo", networkInfo);
        RudderDeviceInfo deviceInfo = Mockito.mock(RudderDeviceInfo.class);
        ReflectionUtils.setPrivateField(deviceInfo, "type", "Android");
        ReflectionUtils.setPrivateField(deviceInfo, "manufacturer", "Google");
        ReflectionUtils.setPrivateField(deviceInfo, "model", "sdk_gphone64_arm64");
        ReflectionUtils.setPrivateField(deviceInfo, "name", "emu64a");
        ReflectionUtils.setPrivateField(deviceInfo, "deviceId", "10f9e80f-5342-48e0-8908-1b221179895b");
        ReflectionUtils.setPrivateField(context, "deviceInfo", deviceInfo);
        ReflectionUtils.setPrivateField(context, "timezone", "Asia/Kolkata");
        ReflectionUtils.setPrivateField(context, "sessionId", 1699538605l);
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
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(traits), getTraitsJsonString(i)), is(true));
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
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(traits), getTraitsJsonString(i)), is(true));
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
                    assertThat("traits got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(traits), getTraitsJsonString(i)), is(true));
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
            try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
                utilities.when(Utils::getTimeStamp).thenReturn("2022-03-14T06:46:41.365Z");
                for (int i = 1; i <= 1000; i++) {
                    RudderMessage trackMessage = getTrackMessage(i);
                    assertThat("track message got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(trackMessage), getTrackJsonString(i, trackMessage)), is(true));
                    RudderMessage identifyMessage = getIdentifyMessage(i);
                    assertThat("identify message got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(identifyMessage), getIdentifyJsonString(i, identifyMessage)), is(true));
                    messagesSerialized.addAndGet(2);
                }
            }
        }, "serialize-rudder-message-thread-1") {
        }.start();

        new Thread(() -> {
            try (MockedStatic<Utils> utilities = Mockito.mockStatic(Utils.class)) {
                utilities.when(Utils::getTimeStamp).thenReturn("2022-03-14T06:46:41.365Z");
                for (int i = 1001; i <= 2000; i++) {
                    RudderMessage trackMessage = getTrackMessage(i);
                    assertThat("track message got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(trackMessage), getTrackJsonString(i, trackMessage)), is(true));
                    RudderMessage identifyMessage = getIdentifyMessage(i);
                    assertThat("identify message got serialized perfectly", areJsonStringsEqual(RudderGson.serialize(identifyMessage), getIdentifyJsonString(i, identifyMessage)), is(true));
                    messagesSerialized.addAndGet(2);
                }
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
        return result;
    }

    @Test
    public void testGsonWithInvalidNumbers() {
        RudderContext context = new RudderContext();
        // insert traits in the context which contains invalid numbers
        context.updateTraitsMap(getInvalidNumbersMap());
        // set custom context with invalid numbers
        context.setCustomContexts(getInvalidNumbersMap());

        Map<String, Object> eventProperties = getInvalidNumbersMap();
        eventProperties.put("list", new ArrayList<>(Arrays.asList(POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE, Double.NaN)));
        eventProperties.put("map", getInvalidNumbersMap());

        RudderMessage message = new RudderMessageBuilder().setEventName("TestEvent").setProperty(eventProperties).build();
        message.setContext(context);


        String outputJsonString = RudderGson.serialize(message);
        String expectedJsonString = "{\n" +
                "  \"messageId\": \"" + message.getMessageId() + "\",\n" +
                "  \"channel\": \"mobile\",\n" +
                "  \"originalTimestamp\": \"2022-03-14T06:46:41.365Z\",\n" +
                "  \"event\": \"TestEvent\",\n" +
                "  \"properties\": {\n" +
                "    \"maxValue\": 1.7976931348623157E308,\n" +
                "    \"double\": 45.0,\n" +
                "    \"positiveInfinity\": \"Infinity\",\n" +
                "    \"float\": 45.0,\n" +
                "    \"list\": [\n" +
                "      \"Infinity\",\n" +
                "      \"-Infinity\",\n" +
                "      1.7976931348623157E308,\n" +
                "      4.9E-324,\n" +
                "      \"NaN\"\n" +
                "    ],\n" +
                "    \"long\": 45,\n" +
                "    \"int\": 45,\n" +
                "    \"negativeInfinity\": \"-Infinity\",\n" +
                "    \"general\": 45,\n" +
                "    \"minValue\": 4.9E-324,\n" +
                "    \"nan\": \"NaN\",\n" +
                "    \"map\": {\n" +
                "      \"general\": 45,\n" +
                "      \"minValue\": 4.9E-324,\n" +
                "      \"maxValue\": 1.7976931348623157E308,\n" +
                "      \"double\": 45.0,\n" +
                "      \"positiveInfinity\": \"Infinity\",\n" +
                "      \"nan\": \"NaN\",\n" +
                "      \"float\": 45.0,\n" +
                "      \"list\": [\n" +
                "        \"Infinity\",\n" +
                "        \"-Infinity\",\n" +
                "        1.7976931348623157E308,\n" +
                "        4.9E-324,\n" +
                "        \"NaN\",\n" +
                "        45,\n" +
                "        45.0\n" +
                "      ],\n" +
                "      \"map\": {\n" +
                "        \"general\": 45,\n" +
                "        \"minValue\": 4.9E-324,\n" +
                "        \"maxValue\": 1.7976931348623157E308,\n" +
                "        \"double\": 45.0,\n" +
                "        \"positiveInfinity\": \"Infinity\",\n" +
                "        \"nan\": \"NaN\",\n" +
                "        \"negativeInfinity\": \"-Infinity\"\n" +
                "      },\n" +
                "      \"long\": 45,\n" +
                "      \"int\": 45,\n" +
                "      \"negativeInfinity\": \"-Infinity\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"integrations\": {}\n" +
                "}";
        assertThat("Invalid Numbers are removed and the message is serialized perfectly", outputJsonString, is(expectedJsonString.replace("\n", "").replace(" ", "")));
    }

    Map<String, Object> getInvalidNumbersMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("positiveInfinity", POSITIVE_INFINITY);
        map.put("negativeInfinity", Double.NEGATIVE_INFINITY);
        map.put("maxValue", Double.MAX_VALUE);
        map.put("minValue", Double.MIN_VALUE);
        map.put("nan", Double.NaN);
        map.put("general", 45);
        map.put("double", 45.0);
        map.put("float", 45.0f);
        map.put("long", 45L);
        map.put("int", 45);
        // add a nested map with invalid numbers
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("positiveInfinity", POSITIVE_INFINITY);
        nestedMap.put("negativeInfinity", Double.NEGATIVE_INFINITY);
        nestedMap.put("maxValue", Double.MAX_VALUE);
        nestedMap.put("minValue", Double.MIN_VALUE);
        nestedMap.put("nan", Double.NaN);
        nestedMap.put("general", 45);
        nestedMap.put("double", 45.0);
        map.put("map", nestedMap);
        // add a nested array with invalid numbers
        List<Object> nestedList = new ArrayList<>();
        nestedList.add(POSITIVE_INFINITY);
        nestedList.add(Double.NEGATIVE_INFINITY);
        nestedList.add(Double.MAX_VALUE);
        nestedList.add(Double.MIN_VALUE);
        nestedList.add(Double.NaN);
        nestedList.add(45);
        nestedList.add(45.0);
        map.put("list", nestedList);
        return map;
    }
}
