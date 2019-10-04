package com.rudderlabs.android.sdk.ecomm;

import com.rudderlabs.android.sdk.core.RudderClient;
import com.rudderlabs.android.sdk.core.RudderMessageBuilder;
import com.rudderlabs.android.sdk.core.RudderException;
import com.rudderlabs.android.sdk.core.RudderUserProperty;

public class RudderECommerceClient extends RudderClient {

    public void track(ECommercePropertyBuilder builder) throws RudderException {
        track(builder, null, null);
    }

    public void track(ECommercePropertyBuilder builder, RudderUserProperty userProperty) throws RudderException {
        track(builder, userProperty, null);
    }

    public void track(ECommercePropertyBuilder builder, String userId) throws RudderException {
        track(builder, null, userId);
    }

    public void track(ECommercePropertyBuilder builder, RudderUserProperty userProperty, String userId) throws RudderException {
        RudderMessageBuilder elementBuilder = new RudderMessageBuilder()
                .setEventName(builder.event())
                .setProperty(builder.build());
        if (userProperty != null) elementBuilder.setUserProperty(userProperty);
        if (userId != null) elementBuilder.setUserId(userId);
        track(elementBuilder);
    }
}
