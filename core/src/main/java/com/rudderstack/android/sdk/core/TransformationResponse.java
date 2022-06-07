package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import java.util.List;

public class TransformationResponse {
    @Nullable final String id; //transformer_id
    final int status;
    @Nullable final List<TransformedEvent> payload;

    public TransformationResponse(@Nullable String id, int status,@Nullable List<TransformedEvent> payload) {
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
