package com.rudderstack.android.sdk.core;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import org.hamcrest.MatcherAssert;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.net.ssl.*" })
public class RudderNetworkManagerTest {

    private final static String DUMMY_AUTH = "dp_auth";
    private final static String DUMMY_ANONYMOUS_ID = "anon_id";
    private final static String DMT_AUTH_HEADER = "dmt_auth_header";
    private RudderNetworkManager networkManager;

    private static final String TEST_BATCH = "{\n" +
            "\t\"sentAt\":\"2022-01-19T19:35:40.000z\",\n" +
            "\t\"batch\":[\n" +
            "\t\t{\n" +
            "  \"type\": \"Track\",\n" +
            "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
            "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
            "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
            "  \"userId\": \"debanjanchatterjee\",\n" +
            "  \"context\": null,\n" +
            "  \"integrations\": {\n" +
            "\t\t\"All\": true,\n" +
            "    \"Some_Integration\": true,\n" +
            "\t\t\"Some_Integration_2\": false\n" +
            "  },\n" +
            "  \"event\": \"Java Test\",\n" +
            "  \"properties\": {\n" +
            "    \"count\": \"1\"\n" +
            "  }\n" +
            "},\n" +
            "\t\t{\n" +
            "  \"type\": \"Alias\",\n" +
            "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
            "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
            "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
            "  \"userId\": \"debanjanchatterjee\",\n" +
            "  \"context\": {\n" +
            "\t\t\"app\": {\n" +
            "\t\t\t\"build\": \"0.0.0\",\n" +
            "\t\t\t\"name\": \"dummy_name\",\n" +
            "\t\t\t\"namespace\": \"android\",\n" +
            "\t\t\t\"version\": \"1.0.1\"\n" +
            "\t\t},\n" +
            "\t\t\"traits\":{\n" +
            "\t\t\t\"trait_1\":\"trait_name_1\",\n" +
            "\t\t\t\"trait_2\":\"trait_name_2\"\n" +
            "\t\t},\n" +
            "\t\t\"library\":{\n" +
            "\t\t\t\"name\":\"com.rudderstack.android.sdk.core\",\n" +
            "\t\t\t\"version\":\"0.0.0\"\n" +
            "\t\t},\n" +
            "\t\t\"os\":{\n" +
            "\t\t\t\"name\": \"Android\",\n" +
            "\t\t\t\"version\": \"11\"\n" +
            "\t\t},\n" +
            "\t\t\"screen\":{\n" +
            "\t\t\t\"density\": 20,\n" +
            "\t\t\t\"width\": 1200,\n" +
            "\t\t\t\"height\": 1800\n" +
            "\t\t},\n" +
            "\t\t\"userAgent\":\"agent\",\n" +
            "\t\t\"locale\": \"some_locale\",\n" +
            "\t\t\"device\": {\n" +
            "\t\t\t\"id\": \"s_id\",\n" +
            "\t\t\t\"manufacturer\":\"Nothing\",\n" +
            "\t\t\t\"model\":\"One\",\n" +
            "\t\t\t\"name\":\"One\",\n" +
            "\t\t\t\"token\":\"token\",\n" +
            "\t\t\t\"adTrackingEnabled\": false,\n" +
            "\t\t\t\"advertisingId\": \"a_id\"\n" +
            "\t\t},\n" +
            "\t\t\"network\": {\n" +
            "\t\t\t\"carrier\": \"s_carrier\",\n" +
            "\t\t\t\"wifi\": true,\n" +
            "\t\t\t\"bluetooth\":true,\n" +
            "\t\t\t\"cellular\":true\n" +
            "\t\t},\n" +
            "\t\t\"timezone\": \"timezone\",\n" +
            "\t\t\"sessionId\": \"some_session\",\n" +
            "\t\t\"sessionStart\": true,\n" +
            "\t\t\"externalId\":[\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"external_id_1\": \"s_e_id_1\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"external_id_2\": \"s_e_id_2\"\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"external_id_3\": \"s_e_id_3\"\n" +
            "\t\t\t}\n" +
            "\t\t],\n" +
            "\t\t\"customContextMap\":{\n" +
            "\t\t\t\"custom_context_1\": {\n" +
            "\t\t\t\t\"some_key\":\"some_value_1\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"custom_context_2\": \"some_value_2\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "  \"integrations\": {\n" +
            "    \"All\": true\n" +
            "  },\n" +
            "  \"previousId\": \"172d84b9-a684-4249-8646-0994173555cd\"\n" +
            "}\n" +
            "\t]\n" +
            "}";
    private static final String BATCH_WITH_DMT_HEADER = "{" +
            "\n\"metadata\":{" +
            "\n\t\"Custom-Authorization\":\"" + DMT_AUTH_HEADER + "\"" +
            "\n\t},\n"
            + TEST_BATCH.substring(1);

    @Before
    public void setup() {
        networkManager = new RudderNetworkManager(DUMMY_AUTH, DUMMY_ANONYMOUS_ID, DMT_AUTH_HEADER, true);

    }

    @Test
    public void withAddedMetadataToRequestPayload() throws JSONException {
        String withAddedCustomAuth = networkManager.withAddedMetadataToRequestPayload(TEST_BATCH, true);
        JSONAssert.assertEquals(BATCH_WITH_DMT_HEADER, withAddedCustomAuth, true);
    }
    OutputStream customWrappedOS;

    @Test
    /**
     * Keeping gzip compression out of scope. Just testing if the streams are working.
     */
    public void getHttpConnection() throws IOException, JSONException {
        String testingPayload = "{\"test\":\"test\", \"test2\":\"test2\", \"test3\":{   \"test4\":\"test4\"}}";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpURLConnection connection = networkManager.updateHttpConnection(getDummyHttpURLConnection(),
                RudderNetworkManager.RequestMethod.POST, testingPayload, false,
                Map.of("Content-Encoding", "gzip"), outputStream -> {
                    customWrappedOS = new GZIPOutputStream(outputStream) {

                        @Override
                        public void write(byte[] b) throws IOException {
                            super.write(b);
                        }

                        @Override
                        public synchronized void write(byte[] buf, int off, int len) throws IOException {
                            super.write(buf, off, len);
                            baos.write(buf, off, len);
                        }
                    };
                    return customWrappedOS;
                }

        );
        assertNotNull(connection);
        ByteArrayInputStream bios = new ByteArrayInputStream(baos.toByteArray());

        String result = new BufferedReader(new InputStreamReader(bios))
                .lines().collect(Collectors.joining("\n"));
        MatcherAssert.assertThat(result, notNullValue());
        JSONAssert.assertEquals(testingPayload, result, true);

    }

    private HttpURLConnection getDummyHttpURLConnection() throws MalformedURLException {
        return new HttpURLConnection(new URL("http://www.google.com")) {
            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public void connect() throws IOException {

            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return new ByteArrayOutputStream();
            }
        };
    }
}