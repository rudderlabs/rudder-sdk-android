package com.rudderlabs.android.integration.dummy;

import com.google.gson.Gson;
import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderIntegration;
import com.rudderlabs.android.sdk.core.RudderMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DummyGAIntegrationImpl extends RudderIntegration<DummyGAIntegration> {
    private DummyGAIntegration integration;
    static final String DUMMY_KEY = "AM";
    static final String DUMMY_DISPLAY_NAME = "Dummy AM Integration";
    private static final String DUMMY_TYPE = "type";

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client) {
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
    public void identify(RudderMessage identify) {
        dump(identify);
    }

    @Override
    public void group(RudderMessage group) {
        dump(group);
    }

    @Override
    public void track(RudderMessage track) {
        dump(track);
    }

    @Override
    public void alias(RudderMessage alias) {
        dump(alias);
    }

    @Override
    public void screen(RudderMessage screen) {
        dump(screen);
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
    public void dump(RudderMessage message) {
        try {
            String eventName = message.getEventName();
            String eventType = message.getType();
            String userId = message.getUserId();
            Map<String, Object> eventProps = message.getProperties();
            String eventJson = new Gson().toJson(eventProps);
            System.out.println("eventJson: " + eventJson);
            JSONObject eventObject = new JSONObject(eventJson);
            Map<String, Object> userProps = message.getUserProperties();
            JSONObject userObject = null;
            if (userProps != null) {
                String userJson = new Gson().toJson(userProps);
                System.out.println("userJson: " + userJson);
                userObject = new JSONObject(userJson);
            }
            integration.dumpEvent(
                    eventName,
                    eventType,
                    userId,
                    eventObject,
                    userObject
            );
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public DummyGAIntegration getUnderlyingInstance() {
        return integration;
    }
}
