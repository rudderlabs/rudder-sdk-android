package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.BLACKLISTED_EVENTS;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.DISABLE;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.EVENT_FILTERING_OPTION;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.WHITELISTED_EVENTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import android.text.TextUtils;
import android.util.Log;

import com.rudderstack.android.sdk.core.util.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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


    private RudderEventFilteringPlugin plugin_Blacklist;
    private RudderEventFilteringPlugin plugin_Whitelist;
    private RudderEventFilteringPlugin plugin_Disable;
    private RudderEventFilteringPlugin getPlugin_Whitelist_LifeCycle;
    private RudderEventFilteringPlugin getPlugin_Blacklist_LifeCycle;

    @Before
    public void setup() {
        mockTheLog();
        PowerMockito.mockStatic(TextUtils.class);

        final List<RudderServerDestination> testDestinations_Amplitude_BlacklistedEvents = new ArrayList();
        final List<RudderServerDestination> testDestinations_Amplitude_WhitelistedEvents = new ArrayList();
        final List<RudderServerDestination> testDestinations_Amplitude_Disable = new ArrayList();

        List<Map<String, String>> blackListedEvents = new ArrayList<>();
        Map<String, String> event1_1 = new HashMap<>();
        event1_1.put("eventName", "Black-1");
        Map<String, String> event1_2 = new HashMap<>();
        event1_2.put("eventName", "Black-2");
        Map<String, String> event1_3 = new HashMap<>();
        event1_3.put("eventName", "Black-3");
        Map<String, String> event1_4 = new HashMap<>();
        event1_4.put("eventName", "Black-4");

        blackListedEvents.add(event1_1);
        blackListedEvents.add(event1_2);
        blackListedEvents.add(event1_3);
        blackListedEvents.add(event1_4);

        List<Map<String, String>> whiteListedEvents = new ArrayList<>();
        Map<String, String> event2_1 = new HashMap<>();
        event2_1.put("eventName", "White-1");
        Map<String, String> event2_2 = new HashMap<>();
        event2_2.put("eventName", "White-2");
        Map<String, String> event2_3 = new HashMap<>();
        event2_3.put("eventName", "White-3");
        Map<String, String> event2_4 = new HashMap<>();
        event2_4.put("eventName", "White-4");

        whiteListedEvents.add(event2_1);
        whiteListedEvents.add(event2_2);
        whiteListedEvents.add(event2_3);
        whiteListedEvents.add(event2_4);

        List<Map<String, String>> lifeCycleEvents = new ArrayList<>();
        Map<String, String> event3_1 = new HashMap<>();
        event3_1.put("eventName", "White-1");
        Map<String, String> event3_2 = new HashMap<>();
        event3_2.put("eventName", "White-2");
        Map<String, String> event3_3 = new HashMap<>();
        event3_3.put("eventName", "White-3");
        Map<String, String> event3_4 = new HashMap<>();
        event3_4.put("eventName", "White-4");

        lifeCycleEvents.add(event3_1);
        lifeCycleEvents.add(event3_2);
        lifeCycleEvents.add(event3_3);
        lifeCycleEvents.add(event3_4);

        // Blacklisted
        testDestinations_Amplitude_BlacklistedEvents.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, blackListedEvents
        ));
        // Whitelisted
        testDestinations_Amplitude_WhitelistedEvents.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.WHITE_LISTED, whiteListedEvents, null
        ));
        // Disable
        testDestinations_Amplitude_Disable.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.DISABLE,
                null, blackListedEvents
        ));

//        testDestinations_Amplitude_BlacklistedEvents.add(createServerDestination("2", "D-2",
//                true, "d-2",
//                "ddd-2", EventFilteringStatus.WHITE_LISTED,
//                 whiteListedEvents, null
//        ));


        // For any string values
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
        // For null values
        PowerMockito.when(TextUtils.isEmpty(null)).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        });

        plugin_Blacklist = new RudderEventFilteringPlugin(testDestinations_Amplitude_BlacklistedEvents);
        plugin_Whitelist = new RudderEventFilteringPlugin(testDestinations_Amplitude_WhitelistedEvents);
        plugin_Disable = new RudderEventFilteringPlugin(testDestinations_Amplitude_Disable);
    }

    private void mockTheLog() {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.when(Log.d(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
        PowerMockito.when(Log.e(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
        PowerMockito.when(Log.w(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
        PowerMockito.when(Log.i(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
        PowerMockito.when(Log.v(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
        PowerMockito.when(Log.wtf(anyString(), anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                return 0;
            }
        });
    }

    @Test
    public void isEventAllowed() throws Exception {
        RudderMessage message = PowerMockito.mock(RudderMessage.class);

        // When event is blocked or not whitelisted -> Do not allow the event
        PowerMockito.when(message,"getEventName").thenReturn("Black-1");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Blacklist.isEventAllowed("Amplitude", message), is(false));

        PowerMockito.when(message,"getEventName").thenReturn("AsNotWhitelisted-EventBlocked");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(false));

        // When event is whitelisted or not blacked -> Allow the event
        PowerMockito.when(message,"getEventName").thenReturn("AsNotBlacklisted-EventAllowed");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Blacklist.isEventAllowed("Amplitude", message), is(true));

        PowerMockito.when(message,"getEventName").thenReturn("White-1");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));

        // When message.getType() == null or empty -> Allow the event (If message.type is undetermined)
        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");
        PowerMockito.when(message,"getType").thenReturn(null);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));


        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");
        PowerMockito.when(message,"getType").thenReturn("");
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));

        // When message.getEventName == null -> Allow the event (If message.getEventName is null
        PowerMockito.when(message,"getEventName").thenReturn(null);
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));

        PowerMockito.when(message,"getEventName").thenReturn("");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));

        // When eventFilterOption is disable (backward compatibility) -> Allow the event
        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Disable.isEventAllowed("Amplitude", message), is(true));
        assertThat(plugin_Whitelist.isEventAllowed("Adobe", message), is(true));
        assertThat(plugin_Blacklist.isEventAllowed("Adobe", message), is(true));

        // Event name with white spaces -> If the event is whitelisted or not blacklisted then allow the event.
        PowerMockito.when(message,"getEventName").thenReturn("    AsNotBlacklisted-EventAllowed     ");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Blacklist.isEventAllowed("Amplitude", message), is(true));

        PowerMockito.when(message,"getEventName").thenReturn("      White-1         ");
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        assertThat(plugin_Whitelist.isEventAllowed("Amplitude", message), is(true));



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

    enum EventFilteringStatus {
        DISABLE,
        WHITE_LISTED,
        BLACK_LISTED
    }
}