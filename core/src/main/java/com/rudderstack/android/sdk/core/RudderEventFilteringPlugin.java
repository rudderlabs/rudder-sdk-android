package com.rudderstack.android.sdk.core;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RudderEventFilteringPlugin {

    private static final String DISABLE = "disable";
    private static final String WHITELIST = "whitelist";
    private static final String BLACKLIST = "blacklist";
    private static final String EVENT_FILTERING_OPTION = "eventFilteringOption";
    private static final String EVENT_NAME = "eventName";

    private Map<String, String> eventFilteringOption = new HashMap<>();
    private Map<String, ArrayList<String>> whitelist = new HashMap<>();
    private Map<String, ArrayList<String>> blacklist = new HashMap<>();

    RudderEventFilteringPlugin(List<RudderServerDestination> destinations) {
        if (!destinations.isEmpty()) {
            // Iterate all destinations
            for (RudderServerDestination destination:destinations) {
                Map<String, Object> destinationConfig = (Map<String, Object>) destination.destinationConfig;
                final String destinationName = destination.destinationDefinition.displayName;
                final String eventFilteringStatus = destinationConfig.containsKey(EVENT_FILTERING_OPTION)
                        ? (String) destinationConfig.get(EVENT_FILTERING_OPTION) : DISABLE;
                if (!eventFilteringStatus.equals(DISABLE) && !eventFilteringOption.containsKey(destinationName)) {
                    eventFilteringOption.put(destinationName, eventFilteringStatus);
                    // If it is whiteListed events
                    if (eventFilteringStatus.equals(WHITELIST) &&
                            destinationConfig.containsKey(WHITELIST) ) {
                        setEvent(destinationName,
                                (ArrayList<LinkedHashMap<String, String>>) destinationConfig.get(WHITELIST),
                                whitelist);
                    }
                    // If it is blackListed events
                    else if (eventFilteringStatus.equals(BLACKLIST) &&
                            destinationConfig.containsKey(BLACKLIST)) {
                      setEvent(destinationName,
                              (ArrayList<LinkedHashMap<String, String>>) destinationConfig.get(BLACKLIST),
                              blacklist);
                    }
                }
            }
        }
    }

    /**
     * setEvent - It sets either the whitelist or blacklist depending on the eventFilteringOption configured at the dashboard
     *
     * @param destinationName A string containing the destination name.
     * @param configEventsObject A {@code ArrayList<LinkedHashMap<String, String>>} which contains all the blacklist or whitelist event configured at the dashboard.
     * @param whiteOrBlackListEvent A {@code Map<String, ArrayList<String>>} with key/value pairs that will store the whitelist or blacklist event depending on the eventFilteringOption configured at the dashboard.
     */
    private void setEvent(String destinationName,
                          ArrayList<LinkedHashMap<String, String>> configEventsObject,
                          Map<String, ArrayList<String>> whiteOrBlackListEvent) {
        whiteOrBlackListEvent.put(destinationName, new ArrayList<String>());
        for (LinkedHashMap<String, String> whiteListedEvent : configEventsObject) {
            String eventName = String.valueOf(whiteListedEvent.get(EVENT_NAME));
            if (!TextUtils.isEmpty(eventName)) {
                whiteOrBlackListEvent.get(destinationName).add(eventName);
            }
        }
    }

    boolean isEventAllowed(@Nullable String destinationName, @Nullable RudderMessage message) {
        if (message != null && !TextUtils.isEmpty(message.getType()) && message.getType().equals(MessageType.TRACK))
            // If destination is configured to whitelist or blacklist then proceed
            if (eventFilteringOption.containsKey(destinationName)) {
                if (eventFilteringOption.get(destinationName).equals(WHITELIST)) {
                    return whitelist.get(destinationName).contains(message.getEventName());
                }
                return !blacklist.get(destinationName).contains(message.getEventName());
            }
        return true;
    }

    Map<String, String> getEventFilteringOption() {
        return eventFilteringOption;
    }

    Map<String, ArrayList<String>> getWhitelist() {
        return whitelist;
    }

    Map<String, ArrayList<String>> getBlacklist() {
        return blacklist;
    }
}