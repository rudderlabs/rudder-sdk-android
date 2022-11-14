package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderDataResidencyUrls implements Serializable {
    @SerializedName("url")
    String url;
    @SerializedName("default")
    boolean defaultTo;
}