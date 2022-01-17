package com.rudderstack.android.sdk.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for Event Filtering
 */
class RudderEventFilteringPlugin {

    static final String DISABLE = "disable";
    static final String WHITELISTED_EVENTS = "whitelistedEvents";
    static final String BLACKLISTED_EVENTS = "blacklistedEvents";
    static final String EVENT_FILTERING_OPTION = "eventFilteringOption";
    private static final String EVENT_NAME = "eventName";

    private Map<String, String> eventFilteringOption = new HashMap<>();
    private Map<String, List<String>> whitelistEvents = new HashMap<>();
    private Map<String, List<String>> blacklistEvents = new HashMap<>();

    RudderEventFilteringPlugin(List<RudderServerDestination> destinations) {
        if (!destinations.isEmpty()) {
            // Iterate all destinations
            for (RudderServerDestination destination:destinations) {
                Map<String, Object> destinationConfig = (Map<String, Object>) destination.destinationConfig;
                final String destinationDefinitionDisplayName = destination.destinationDefinition.displayName;
                final String eventFilteringStatus = destinationConfig.containsKey(EVENT_FILTERING_OPTION)
                        ? (String) destinationConfig.get(EVENT_FILTERING_OPTION) : DISABLE;
                if (!eventFilteringStatus.equals(DISABLE) && !eventFilteringOption.containsKey(destinationDefinitionDisplayName)) {
                    eventFilteringOption.put(destinationDefinitionDisplayName, eventFilteringStatus);
                    // If it is whiteListed events
                    if (eventFilteringStatus.equals(WHITELISTED_EVENTS) && destinationConfig.containsKey(WHITELISTED_EVENTS) ) {
                        setEvent(destinationDefinitionDisplayName,
                                (List<Map<String, String>>) destinationConfig.get(WHITELISTED_EVENTS),
                                whitelistEvents);
                    }
                    // If it is blackListed events
                    else if (eventFilteringStatus.equals(BLACKLISTED_EVENTS) && destinationConfig.containsKey(BLACKLISTED_EVENTS)) {
                      setEvent(destinationDefinitionDisplayName,
                              (List<Map<String, String>>) destinationConfig.get(BLACKLISTED_EVENTS),
                              blacklistEvents);
                    }
                }
            }
        }
    }

    /**
     * setEvent - It sets either the whitelist or blacklist depending on the eventFilteringOption configured at the dashboard
     *
     * @param destinationDefinitionDisplayName A string containing the destination name.
     * @param configEventsObject A {@code ArrayList<LinkedHashMap<String, String>>} which contains all the blacklist or whitelist event configured at the dashboard.
     * @param whiteOrBlackListEvent A {@code Map<String, ArrayList<String>>} with key/value pairs that will store the whitelist or blacklist event depending on the eventFilteringOption configured at the dashboard.
     */
    private void setEvent(String destinationDefinitionDisplayName,
                          List<Map<String, String>> configEventsObject,
                          Map<String, List<String>> whiteOrBlackListEvent) {
        whiteOrBlackListEvent.put(destinationDefinitionDisplayName, new ArrayList<String>());
        for (Map<String, String> whiteListedEvent : configEventsObject) {
            String eventName = String.valueOf(whiteListedEvent.get(EVENT_NAME)).trim();
            if (!TextUtils.isEmpty(eventName)) {
                whiteOrBlackListEvent.get(destinationDefinitionDisplayName).add(eventName);
            }
        }
    }

    /**
     * It return true if event is either whitelisted or not blocked else it return false.
     *
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @param message A {@code RudderMessage} object.
     * @return A {@code boolean} value true if event is allowed to execute else false if it is blocked.
     */
    boolean isEventAllowed(@NonNull String destinationName, @Nullable RudderMessage message) {
        if (message != null
                && !TextUtils.isEmpty(message.getType())
                && message.getType().equals(MessageType.TRACK)
                && !TextUtils.isEmpty(message.getEventName()))
            // If destination is configured to whitelist or blacklist then proceed
            if (isEventFilterEnabled(destinationName)) {
                boolean isEventAllowed;
                if (getEventFilterType(destinationName).equals(WHITELISTED_EVENTS)) {
                    isEventAllowed = getWhitelistEvents(destinationName).contains(message.getEventName().trim());
                } else {
                    isEventAllowed = !getBlacklistEvents(destinationName).contains(message.getEventName().trim());
                }
                handleLogMessage(isEventAllowed, destinationName, message.getEventName().trim());
                return isEventAllowed;
            }
        return true;
    }

    /**
     * It logs message to the console if the event is not allowed because of being blocked or not whitelisted.
     *
     * @param isEventAllowed A {@code boolean} value to decide that current event is allowed to be executed or not.
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @param eventName A {@code String} value which refers to the event name.
     */
    private void handleLogMessage(boolean isEventAllowed, String destinationName, String eventName) {
        if (!isEventAllowed) {
            if (getEventFilterType(destinationName).equals(WHITELISTED_EVENTS)) {
                RudderLogger.logInfo("Since " + eventName + " event is not Whitelisted it is being dropped.");
                return;
            }
            RudderLogger.logInfo("Since " + eventName + " event is Blacklisted it is being dropped.");
        }
    }

    /**
     *
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @return A {@code boolean} value true if there exists the destination and it is set to either whitelist or blacklist else false.
     */
    boolean isEventFilterEnabled(String destinationName) {
        return eventFilteringOption.containsKey(destinationName);
    }

    /**
     *
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @return It returns three things: whitelisted, blacklisted or NULL value.
     */
    String getEventFilterType(String destinationName) {
        return eventFilteringOption.get(destinationName);
    }

    /**
     *
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @return It returns {@code ArrayList<String>} value containing all the whitelisted events of the given destination type.
     */
    List<String> getWhitelistEvents(String destinationName) {
        return whitelistEvents.get(destinationName);
    }

    /**
     *
     * @param destinationName A {@code String} value which refers to the destination which event belongs to.
     * @return It returns {@code ArrayList<String>} value containing all the blacklisted events of the given destination type.
     */
    List<String> getBlacklistEvents(String destinationName) {
        return blacklistEvents.get(destinationName);
    }
}