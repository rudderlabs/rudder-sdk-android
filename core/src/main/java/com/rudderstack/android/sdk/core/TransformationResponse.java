package com.rudderstack.android.sdk.core;

import androidx.annotation.Nullable;

import java.util.List;

public class TransformationResponse {
    @Nullable
    final List<TransformedDestination> transformedBatch;

    public TransformationResponse(@Nullable List<TransformedDestination> transformedBatch) {
        this.transformedBatch = transformedBatch;
    }

    static class TransformedDestination {
        @Nullable
        final TransformedPayload destination;

        public TransformedDestination(@Nullable TransformedPayload destination) {
            this.destination = destination;
        }
    }

    static class TransformedPayload {
        @Nullable
        final String id; //destination_id
        final int status;
        @Nullable
        final List<TransformedEvent> payload;

        public TransformedPayload(@Nullable String id, int status, @Nullable List<TransformedEvent> payload) {
            this.id = id;
            this.status = status;
            this.payload = payload;
        }
    }

    static class TransformedEvent {
        final int orderNo;
        final RudderMessage event;

        public TransformedEvent(int orderNo, RudderMessage event) {
            this.orderNo = orderNo;
            this.event = event;
        }
    }
}
