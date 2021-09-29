package com.rudderstack.android.sdk.core;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

class RudderServerDestinationDefinition implements Serializable {
    @SerializedName("name")
    String definitionName;
    @SerializedName("displayName")
    String displayName;
    @SerializedName("updatedAt")
    String updatedAt;
}
