package com.rudderstack.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Map;

public class RedisStoreResponse {
    @Nullable
    Map<String, Object> payload;
    boolean success;
    @Nullable
    String errorMsg;

    /**
     * @return true if it is a success else false.
     */
    @NonNull
    public boolean getSuccess() {
        return success;
    }

    /**
     * @return sends back the payload if transform request is successful else return null.
     */
    @Nullable
    public Map<String, Object> getPayload() {
        return payload;
    }

    /**
     * @return error message if there is any kind of error while transformation else null.
     */
    @Nullable
    public String getErrorMsg() {
        return errorMsg;
    }
}
