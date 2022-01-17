package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.BLACKLISTED_EVENTS;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.DISABLE;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.EVENT_FILTERING_OPTION;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.WHITELISTED_EVENTS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;

import android.text.TextUtils;
import android.util.Log;

import com.rudderstack.android.sdk.core.util.Utils;

import org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
@RunWith(PowerMockRunner.class)
@PrepareForTest({TextUtils.class, Log.class})
public class RudderEventFilteringPluginTest {
    private final List<RudderServerDestination> testDestinations = new ArrayList();

    private RudderEventFilteringPlugin plugin;



    @Before
    public void setup() {
        List<Map<String, String>> blackListedEvents1 = new ArrayList<>();
        Map<String, String> event1_1 = new HashMap<>();
        event1_1.put("eventName", "w_e_1_1");
        Map<String, String> event1_2 = new HashMap<>();
        event1_2.put("eventName", "w_e_1_2");
        Map<String, String> event1_3 = new HashMap<>();
        event1_3.put("eventName", "w_e_1_3");
        Map<String, String> event1_4 = new HashMap<>();
        event1_4.put("eventName", "w_e_1_4");

        blackListedEvents1.add(event1_1);
        blackListedEvents1.add(event1_2);
        blackListedEvents1.add(event1_3);
        blackListedEvents1.add(event1_4);

        List<Map<String, String>> whiteListedEvents2 = new ArrayList<>();
        Map<String, String> event2_1 = new HashMap<>();
        event2_1.put("eventName", "w_e_2_1");
        Map<String, String> event2_2 = new HashMap<>();
        event2_2.put("eventName", "w_e_2_2");
        Map<String, String> event2_3 = new HashMap<>();
        event2_3.put("eventName", "w_e_2_3");
        Map<String, String> event2_4 = new HashMap<>();
        event2_4.put("eventName", "w_e_2_4");

        whiteListedEvents2.add(event2_1);
        whiteListedEvents2.add(event2_2);
        whiteListedEvents2.add(event2_3);
        whiteListedEvents2.add(event2_4);

        testDestinations.add(createServerDestination("1", "D-1",
                true, "d-1",
                "ddd-1", EventFilteringStatus.BLACK_LISTED,
                null, blackListedEvents1
        ));
        testDestinations.add(createServerDestination("2", "D-2",
                true, "D-2",
                "ddd-2", EventFilteringStatus.WHITE_LISTED,
                 whiteListedEvents2, null
        ));

        mockTheLog();
        PowerMockito.mockStatic(TextUtils.class);
//        mockTextUtils = mockStatic(TextUtils.class);

        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {

                CharSequence a = (CharSequence) invocation.getArguments()[0];

                return !(a != null && a.length() > 0);

            }
        });

        plugin = new RudderEventFilteringPlugin(testDestinations);

    }

    private void mockTheLog() {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.when(Log.d(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
        PowerMockito.when(Log.e(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
        PowerMockito.when(Log.w(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
        PowerMockito.when(Log.i(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
        PowerMockito.when(Log.v(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
        PowerMockito.when(Log.wtf(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        });
    }

    @After
     public void destroy(){
//         mockTextUtils.close();
     }


    @Test
    public void isEventAllowed() throws Exception {

        RudderMessage msg =
                PowerMockito.mock(RudderMessage.class);
        PowerMockito.when(msg,"getEventName").thenReturn("w_e_1_1");
        PowerMockito.when(msg,"getType" ).thenReturn(MessageType.TRACK);
//        msg.setEventName();
//        msg.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("ddd-1", msg), is(false));

        /*RudderMessage msg2 = PowerMockito.mock(RudderMessage.class);
        msg2.setEventName("w_e_1_2");
        msg2.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-1", msg2), is(false));

        RudderMessage msg3 = new RudderMessage();
        msg3.setEventName("w_e_1_3");
        msg3.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-1", msg3), is(false));

        RudderMessage msg4 = new RudderMessage();
        msg4.setEventName("w_e_1_4");
        msg4.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-1", msg4), is(false));

        RudderMessage msg5 = new RudderMessage();
        msg5.setEventName("w_e_1_5");
        msg5.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-1", msg5), is(true));

        //check for whitelisted

        RudderMessage msgW = new RudderMessage();
        msgW.setEventName("w_e_2_1");
        msgW.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-2", msgW), is(true));

        RudderMessage msgW2 = new RudderMessage();
        msgW2.setEventName("w_e_2_2");
        msgW2.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-2", msgW2), is(true));

        RudderMessage msgW3 = new RudderMessage();
        msgW3.setEventName("w_e_2_3");
        msgW3.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-2", msgW3), is(true));

        RudderMessage msgW4 = new RudderMessage();
        msgW4.setEventName("w_e_2_4");
        msgW4.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-2", msgW4), is(true));

        RudderMessage msgW5 = new RudderMessage();
        msgW5.setEventName("w_e_2_5");
        msgW5.setType(MessageType.TRACK);
        assertThat(plugin.isEventAllowed("D-2", msgW5), is(false));
*/
    }

    @Test
    public void getWhitelistEvents() {
    }

    @Test
    public void getBlacklistEvents() {
    }

    private RudderServerDestination createServerDestination(String destinationId, String destinationName, boolean isDestinationEnabled,
                                                            String destinationDefinitionName, String destinationDefinitionDisplayName,
//                                                            Map<String, Object> destinationConfig,
                                                            EventFilteringStatus eventFilteringStatus,
                                                            @Nullable List<Map<String, String>> whiteListedEvents,
                                                            @Nullable List<Map<String, String>> blackListedEvents) {
        Map<String, Object> destinationConfig = new HashMap<>();
        destinationConfig.put(EVENT_FILTERING_OPTION, eventFilteringStatus == EventFilteringStatus.DISABLE ? DISABLE :
                eventFilteringStatus == EventFilteringStatus.WHITE_LISTED ? WHITELISTED_EVENTS : BLACKLISTED_EVENTS);
        if (blackListedEvents != null)
            destinationConfig.put(BLACKLISTED_EVENTS, blackListedEvents);
        if (whiteListedEvents != null)
            destinationConfig.put(WHITELISTED_EVENTS, whiteListedEvents);

        RudderServerDestination destination = new RudderServerDestination();
        String timestamp = Utils.getTimeStamp();

        destination.destinationDefinition = new RudderServerDestinationDefinition();
        destination.destinationDefinition.definitionName = destinationDefinitionName;
        destination.destinationDefinition.displayName = destinationDefinitionDisplayName;
        destination.destinationDefinition.updatedAt = timestamp;

        destination.destinationId = destinationId;
        destination.destinationName = destinationName;
        destination.isDestinationEnabled = isDestinationEnabled;
        destination.updatedAt = timestamp;
        destination.destinationConfig = destinationConfig;

        return destination;
    }

    static enum EventFilteringStatus {
        DISABLE,
        WHITE_LISTED,
        BLACK_LISTED
    }
}