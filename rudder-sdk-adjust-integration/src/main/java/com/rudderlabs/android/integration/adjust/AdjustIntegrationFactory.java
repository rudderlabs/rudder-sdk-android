package com.rudderlabs.android.integration.adjust;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustInstance;
import com.adjust.sdk.LogLevel;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjustIntegrationFactory extends RudderIntegration<AdjustInstance> {
    static final String ADJUST_KEY = "ADJ";
    static final String ADJUST_DISPLAY_NAME = "Adjust";
    private static final String ADJUST_TYPE = "type";
    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client) {
            return new AdjustIntegrationFactory(settings, client);
        }

        @Override
        public String key() {
            return ADJUST_KEY;
        }
    };

    private final AdjustInstance adjust;
    private Map<String, String> eventMap = new HashMap<>();

    private AdjustIntegrationFactory(Object config, RudderClient client) {
        this.adjust = Adjust.getDefaultInstance();
        String apiToken = "";
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig.containsKey("apiToken")) apiToken = (String) destinationConfig.get("apiToken");
        if (destinationConfig.containsKey("eventTokenMap")){
            List<Object> eventList = (List<Object>) destinationConfig.get("eventTokenMap");
            if (eventList != null && !eventList.isEmpty()) {
                for (Object item : eventList) {
                    Map<String, String> keyMap = (Map<String, String>) item;
                    if (keyMap.containsKey("from") && keyMap.containsKey("to")) {
                        eventMap.put(keyMap.get("from"), keyMap.get("to"));
                    }
                }
            }
        }

        AdjustConfig adjustConfig = new AdjustConfig(
                client.getApplication(),
                apiToken,
                AdjustConfig.ENVIRONMENT_SANDBOX
        );
        adjustConfig.setLogLevel(LogLevel.VERBOSE);

        this.adjust.onCreate(adjustConfig);
    }

    private void processRudderEvent(RudderMessage element) {
        String eventToken = null;
        if (eventMap.containsKey(element.getEventName())) {
            eventToken = eventMap.get(element.getEventName());
        }
        if (eventToken == null) return;

        AdjustEvent event = new AdjustEvent(eventToken);
        event.addCallbackParameter(ADJUST_TYPE, element.getType());

        Map<String, Object> eventProperties = element.getProperties();
        if (eventProperties != null) {
            for (String key : eventProperties.keySet()) {
                event.addPartnerParameter(key, String.valueOf(eventProperties.get(key)));
            }

            if (eventProperties.containsKey("total") && eventProperties.containsKey("currency")) {
                event.setRevenue(
                        Double.parseDouble(String.valueOf(eventProperties.get("total"))),
                        String.valueOf(eventProperties.get("currency"))
                );
            }
        }

        Map<String, Object> userProperties = element.getUserProperties();
        if (userProperties != null) {
            for (String key : userProperties.keySet()) {
                event.addCallbackParameter(key, String.valueOf(userProperties.get(key)));
            }
        }

        Adjust.getDefaultInstance().trackEvent(event);
    }

    @Override
    public void track(RudderMessage track) {
        processRudderEvent(track);
    }

    @Override
    public void identify(RudderMessage identify) {
        processRudderEvent(identify);
    }

    @Override
    public void group(RudderMessage group) {
        processRudderEvent(group);
    }

    @Override
    public void alias(RudderMessage alias) {
        processRudderEvent(alias);
    }

    @Override
    public void screen(RudderMessage screen) {
        processRudderEvent(screen);
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void dump(RudderMessage element) {
        processRudderEvent(element);
    }

    @Override
    public AdjustInstance getUnderlyingInstance() {
        return adjust;
    }
}
