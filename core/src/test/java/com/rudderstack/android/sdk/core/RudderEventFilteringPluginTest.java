package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.BLACKLISTED_EVENTS;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.DISABLE;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.EVENT_FILTERING_OPTION;
import static com.rudderstack.android.sdk.core.RudderEventFilteringPlugin.WHITELISTED_EVENTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    private RudderEventFilteringPlugin plugin_Whitelist_LifeCycle;
    private RudderEventFilteringPlugin plugin_Blacklist_LifeCycle;
    private RudderEventFilteringPlugin plugin_Whitelist_Empty;
    private RudderEventFilteringPlugin plugin_Blacklist_Empty;
    private RudderEventFilteringPlugin plugin_WithMultipleDestinations;
    private RudderEventFilteringPlugin getPlugin_WithMultipleDestinationsOfSameType;
    private RudderMessage message;

    @Before
    public void setup() {
        mockTheLog();
        PowerMockito.mockStatic(TextUtils.class);
         message = PowerMockito.mock(RudderMessage.class);


        // Blacklisted
        final List<RudderServerDestination> testDestinations_Amplitude_BlacklistedEvents = new ArrayList();
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
        testDestinations_Amplitude_BlacklistedEvents.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, blackListedEvents
        ));

        // Whitelisted
        final List<RudderServerDestination> testDestinations_Amplitude_WhitelistedEvents = new ArrayList();
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
        testDestinations_Amplitude_WhitelistedEvents.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.WHITE_LISTED,
                whiteListedEvents, null
        ));

        // Disable
        final List<RudderServerDestination> testDestinations_Amplitude_Disable = new ArrayList();
        testDestinations_Amplitude_Disable.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.DISABLE,
                null, blackListedEvents
        ));

        // Lifecycle - Whitelist
        final List<RudderServerDestination> testDestinations_Amplitude_Whitelist_LifeCycle = new ArrayList();
        List<Map<String, String>> lifeCycleEvents_Whitelist = new ArrayList<>();
        Map<String, String> event3_1 = new HashMap<>();
        event3_1.put("eventName", "Application Updated");
        Map<String, String> event3_2 = new HashMap<>();
        event3_2.put("eventName", "Application Opened");
        Map<String, String> event3_3 = new HashMap<>();
        event3_3.put("eventName", "Application Backgrounded");
        Map<String, String> event3_4 = new HashMap<>();
        event3_4.put("eventName", "Application Installed");
        lifeCycleEvents_Whitelist.add(event3_1);
        lifeCycleEvents_Whitelist.add(event3_2);
        lifeCycleEvents_Whitelist.add(event3_3);
        lifeCycleEvents_Whitelist.add(event3_4);
        testDestinations_Amplitude_Whitelist_LifeCycle.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.WHITE_LISTED,
                lifeCycleEvents_Whitelist, null
        ));

        // Lifecycle - Blacklist
        final List<RudderServerDestination> testDestinations_Amplitude_Blacklist_LifeCycle = new ArrayList();
        List<Map<String, String>> lifeCycleEvents_Blacklist = new ArrayList<>();
        Map<String, String> event4_1 = new HashMap<>();
        event4_1.put("eventName", "Application Updated");
        Map<String, String> event4_2 = new HashMap<>();
        event4_2.put("eventName", "Application Opened");
        Map<String, String> event4_3 = new HashMap<>();
        event4_3.put("eventName", "Application Backgrounded");
        Map<String, String> event4_4 = new HashMap<>();
        event4_4.put("eventName", "Application Installed");
        lifeCycleEvents_Blacklist.add(event4_1);
        lifeCycleEvents_Blacklist.add(event4_2);
        lifeCycleEvents_Blacklist.add(event4_3);
        lifeCycleEvents_Blacklist.add(event4_4);
        testDestinations_Amplitude_Blacklist_LifeCycle.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, lifeCycleEvents_Blacklist
        ));

        // Empty Whitelist
        final List<RudderServerDestination> testDestinations_Amplitude_EmptyWhitelist = new ArrayList();
        List<Map<String, String>> lifeCycleEvents_EmptyWhitelist = new ArrayList<>();
        testDestinations_Amplitude_EmptyWhitelist.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.WHITE_LISTED,
                lifeCycleEvents_EmptyWhitelist, null
        ));

        // Empty Blacklist
        final List<RudderServerDestination> testDestinations_Amplitude_EmptyBlacklist = new ArrayList();
        List<Map<String, String>> lifeCycleEvents_EmptyBlacklist = new ArrayList<>();
        testDestinations_Amplitude_EmptyBlacklist.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOIr", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, lifeCycleEvents_EmptyBlacklist
        ));

        // With multiple destinations
        final List<RudderServerDestination> testDestinations_Amplitude_MultipleDestinations = new ArrayList();
        // Blacklist
        List<Map<String, String>> blackListedEvent2 = new ArrayList<>();
        Map<String, String> event5_1 = new HashMap<>();
        event5_1.put("eventName", "Black-1");
        Map<String, String> event5_2 = new HashMap<>();
        event5_2.put("eventName", "Black-2");
        Map<String, String> event5_3 = new HashMap<>();
        event5_3.put("eventName", "Black-3");
        Map<String, String> event5_4 = new HashMap<>();
        event5_4.put("eventName", "Black-4");
        blackListedEvent2.add(event5_1);
        blackListedEvent2.add(event5_2);
        blackListedEvent2.add(event5_3);
        blackListedEvent2.add(event5_4);
        testDestinations_Amplitude_MultipleDestinations.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOI1", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, blackListedEvent2
        ));
        // Whitelisted
        List<Map<String, String>> whiteListedEvents2 = new ArrayList<>();
        Map<String, String> event6_1 = new HashMap<>();
        event6_1.put("eventName", "White-1");
        Map<String, String> event6_2 = new HashMap<>();
        event6_2.put("eventName", "White-2");
        Map<String, String> event6_3 = new HashMap<>();
        event6_3.put("eventName", "White-3");
        Map<String, String> event6_4 = new HashMap<>();
        event6_4.put("eventName", "White-4");
        whiteListedEvents2.add(event6_1);
        whiteListedEvents2.add(event6_2);
        whiteListedEvents2.add(event6_3);
        whiteListedEvents2.add(event6_4);
        testDestinations_Amplitude_MultipleDestinations.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOI2", "Firebase Event Filtering",
                true, "FIREBASE",
                "Firebase", EventFilteringStatus.WHITE_LISTED,
                whiteListedEvents2, null
        ));

        // With multiple destinations of same type
        final List<RudderServerDestination> testDestinations_Amplitude_MultipleDestinationsOfSameType = new ArrayList();
        // Blacklist
        List<Map<String, String>> blackListedEvent3 = new ArrayList<>();
        Map<String, String> event8_1 = new HashMap<>();
        event8_1.put("eventName", "Black-1");
        Map<String, String> event8_2 = new HashMap<>();
        event8_2.put("eventName", "Black-2");
        Map<String, String> event8_3 = new HashMap<>();
        event8_3.put("eventName", "Black-3");
        Map<String, String> event8_4 = new HashMap<>();
        event8_4.put("eventName", "Black-4");
        blackListedEvent3.add(event8_1);
        blackListedEvent3.add(event8_2);
        blackListedEvent3.add(event8_3);
        blackListedEvent3.add(event8_4);
        testDestinations_Amplitude_MultipleDestinationsOfSameType.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOI1", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.BLACK_LISTED,
                null, blackListedEvent3
        ));
        // Whitelisted
        List<Map<String, String>> whiteListedEvents3 = new ArrayList<>();
        Map<String, String> event7_1 = new HashMap<>();
        event7_1.put("eventName", "White-1");
        Map<String, String> event7_2 = new HashMap<>();
        event7_2.put("eventName", "White-2");
        Map<String, String> event7_3 = new HashMap<>();
        event7_3.put("eventName", "White-3");
        Map<String, String> event7_4 = new HashMap<>();
        event7_4.put("eventName", "White-4");
        whiteListedEvents3.add(event7_1);
        whiteListedEvents3.add(event7_2);
        whiteListedEvents3.add(event7_3);
        whiteListedEvents3.add(event7_4);
        testDestinations_Amplitude_MultipleDestinationsOfSameType.add(createServerDestination("23YLpuMKgF5t5U6Ac7HcY5seOI1", "Amplitude Event Filtering",
                true, "AM",
                "Amplitude", EventFilteringStatus.WHITE_LISTED,
                whiteListedEvents3, null
        ));

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
        plugin_Whitelist_LifeCycle = new RudderEventFilteringPlugin(testDestinations_Amplitude_Whitelist_LifeCycle);
        plugin_Blacklist_LifeCycle = new RudderEventFilteringPlugin(testDestinations_Amplitude_Blacklist_LifeCycle);
        plugin_Whitelist_Empty = new RudderEventFilteringPlugin(testDestinations_Amplitude_EmptyWhitelist);
        plugin_Blacklist_Empty = new RudderEventFilteringPlugin(testDestinations_Amplitude_EmptyBlacklist);
        plugin_WithMultipleDestinations = new RudderEventFilteringPlugin(testDestinations_Amplitude_MultipleDestinations);
        getPlugin_WithMultipleDestinationsOfSameType = new RudderEventFilteringPlugin(testDestinations_Amplitude_MultipleDestinationsOfSameType);
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
    public void isEventAllowed_eventFilterOptionIsBlacklisted() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        PowerMockito.when(message,"getEventName").thenReturn("Black-1");
        assertFalse(plugin_Blacklist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-2");
        assertFalse(plugin_Blacklist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-3");
        assertFalse(plugin_Blacklist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-4");
        assertFalse(plugin_Blacklist.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("AsNotBlacklisted-EventAllowed");
        assertTrue(plugin_Blacklist.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_eventFilterOptionIsWhitelisted() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        PowerMockito.when(message,"getEventName").thenReturn("White-1");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-2");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-3");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-4");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("AsNotWhitelisted-EventBlocked");
        assertFalse(plugin_Whitelist.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_getTypeReturnsNullOrEmpty() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");

        // When message.getType() == null or empty -> Allow the event
        PowerMockito.when(message,"getType").thenReturn(null);
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getType").thenReturn("");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_getEventNameReturnsNullOrEmpty() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        // When message.getEventName == null or empty -> Allow the event
        PowerMockito.when(message,"getEventName").thenReturn(null);
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_eventFilterOptionIsDisable() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");

        // When eventFilterOption is disable (backward compatibility) -> Allow the event
        assertTrue(plugin_Disable.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_eventFilterOptionDoNotContainGivenDestination() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);
        PowerMockito.when(message,"getEventName").thenReturn("CouldBeAnything");

        assertTrue(plugin_Whitelist.isEventAllowed("Adobe", message));
        assertTrue(plugin_Blacklist.isEventAllowed("Adobe", message));
    }

    @Test
    public void isEventAllowed_EventNameWithTrailingAndLeadingSpaces() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        // Event name with trailing and leading white spaces -> If the event is whitelisted or not blacklisted then allow the event.
        PowerMockito.when(message,"getEventName").thenReturn("    AsNotBlacklisted-EventAllowed     ");
        assertTrue(plugin_Blacklist.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("      White-1         ");
        assertTrue(plugin_Whitelist.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_LifeCycleWhitelist() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        // LifeCycle whitelist events
        PowerMockito.when(message,"getEventName").thenReturn("Application Updated");
        assertTrue(plugin_Whitelist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Opened");
        assertTrue(plugin_Whitelist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Backgrounded");
        assertTrue(plugin_Whitelist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Installed");
        assertTrue(plugin_Whitelist_LifeCycle.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("AsNotWhitelisted-EventBlocked");
        assertFalse(plugin_Whitelist_LifeCycle.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_LifeCycleBlacklist() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        // LifeCycle blacklist events
        PowerMockito.when(message,"getEventName").thenReturn("Application Updated");
        assertFalse(plugin_Blacklist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Opened");
        assertFalse(plugin_Blacklist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Backgrounded");
        assertFalse(plugin_Blacklist_LifeCycle.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Application Installed");
        assertFalse(plugin_Blacklist_LifeCycle.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("AsNotBlacklisted-EventAllowed");
        assertTrue(plugin_Blacklist_LifeCycle.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_WhitelistEmpty_BlockAllEvents() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        PowerMockito.when(message,"getEventName").thenReturn("White-1");
        assertFalse(plugin_Whitelist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-2");
        assertFalse(plugin_Whitelist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-3");
        assertFalse(plugin_Whitelist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("RandomBlacklistEvents");
        assertFalse(plugin_Whitelist_Empty.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_BlacklistEmpty_AllowAllEvents() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        PowerMockito.when(message,"getEventName").thenReturn("Black-1");
        assertTrue(plugin_Blacklist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-2");
        assertTrue(plugin_Blacklist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-3");
        assertTrue(plugin_Blacklist_Empty.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("RandomBlacklistEvents");
        assertTrue(plugin_Blacklist_Empty.isEventAllowed("Amplitude", message));
    }

    @Test
    public void isEventAllowed_MultipleDestinations() throws Exception {
        PowerMockito.when(message,"getType").thenReturn(MessageType.TRACK);

        // Here destinations: Amplitude events is Blacklisted and Firebase events is Whitelisted
        PowerMockito.when(message,"getEventName").thenReturn("Black-1");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-2");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-3");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("Black-4");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("White-1");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));
        PowerMockito.when(message,"getEventName").thenReturn("AsNotBlacklistedThenAllowed");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Amplitude", message));

        PowerMockito.when(message,"getEventName").thenReturn("White-1");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-2");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-3");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));
        PowerMockito.when(message,"getEventName").thenReturn("White-4");
        assertTrue(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));

        PowerMockito.when(message,"getEventName").thenReturn("Black-1");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));
        PowerMockito.when(message,"getEventName").thenReturn("AsNotWhitelistedThenBlocked");
        assertFalse(plugin_WithMultipleDestinations.isEventAllowed("Firebase", message));
    }

    @Test
    public void getWhitelistEvents() {
        List<String> whiteListedEvents = new ArrayList<>();
        whiteListedEvents.add( "White-1");
        whiteListedEvents.add( "White-2");
        whiteListedEvents.add( "White-3");
        whiteListedEvents.add( "White-4");
        assertThat(plugin_Whitelist.getWhitelistEvents("Amplitude"), is(whiteListedEvents));

        assertNull(plugin_Blacklist.getWhitelistEvents("Amplitude"));
        assertNull(plugin_Blacklist_Empty.getWhitelistEvents("Amplitude"));
        assertNotNull(plugin_Whitelist_Empty.getWhitelistEvents("Amplitude"));
    }

    @Test
    public void getBlacklistEvents() {
        List<String> blackListedEvents = new ArrayList<>();
        blackListedEvents.add( "Black-1");
        blackListedEvents.add( "Black-2");
        blackListedEvents.add( "Black-3");
        blackListedEvents.add( "Black-4");
        assertThat(plugin_Blacklist.getBlacklistEvents("Amplitude"), is(blackListedEvents));

        assertNull(plugin_Whitelist.getBlacklistEvents("Amplitude"));
        assertNull(plugin_Whitelist_Empty.getBlacklistEvents("Amplitude"));
        assertNotNull(plugin_Blacklist_Empty.getBlacklistEvents("Amplitude"));
    }

    @Test
    public void isEventFilterEnabled() {
        // Destinations which is configured to either whitelist or blacklist
        assertTrue(plugin_Whitelist.isEventFilterEnabled("Amplitude"));
        assertTrue(plugin_Blacklist.isEventFilterEnabled("Amplitude"));
        assertTrue(plugin_Whitelist_Empty.isEventFilterEnabled("Amplitude"));
        assertTrue(plugin_Blacklist_Empty.isEventFilterEnabled("Amplitude"));

        // Destinations which is configured to disable (Backward compatibility i.e., when destination is by default set to disable)
        assertFalse(plugin_Disable.isEventFilterEnabled("Firebase"));
        assertFalse(plugin_Whitelist.isEventFilterEnabled("Firebase"));
        assertFalse(plugin_Blacklist.isEventFilterEnabled("Firebase"));
        assertFalse(plugin_Whitelist_Empty.isEventFilterEnabled("Firebase"));
        assertFalse(plugin_Blacklist_Empty.isEventFilterEnabled("Firebase"));
    }

    @Test
    public void getEventFilterType() {
        assertThat(plugin_Whitelist.getEventFilterType("Amplitude"), is(WHITELISTED_EVENTS));
        assertThat(plugin_Blacklist.getEventFilterType("Amplitude"), is(BLACKLISTED_EVENTS));
        assertNull(plugin_Disable.getEventFilterType("Amplitude"));
        assertNull(plugin_Disable.getEventFilterType("Firebase"));
        assertThat(plugin_Whitelist_Empty.getEventFilterType("Amplitude"), is(WHITELISTED_EVENTS));
        assertThat(plugin_Blacklist_Empty.getEventFilterType("Amplitude"), is(BLACKLISTED_EVENTS));
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