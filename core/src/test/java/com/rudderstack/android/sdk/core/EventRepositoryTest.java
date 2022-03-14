package com.rudderstack.android.sdk.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.rudderstack.android.sdk.core.util.Utils;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FlushUtils.class,
        RudderLogger.class,
        TextUtils.class, Utils.class})
public class EventRepositoryTest {
    /**
     * test flushEventsToServer is called properly
     */
    @Test
    public void flush() throws Exception {
        // data to be used
        final List<Integer> messageIds = new ArrayList<Integer>(ImmutableList.of(1, 2, 3, 4, 5));
        final List<String> messages = new ArrayList<String>(ImmutableList.of("{\"message\":\"m-1\"}",
                "{\"message\":\"m-2\"}", "{\"message\":\"m-3\"}", "{\"message\":\"m-4\"}", "{\"message\":\"m-5\"}"));
        final List<Integer> messageIdsParams = new ArrayList<Integer>(5);
        final List<String> messagesParams = new ArrayList<String>(5);

        //database manager mock
        DBPersistentManager dbPersistentManager = PowerMockito.mock(DBPersistentManager.class);
        PowerMockito.when(dbPersistentManager, "fetchAllEventsFromDB", messageIdsParams, messagesParams)
                .thenAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        messageIdsParams.addAll(messageIds);
                        messagesParams.addAll(messages);
                        return null;
                    }
                });

        // creating static mocks
        PowerMockito.spy(FlushUtils.class);
        PowerMockito.when(FlushUtils.class, "flushEventsToServer",
                anyString(), anyString(), anyString(), anyString()
        )
                .thenAnswer(new Answer<Utils.NetworkResponses>() {
                    @Override
                    public Utils.NetworkResponses answer(InvocationOnMock invocation) throws Throwable {
                        return Utils.NetworkResponses.SUCCESS;
                    }
                });
        //mocking timestamp
        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.class, "getTimeStamp"
        )
                .thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return "2022-03-14T06:46:41.365Z";
                    }
                });
        //expectation
        String expectedPayload = "{\n" +
                "  \"sentAt\": \"2022-03-14T06:46:41.365Z\",\n" +
                "  \"batch\": [\n" +
                "    {\n" +
                "      \"message\": \"m-1\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-2\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-3\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-4\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"message\": \"m-5\",\n" +
                "      \"sentAt\": \"2022-03-14T06:46:41.365Z\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        //when
        boolean result = FlushUtils.flush(false, null, messageIdsParams, messagesParams,
                30, "https://api.rudderlabs.com/", dbPersistentManager
                , "auth_key", "anon_id");


        //verify flushEventsToServer is called once with proper arguments
        //we use argument captor, cause we would need to remove spaces from argument
        PowerMockito.verifyStatic(FlushUtils.class, Mockito.times(1));
        ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg2 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg3 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> arg4 = ArgumentCaptor.forClass(String.class);

        FlushUtils.flushEventsToServer(
                arg1.capture(),
                arg2.capture(),
                arg3.capture(),
                arg4.capture()
//                Mockito.eq(expectedPayload.replace("\n", "").replace(" ", "")),
//                Mockito.eq("https://api.rudderlabs.com/"),
//                Mockito.eq("auth_key"),
//                Mockito.eq("anon_id")
                );
        assertThat(result, Matchers.is(true));
        System.out.println(arg1.getValue());
        assertThat(arg1.getValue().replace(" ", ""),
                Matchers.is(expectedPayload.replace("\n", "").replace(" ", "")));
        System.out.println(arg2.getValue());
        assertThat(arg2.getValue().replace(" ", ""), Matchers.is("https://api.rudderlabs.com/"));
        System.out.println(arg3.getValue());
        assertThat(arg3.getValue(), Matchers.is("auth_key"));
        System.out.println(arg4.getValue());
        assertThat(arg4.getValue(), Matchers.is("anon_id"));
    }
    /*public void partialMockTest() throws Exception {
        assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("noMock"));
        PowerMockito.spy(MockSample.class);
        PowerMockito.when(MockSample.class, "returnNotMockIfNotMocked").thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "mocked";
            }
        });
        assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("mocked"));
        assertThat(MockSample.return2IfNotMocked(), Matchers.is(2));

        *//*try (MockedStatic<MockSample> utilities = Mockito.mockStatic(MockSample.class)) {
            utilities.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    MockSample.return2IfNotMocked();
                }
            }).thenReturn(*//**//*f.call(MockSample.class,new Object() , new Object[]{})*//**//*2);
            utilities.when(new MockedStatic.Verification() {
                @Override
                public void apply() throws Throwable {
                    MockSample.returnNotMockIfNotMocked();
                }
            }).thenReturn("mocked");
            assertThat(MockSample.returnNotMockIfNotMocked() , Matchers.is("mocked"));
            assertThat(MockSample.return2IfNotMocked(), Matchers.is(2));

        }*//*

//        assertThat(StaticUtils.name()).isEqualTo("Baeldung");
    }*/
}