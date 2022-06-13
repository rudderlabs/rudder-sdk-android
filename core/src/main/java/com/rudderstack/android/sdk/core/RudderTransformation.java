package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RudderTransformation implements Serializable {
    @SerializedName("id")
    String transformationId;
    @SerializedName("name")
    String transformationName;
    @SerializedName("versionId")
    String transformationVersionId;
}
