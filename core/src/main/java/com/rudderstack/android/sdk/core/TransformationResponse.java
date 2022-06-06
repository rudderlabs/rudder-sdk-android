package com.rudderstack.android.sdk.core;

import java.util.List;

public class TransformationResponse {
    final String id;
    final int status;
    final List<TransformedEvent> payload;

    public TransformationResponse(String id, int status, List<TransformedEvent> payload) {
        this.id = id;
        this.status = status;
        this.payload = payload;
    }

    static class TransformedEvent{
        final int orderNo;
        final RudderMessage event;

        public TransformedEvent(int orderNo, RudderMessage event) {
            this.orderNo = orderNo;
            this.event = event;
        }
    }
}
