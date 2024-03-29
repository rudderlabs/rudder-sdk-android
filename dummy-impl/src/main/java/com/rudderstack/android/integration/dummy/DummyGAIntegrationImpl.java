package com.rudderstack.android.integration.dummy;

import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderMessage;


import java.util.Map;

public class DummyGAIntegrationImpl extends RudderIntegration<DummyGAIntegration> {
    private DummyGAIntegration integration;
    static final String DUMMY_KEY = "Firebase";
    static final String DUMMY_DISPLAY_NAME = "Dummy AM Integration";
    private static final String DUMMY_TYPE = "type";

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig config) {
            return new DummyGAIntegrationImpl(settings, client);
        }

        @Override
        public String key() {
            return DUMMY_KEY;
        }
    };

    private DummyGAIntegrationImpl(Object settings, RudderClient client) {
        Map<String, String> settingsMap = (Map<String, String>) settings;
        String key = settingsMap.get("apiKey");
        integration = DummyGAIntegration.getInstance(client.getApplication(), key);
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public void dump(RudderMessage message) {
        System.out.println("Event: " + message.getEventName() + " | Type: " + message.getType());
//        try {
//            String eventName = message.getEventName();
//            String eventType = message.getType();
//            String userId = message.getUserId();
//            Map<String, Object> eventProps = message.getProperties();
//            String eventJson = new Gson().toJson(eventProps);
//            System.out.println("eventJson: " + eventJson);
//            JSONObject eventObject = new JSONObject(eventJson);
//            Map<String, Object> userProps = message.getUserProperties();
//            JSONObject userObject = null;
//            if (userProps != null) {
//                String userJson = new Gson().toJson(userProps);
//                System.out.println("userJson: " + userJson);
//                userObject = new JSONObject(userJson);
//            }
//            integration.dumpEvent(
//                    eventName,
//                    eventType,
//                    userId,
//                    eventObject,
//                    userObject
//            );
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//        }
    }

    @Override
    public DummyGAIntegration getUnderlyingInstance() {
        return integration;
    }
}
