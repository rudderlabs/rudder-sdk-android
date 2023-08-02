package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransformationRequest {
    final List<TransformationRequestEvent> batch;

    public TransformationRequest(List<TransformationRequestEvent> batch) {
        this.batch = batch;
    }

    static class TransformationRequestEvent {

        @SerializedName(value="orderNo")
        final Integer orderNo;
        @SerializedName(value="event")
        final RudderMessage event;
        @SerializedName(value="destinationIds")
        final List<String> destinationIds;

        public TransformationRequestEvent(Integer orderNo, RudderMessage event, List<String> destinationIds) {
            this.orderNo = orderNo;
            this.event = event;
            this.destinationIds = destinationIds;
        }
    }
}
