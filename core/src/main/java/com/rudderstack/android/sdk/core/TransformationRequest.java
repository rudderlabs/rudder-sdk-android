package com.rudderstack.android.sdk.core;

import java.util.List;

public class TransformationRequest {
    final List<TransformationRequestEvent> batch;

    public TransformationRequest(List<TransformationRequestEvent> batch) {
        this.batch = batch;
    }

    static class TransformationRequestEvent {

        final Integer rowId;
        final RudderMessage message;

        private TransformationRequestEvent(Integer rowId, RudderMessage message) {
            this.rowId = rowId;
            this.message = message;
        }
    }
}
