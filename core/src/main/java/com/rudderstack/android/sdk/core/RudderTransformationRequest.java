package com.rudderstack.android.sdk.core;

import java.util.List;

public class RudderTransformationRequest {
    private RudderMessage message;
    private List<String> destinationIds;

    public RudderTransformationRequest() {
    }

    public void setMessage(RudderMessage message) {
        this.message = message;
    }

    public void setDestinationIds(List<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    public RudderMessage getMessage() {
        return message;
    }

    public List<String> getDestinationIds() {
        return destinationIds;
    }
}
