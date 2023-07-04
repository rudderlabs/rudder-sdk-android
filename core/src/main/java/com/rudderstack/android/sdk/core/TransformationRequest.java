package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransformationRequest {
    final List<TransformationRequestEvent> batch;

    public TransformationRequest(List<TransformationRequestEvent> batch) {
        this.batch = batch;
    }

    static class TransformationRequestEvent {

        @SerializedName(value="rowId", alternate = {"orderNo"})
        final Integer rowId;
        @SerializedName(value="message", alternate = {"event"})
        final RudderMessage message;
        @SerializedName(value="destinationIds")
        final List<String> destinationIds;

        private TransformationRequestEvent(Integer rowId, RudderMessage message, List<String> destinationIds) {
            this.rowId = rowId;
            this.message = message;
            this.destinationIds = destinationIds;
        }
    }
}
