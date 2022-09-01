package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

public class RudderSessionInfo {
    @SerializedName("id")
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    @SerializedName("start")
    private Boolean start;

    public void setStart(Boolean start) {
        this.start = start;
    }
}
