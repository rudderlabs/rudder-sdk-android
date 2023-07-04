package com.rudderstack.android.sdk.core;

import com.google.gson.Gson;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

//check proper serialiization of incoming json
public class SerializationTest {
    private static final String INCOMING_JSON_SERVER_CONFIG = "" +
            "{\n" +
            "    \"appKey\": \"Rudderstack_event_QA\",\n" +
            "    \"blacklistedEvents\": [\n" +
            "        {\n" +
            "            \"eventName\": \"\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"dataCenter\": \"EU-01\",\n" +
            "    \"eventDelivery\": false,\n" +
            "    \"eventDeliveryTS\": 16425.0,\n" +
            "    \"eventFilteringOption\": \"disable\",\n" +
            "    \"oneTrustCookieCategories\": {\n" +
            "        \"web\": [\n" +
            "            {\n" +
            "                \"oneTrustCookieCategory\": \"\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"oneTrustCookieCategory\": \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"restApiKey\": \"7ed6f931-2313-432f-a643-880426bd226e\",\n" +
            "    \"useNativeSDK\": {\n" +
            "        \"android\": true,\n" +
            "        \"ios\": true,\n" +
            "        \"web\": false\n" +
            "    },\n" +
            "    \"whitelistedEvents\": [\n" +
            "        {\n" +
            "            \"eventName\": \"\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    private static final String INCOMING_JSON =
            "{\n" +
                    "\"source\" : {" +
            "           \"id\" : \"23\",\n" +
            "           \"name\" : \"dummy\",\n" +
            "           \"enabled\" : true,\n" +
            "           \"updatedAt\" : \"23-10-23\",\n" +
            "           \"destinations\" : [\n" +
            "               {   \n" +
            "                   \"id\" : \"1234\",\n" +
            "                   \"name\" : \"der\",\n" +
            "                   \"enabled\" : true,\n" +
            "                   \"updatedAt\" : \"23-10-22\",\n" +
            "                   \"config\" : " + INCOMING_JSON_SERVER_CONFIG + ",\n" +
            "\"shouldApplyDeviceModeTransformation\":false" + ",\n" +
                    "\"propagateEventsUntransformedOnError\":false" +
            "           }" +
            "       ]\n" +
            "   }\n" +
            "}";
    @Test
    public void testObjectOutputStream() throws IOException, ClassNotFoundException {
        System.out.println(INCOMING_JSON);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        baos.write(INCOMING_JSON.getBytes(StandardCharsets.UTF_8));
//
        RudderServerConfig rudderServerConfig = new Gson().fromJson(INCOMING_JSON, RudderServerConfig.class);
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(rudderServerConfig);
        os.flush();

        ByteArrayInputStream bios = new ByteArrayInputStream(baos.toByteArray());
        RudderServerConfig desrializedConfig = (RudderServerConfig) new ObjectInputStream(bios).readObject();
        String configSerialized = new Gson().toJson(desrializedConfig);
        System.out.println(configSerialized);

        ByteArrayOutputStream dummyos = new ByteArrayOutputStream();
        ObjectOutputStream dummyoos = new ObjectOutputStream(dummyos);
        dummyoos.writeObject("abcd");
        dummyoos.flush();

        ByteArrayInputStream dummyBios = new ByteArrayInputStream(dummyos.toByteArray());
//        dummyBios.close();
        ObjectInputStream dummyois = new ObjectInputStream(dummyBios);

        MatcherAssert.assertThat("abcd", Matchers.is((String) dummyois.readObject()));
        MatcherAssert.assertThat(INCOMING_JSON.replace(" ", "").replace("\n", ""), Matchers.is(configSerialized));
    }

    @Test
    public void dummyTesting() throws IOException, ClassNotFoundException {
        String s = "Hello World";
        byte[] b = {'e', 'x', 'a', 'm', 'p', 'l', 'e'};

        // create a new file with an ObjectOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);

        // write something in the file
        oout.writeObject(s);
        oout.writeObject(b);
        oout.flush();

        // create an ObjectInputStream for the file we created before
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));

        // read and print an object and cast it as string

        System.out.println("" + (String) ois.readObject());

        // read and print an object and cast it as string
        byte[] read = (byte[]) ois.readObject();
        String s2 = new String(read);
        System.out.println("" + s2);
    }
}
