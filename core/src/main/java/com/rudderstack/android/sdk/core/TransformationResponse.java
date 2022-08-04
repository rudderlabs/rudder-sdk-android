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
        final String id; //destination_id
        final int status;
        @Nullable
        final List<TransformedEvent> payload;

        public TransformedDestination(@Nullable String id, int status, @Nullable List<TransformedEvent> payload) {
            this.id = id;
            this.status = status;
            this.payload = payload;
        }
    }

    static class TransformedEvent {
        final int orderNo;
        final String status;
        final RudderMessage event;

        public TransformedEvent(int orderNo, String status, RudderMessage event) {
            this.orderNo = orderNo;
            this.status = status;
            this.event = event;
        }
    }
}
